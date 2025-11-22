# Feature 01 – Citizen Attachments & Tracking Number

## Goal
Allow citizens to submit multiple attachments (images, PDFs) along with a complaint, store the binary files securely, and generate a human-friendly tracking number for follow-up.

## Requirements
- Upload 1–10 files (PNG, JPG, PDF) per complaint during submission and later updates.
- Maximum size per file: 10 MB; total per complaint: 50 MB (configurable).
- Virus/malware scanning hook (pluggable, start with stub).
- Generate unique tracking number `SHK-<YYYYMMDD>-<6 alphanumeric>` persisted with each complaint.
- Return tracking number in submission response + notification email.
- Allow staff to download attachments respecting authorization (citizen own complaint, staff same agency, admins).

## Design
1. **Storage**: Local filesystem first (`/data/complaints/<complaintId>/<uuid>.<ext>`), abstracted via `AttachmentStorageService` to swap S3/Azure later.
2. **DB Changes**:
   - `complaints.tracking_number` (unique, indexed).
   - Replace `attachments` string list with `complaint_attachments` table storing metadata: file name, storage key, size, mime type, checksum.
3. **API**:
   - `POST /api/v1/complaints` – Accept multipart (JSON + files) or two-step (create, then upload). Choose multipart + fallback `POST /complaints/{id}/attachments`.
   - `GET /api/v1/complaints/{id}/attachments/{attachmentId}` – Streaming download.
   - `DELETE /api/v1/complaints/{id}/attachments/{attachmentId}` – Remove file + metadata (staff/admin only).
4. **Tracking number generator**: Spring component using date prefix + base36 random, ensuring uniqueness via DB constraint (retry on conflict).
5. **Validation**: File type detection (Tika), size check, number of files, duplicate detection by checksum.
6. **Testing**:
   - Unit tests for generator and storage service.
   - Integration tests for upload/download authorization paths.

## Step-by-Step
1. Create DB migration for `tracking_number` + attachment metadata table.
2. Implement `AttachmentStorageService` interface and `LocalAttachmentStorage`.
3. Update DTOs to handle multipart uploads (Spring `@ModelAttribute`) while keeping JSON API backward compatible.
4. Extend `ComplaintMapper` to map attachments metadata.
5. Add controller endpoints + service methods for upload/download/delete.
6. Emit tracking number in responses and logs.
7. Document manual test matrix (multi-file, invalid file, unauthorized download, cleanup on complaint deletion).

## Open Questions / TODO
- Decide on antivirus integration approach (call external service vs stub).
- Align with mobile/frontend on multipart format.
- Determine retention/cleanup policy for orphaned files (scheduled job).

## Implementation Status (✅ Done)
- Storage abstractions: `AttachmentStorageService`, `LocalAttachmentStorageService`, configurable root path (`storage.complaints.root`).
- Domain changes: `ComplaintAttachment` entity, `Complaint.trackingNumber`, mapper output with download URLs.
- Service logic: `ComplaintService` generates unique tracking numbers, validates uploads, stores metadata, serves downloads, enforces authorization.
- API:
  - `POST /api/v1/complaints` (multipart `data` + optional `files`)
  - `POST /api/v1/complaints/{id}/attachments`
  - `GET /api/v1/complaints/{id}/attachments/{attachmentId}`
  - `DELETE /api/v1/complaints/{id}/attachments/{attachmentId}`
- DTO updates: `ComplaintDTOResponse.attachments` now exposes metadata; `ComplaintDTORequest` no longer embeds file paths.
- Client guidance: submit multipart form with JSON payload part named `data` and file part `files`.

