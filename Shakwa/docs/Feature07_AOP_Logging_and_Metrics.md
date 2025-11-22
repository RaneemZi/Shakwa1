# Feature 07 – Layered Architecture & AOP Logging/Metrics

## Goal
Strengthen the layered architecture by isolating cross-cutting concerns (logging, metrics, validation) into reusable Aspects.

## Key Activities

### 1. Layering Verification
- Controllers → Services → Repositories pattern already present; document guidelines:
  - Controllers: request validation & response mapping.
  - Services: business logic, transactions.
  - Repositories: persistence only.
- Enforce via code reviews & optional ArchUnit tests.

### 2. Aspect Modules
1. **RequestLoggingAspect**
   - Logs endpoint, user, duration, status code.
   - Redact sensitive payloads.
2. **PerformanceMetricsAspect**
   - Wraps service methods and records execution time via Micrometer.
   - Emits metrics `shakwa.service.<method>.duration`.
3. **ValidationAspect**
   - Ensure `@Validated` annotations trigger, log violations.
4. **AuditAspect**
   - Bridges Feature 04 by auto-recording audit events for annotated methods (`@Audited(action="...")`).

### 3. Configuration
- Enable `@EnableAspectJAutoProxy`.
- Organize aspects under `com.Shakwa.utils.aspect`.

### 4. Observability Stack
- Publish metrics to Prometheus (Micrometer exporter).
- Set up dashboards (Grafana) showing request rates, latency, error ratios.

### 5. Testing
- Unit tests for aspects (use `@AspectJTest`).
- Performance tests ensuring overhead acceptable (<5%).

### 6. Documentation
- Describe how to annotate new methods for auditing/logging.
- Provide troubleshooting guide for noisy logs or metric spikes.

