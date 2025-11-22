# Feature 08 – Phase 1 Priority Items Checklist

This document tracks the implementation readiness for the top-priority backlog items. Each row links to the detailed design doc above.

| # | Feature | Status | Next Actions |
| --- | --- | --- | --- |
| 1 | [Attachments & Tracking](Feature01_Attachments_and_Tracking.md) | 🟢 Done | Backend endpoints live (`POST /complaints` multipart, attachment CRUD); update client integrations. |
| 2 | [Concurrency Locking](Feature02_Concurrency_Locking.md) | 🟡 Planned | Define lock timeout policy with business; create lock service skeleton. |
| 3 | [Complaint Versioning](Feature03_Complaint_Versioning.md) | 🟢 Done | Backend endpoints live (`GET /complaints/{id}/history`); history recorded for all mutations. |
| 4 | [Tracing & Auditing](Feature04_Tracing_and_Auditing.md) | 🟡 Planned | Choose storage size/retention; wire into auth filter first. |
| 5 | Notifications (email/push) | 🔴 Pending design | Will be covered in future doc once attachment flow validated. |
| 6 | Priority workflow / SLA | 🔴 Pending | Requires stakeholder agreement on SLA durations. |

Legend: 🟢 Implemented, 🟡 Planned/in-progress, 🔴 Not started.

## Phase 1 Exit Criteria
1. Citizens can upload/download attachments and see tracking numbers.
2. Employees experience deterministic locking behavior.
3. Complaint history API available for dashboards.
4. Audit log accessible and exportable.
5. Security hardening and caching baselines deployed.

## Coordination
- Weekly sync with mobile/dashboard teams to demo progress.
- Ops review before enabling Redis and new security features.


