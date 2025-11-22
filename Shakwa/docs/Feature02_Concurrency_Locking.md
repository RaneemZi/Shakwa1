# Feature 02 – Concurrency Control / Complaint Locking

## Goal
Prevent two employees from editing the same complaint simultaneously, fulfilling the "reservation" requirement.

## Requirements
- When an employee opens a complaint for processing, it should be "reserved" to prevent concurrent edits.
- Other employees should see a warning that the complaint is being processed by another user.
- Prevent concurrent modifications using database-level locking mechanisms.

## Implementation Approach
**Using Optimistic + Pessimistic Locking at Database/Transaction Level**

### Design
1. **DB Changes**: 
   - Add `version` column (BIGINT) to `complaints` table for optimistic locking.
   - JPA `@Version` annotation automatically handles version increment.

2. **Service Layer**:
   - **State-Based Locking**: Check if `Status = IN_PROGRESS` and `respondedBy != currentEmployee` to prevent concurrent edits.
   - **Pessimistic Locking**: Use `@Lock(LockModeType.PESSIMISTIC_WRITE)` when reading complaint for update.
   - **Optimistic Locking**: JPA automatically checks version on save, throws `OptimisticLockException` if version mismatch.
   - `updateComplaint()` and `respondToComplaint()`: Use state-based check + pessimistic lock + optimistic lock validation.

3. **API Changes**:
   - Existing endpoints automatically use locking:
     - `PUT /api/v1/complaints/{id}` – Uses state-based + pessimistic + optimistic locking.
     - `PUT /api/v1/complaints/{id}/respond` – Uses state-based + pessimistic + optimistic locking.
     - When employee sets `status=IN_PROGRESS`, the complaint is automatically "locked" to that employee.

4. **Error Handling**:
   - `OptimisticLockException` → HTTP 409 CONFLICT with Arabic message.
   - Pessimistic lock waits until transaction completes (database-level blocking).

5. **UI Considerations**:
   - Response payload includes `version` field for client-side conflict detection.
   - Client should refresh and retry on 409 CONFLICT error.

## Testing
- Integration tests ensuring concurrent update attempts fail with 409 Conflict.
- Tests for pessimistic lock blocking behavior.
- Tests for optimistic lock version checking.

## Benefits of This Approach
- **Natural and user-friendly**: Status reflects reality - "IN_PROGRESS" means "being processed".
- **No manual lock management**: State-based locking is automatic - no need for explicit lock/unlock.
- **No scheduled tasks**: No need for lock expiration cleanup.
- **Transaction-level**: Pessimistic locks are automatically released when transaction completes.
- **Database-level consistency**: Works across multiple application instances.
- **Simpler code**: No custom LockService needed - just a simple state check.

## See Also
- `docs/Feature02_Why_State_Based_Locking.md` - Detailed explanation of why State-Based Locking is the correct approach.
- `docs/Feature02_Complete_Documentation.md` - Complete documentation with flows and examples.
- `docs/Feature02_Final_Summary.md` - Final implementation summary.

