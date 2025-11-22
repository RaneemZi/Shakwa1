# Feature 06 – Security Hardening (Access Control, Alerts, Brute-Force Protection)

## Objectives
Ensure strict access control, detect brute-force attempts, and alert users/admins when suspicious activity occurs.

## Enhancements

### 1. Access Control Review
- Audit all endpoints for proper annotations (`@PreAuthorize`) aligning with role/permission matrix.
- Centralize permission strings in `RoleConstants`.
- Add integration tests covering unauthorized access attempts.

### 2. Failed Login Tracking & Alerts
- Extend authentication flow to record failed attempts per username + IP.
- Store counters in Redis with TTL (e.g., sliding window 15 minutes).
- Thresholds:
  - 5 failed attempts → temporary account lock (15 minutes).
  - 10 failed attempts → require manual unlock (admin).
- Send email/SMS notification to user on lock.
- Admin dashboard widget listing locked accounts.

### 3. Brute-force Mitigation
- Introduce exponential backoff (wait time) after each failed attempt before next try allowed.
- Apply IP-based rate limiting (existing `RateLimiterConfig`) with stricter rules for auth endpoints.
- Optionally integrate CAPTCHA after threshold.

### 4. Secure Login Logging
- Record every login attempt (success/failure) through Audit service (Feature 04).
- Include IP, user agent, geolocation (if feasible).

### 5. Session Management
- Enforce refresh-token rotation (if applicable).
- Add logout endpoint that revokes token (store blacklist with expiry).

### 6. Secrets Handling
- Move JWT secret & SMTP credentials to environment variables / secret manager.
- Document rotation process.

### 7. Testing
- Unit tests for lock/unlock logic.
- Integration tests simulating repeated failures.
- Security regression tests (OWASP ZAP).

### 8. Ops Checklist
- Monitoring alerts when lock counts spike.
- Runbooks for unlocking accounts, handling suspected attacks.

