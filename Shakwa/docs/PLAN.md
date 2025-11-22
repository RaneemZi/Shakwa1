# Shakwa Enhancement Plan

## Scope

Address the eight highest-priority gaps identified from the official specification (`md.md`) and the existing backend. The focus is on backend capabilities that unblock the mobile app and dashboards.

## Phases

| Phase | Focus | Features |
| --- | --- | --- |
| **Phase 0** | Planning & architecture | Document requirements, data models, interfaces (this document + feature specs below) |
| **Phase 1** | Critical backend gaps | 1) Attachments & tracking numbers, 2) Concurrency locking, 3) Versioning/history, 4) Auditing/tracing |
| **Phase 2** | Platform robustness | 5) Caching, 6) Security hardening |
| **Phase 3** | Observability & governance | 7) Layered AOP logging/metrics, 8) Remaining Phase‑1 backlog items (notification service, priority workflow, etc.) |

## Work Streams

1. **Data & Storage** – Attachment storage, history tables, audit tables.
2. **API & Services** – New endpoints, locking workflow, caching configuration.
3. **Security & Compliance** – Rate limiting, account lockout, audit policies.
4. **Observability** – AOP aspects, metrics, tracing.

## Dependencies

- **Attachments** must land before mobile submission flow can ship.
- **Concurrency & versioning** share database migrations; implement together.
- **Audit trail** feeds the tracing dashboards and export/report requirements.

## Deliverables

For each feature: architecture notes, database migrations, service contracts, acceptance tests, and ops checklist (backup/monitoring implications).

## Tracking

- Use GitHub issues (or team tracker) with labels `phase-1`, `phase-2`, etc.
- Definition of Done for each feature: docs in `docs/` + code + tests + manual verification steps.


