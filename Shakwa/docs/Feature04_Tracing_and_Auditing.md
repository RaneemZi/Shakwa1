# Feature 04 – Comprehensive Tracing & Auditing

## Goal
Provide a system-wide audit trail capturing every user action, who performed it, when, and the outcome, independent of complaint-specific history.

## Requirements
- Record login attempts (success/failure), CRUD actions, lock events, file downloads, configuration changes.
- Support querying by timeframe, user, action type.
- Export logs for admin review (CSV/PDF).
- Retain audit data for at least 12 months.
- Integrate with monitoring/alerting (e.g., failed login threshold).

## Architecture
1. **Audit Event Model**
   - Table `audit_events`:
     - `id`, `timestamp`, `actor_id`, `actor_role`, `action`, `target_type`, `target_id`, `ip_address`, `user_agent`, `status` (SUCCESS/FAILURE), `details` (JSON).
2. **AuditService**
   - Central component with API `record(action, targetType, targetId, status, details)`.
   - Called from controllers/services or via aspects (see Feature 07).
3. **APIs**
   - `GET /api/v1/audit-events` – Admin-only, paginated, filters (user, action, status, date).
   - `GET /api/v1/audit-events/export` – Generates CSV/PDF.
4. **Retention**
   - Scheduled job to archive events older than retention window to cold storage.
5. **Integration Points**
   - Login/auth filter (success/failure).
   - Complaint operations.
   - Attachments download (log file access).
   - Admin configuration changes (roles, permissions).

## Implementation Steps
1. Add table + entity + repository.
2. Implement `AuditService`.
3. Add audit calls in auth filter and complaint service methods (create, update, respond, lock, attachment operations).
4. Build admin endpoints with filtering/export support.
5. Add unit tests for service and integration tests verifying entries.

## Notes
- Sensitive fields (passwords, tokens) must never be stored in audit details.
- Consider asynchronous processing (queue) if volume grows.

