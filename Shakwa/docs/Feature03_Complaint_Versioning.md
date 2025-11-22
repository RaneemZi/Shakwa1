# Feature 03 – Complaint Versioning & History Timeline

## Goal
Maintain an immutable timeline of every change to a complaint (status, description, attachments, comments) to satisfy transparency and tracing requirements.

## Requirements
- Record who changed what, when, and optional reason.
- Support querying history per complaint (ordered chronologically).
- Include history entries for: creation, status change, data edits, attachments added/removed, lock acquisition/release (optional).
- Expose timeline via REST API for dashboards/mobile.

## Design
1. **Data Model**:
   - New table `complaint_history` with columns:
     - `id` (PK)
     - `complaint_id` (FK)
     - `actor_id` (FK to user)
     - `action_type` (enum: CREATED, STATUS_CHANGED, UPDATED_FIELDS, ATTACHMENT_ADDED, ATTACHMENT_REMOVED, COMMENT_ADDED, LOCKED, UNLOCKED, etc.)
     - `field_changed` (nullable)
     - `old_value`
     - `new_value`
     - `metadata` (JSON for structured data)
     - `created_at`
2. **Service Layer**:
   - `ComplaintHistoryService` with helpers like `recordStatusChange`, `recordFieldUpdate`, etc.
   - Integrate with `ComplaintService` after every mutation (update, respond, attachment operations).
3. **API**:
   - `GET /api/v1/complaints/{id}/history` – Returns paginated list (secure: citizen sees own, employees their agency, admin all).
4. **Serialization**:
   - Multi-language descriptions? Start with Arabic summary in `action_description` generated from template.
5. **UI Use**:
   - Timeline view showing event icon, actor, timestamp, details (old/new status, comment text, etc.).

## Testing
- Unit tests verifying history entries for each action.
- Integration test verifying audit entries appear after update/respond endpoints.

## Migration Considerations
- Backfill existing complaints with single “CREATED (Legacy import)” entry during migration.


