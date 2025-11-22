# Feature 05 – Caching Strategy

## Goal
Improve response times and reduce database load while maintaining consistency and authorization guarantees.

## Scope
- Read-heavy endpoints: complaint listings, metadata (complaint types, governorates, agencies), dashboard stats.
- Token/user context must remain secure (no caching per-user sensitive data).

## Approach
1. **Technology**: Spring Cache abstraction using in-memory ConcurrentMapCacheManager (no Redis).
2. **Cache Layers**:
   - **Reference data** (`ComplaintType`, `Governorate`, `GovernmentAgencyType`) – cached indefinitely with manual invalidation via `MetadataService.evictReferenceDataCache()`.
   - **Complaint list filters** – cached per combination of query parameters + user context. Cache evicted on write operations to ensure consistency.
   - **Dashboard metrics** – cache name reserved for future dashboard implementation.
3. **Key Design**:
   - Cache keys generated at repository level using SpEL expressions based on method parameters.
   - Format: `complaints:{filterType}:{filterValue}:page:{page}:size:{size}` (for paginated queries)
   - Format: `complaints:{filterType}:{filterValue}` (for list queries)
   - Format: `complaint:id:{id}` (for single entity lookups)
   - Examples: `complaints:citizen:1:page:0:size:10`, `complaints:agency:وزارة_الصحة:status:PENDING:page:0:size:10`
4. **Eviction**:
   - Cache eviction handled automatically at repository level via overridden methods:
     - `save()`, `saveAll()` - evicts all complaint list caches
     - `delete()`, `deleteById()`, `deleteAll()` - evicts all complaint list caches
   - This ensures data consistency whenever complaints are created, updated, or deleted.
   - Reference data can be manually evicted via `MetadataService.evictReferenceDataCache()` when needed.
5. **Configuration**:
   - Cache configuration in `CacheConfig.java` using `ConcurrentMapCacheManager`.
   - No external dependencies required (pure in-memory caching).
   - Note: In-memory cache doesn't support TTL, but cache eviction on writes ensures data consistency.

## Testing
- Unit tests for cache key generation + eviction.
- Integration tests verifying caching improves response but respects permission boundaries.
- Load tests comparing with/without caching.

## Implementation Details

### Files Created/Modified:
1. **CacheConfig.java** - Cache configuration with ConcurrentMapCacheManager
2. **MetadataService.java** - Service for reference data with caching
3. **MetadataController.java** - REST endpoints for metadata (enums)
4. **CacheKeyUtils.java** - Utility for generating cache keys (kept for potential future use)
5. **ComplaintRepository.java** - Added `@Cacheable` and `@CacheEvict` annotations at repository level
6. **ComplaintService.java** - Removed cache annotations (caching now handled at repository level)
7. **pom.xml** - Added `spring-boot-starter-cache` dependency
8. **ShakwaApplication.java** - Added `@EnableCaching` annotation

### Caching Strategy:
- **Repository Level**: All repository query methods are cached, including:
  - `findById()`, `findAll(Pageable)`
  - `findByCitizenId()`, `findByGovernmentAgency()`, `findByStatus()`, etc.
  - All combination queries (e.g., `findByGovernmentAgencyAndStatus()`)
- **Cache Eviction**: Automatically handled at repository level via overridden `save()`, `saveAll()`, `delete()`, `deleteById()`, `deleteAll()` methods
- **Service Level**: No caching annotations - service layer focuses on business logic and DTO transformation
- **Note**: `filterComplaints()` uses `Specification` queries which are dynamic and not cached. Consider adding specific repository methods if this endpoint is frequently used.

### Cache Names:
- `referenceData`: For enum values (ComplaintType, Governorate, GovernmentAgencyType)
- `complaintLists`: For complaint listing queries
- `dashboardMetrics`: Reserved for future dashboard implementation

### API Endpoints:
- `GET /api/v1/metadata/complaint-types` - Get all complaint types
- `GET /api/v1/metadata/governorates` - Get all governorates
- `GET /api/v1/metadata/government-agencies` - Get all government agencies
- `GET /api/v1/metadata/complaint-types/labels` - Get complaint type labels
- `GET /api/v1/metadata/governorates/labels` - Get governorate labels
- `GET /api/v1/metadata/government-agencies/labels` - Get government agency labels

## Ops
- Monitor cache hit/miss rates via Spring Actuator (if enabled).
- For production with TTL requirements, consider migrating to Caffeine cache (still in-memory but supports TTL).
- Document cache eviction procedures for emergency cache clearing.

