# Feature 05 – Caching Strategy

## Goal
Improve response times and reduce database load while maintaining consistency and authorization guarantees.

## Scope
- Read-heavy endpoints: complaint listings, metadata (complaint types, governorates, agencies), dashboard stats.
- Token/user context must remain secure (no caching per-user sensitive data).

## Approach
1. **Technology**: Spring Cache abstraction backed by Redis.
2. **Cache Layers**:
   - **Reference data** (`ComplaintType`, `Governorate`, `GovernmentAgencyType`) – cache indefinitely with manual invalidation.
   - **Complaint list filters** – cache per combination of query parameters + user role for short TTL (e.g., 60 seconds) to avoid stale data.
   - **Dashboard metrics** – compute every N minutes, cached centrally.
3. **Key Design**:
   - Use namespaced keys `complaints:list:{role}:{agencyId}:{filtersHash}`.
   - Include version counter to invalidate on write operations (increment counter whenever complaint changes, forming part of key).
4. **Eviction**:
   - On complaint create/update/delete/respond, evict relevant caches via Spring cache eviction annotations or manual calls (e.g., `@CacheEvict` on service methods).
5. **Configuration**:
   - Properties: host, port, SSL, TTLs.
   - Fallback to no-cache if Redis unavailable (fail-safe).

## Testing
- Unit tests for cache key generation + eviction.
- Integration tests verifying caching improves response but respects permission boundaries.
- Load tests comparing with/without caching.

## Ops
- Set up Redis monitoring (memory usage, eviction rate).
- Document flush procedures for emergency.

