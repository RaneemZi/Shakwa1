package com.Shakwa.utils.Aspect;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.Shakwa.utils.annotation.Audited;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Aspect for automatic audit event recording
 * Bridges Feature 04 by auto-recording audit events for methods annotated with @Audited
 * 
 * Note: This is a placeholder that logs audit events. 
 * When Feature 04 (AuditService) is implemented, this will be updated to call AuditService.record()
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);

    @AfterReturning(
            pointcut = "@annotation(audited)",
            returning = "result"
    )
    public void auditSuccess(JoinPoint joinPoint, Audited audited, Object result) {
        try {
            String action = audited.action();
            String targetType = audited.targetType().isEmpty() ? 
                inferTargetType(joinPoint) : audited.targetType();
            Long targetId = extractTargetId(result, joinPoint.getArgs());
            String status = "SUCCESS";
            Map<String, Object> details = buildDetails(joinPoint, result, audited.includeArgs());

            logAuditEvent(action, targetType, targetId, status, details);
            
            // TODO: When Feature 04 (AuditService) is implemented, call:
            // auditService.record(action, targetType, targetId, status, details);
            
        } catch (Exception e) {
            logger.error("Error recording audit event", e);
        }
    }

    @AfterThrowing(
            pointcut = "@annotation(audited)",
            throwing = "exception"
    )
    public void auditFailure(JoinPoint joinPoint, Audited audited, Throwable exception) {
        try {
            String action = audited.action();
            String targetType = audited.targetType().isEmpty() ? 
                inferTargetType(joinPoint) : audited.targetType();
            Long targetId = extractTargetId(null, joinPoint.getArgs());
            String status = "FAILURE";
            Map<String, Object> details = buildDetails(joinPoint, null, audited.includeArgs());
            details.put("error", exception.getClass().getSimpleName());
            details.put("errorMessage", exception.getMessage());

            logAuditEvent(action, targetType, targetId, status, details);
            
            // TODO: When Feature 04 (AuditService) is implemented, call:
            // auditService.record(action, targetType, targetId, status, details);
            
        } catch (Exception e) {
            logger.error("Error recording audit event", e);
        }
    }

    private void logAuditEvent(String action, String targetType, Long targetId, 
                              String status, Map<String, Object> details) {
        String user = getCurrentUser();
        String ipAddress = getClientIpAddress();
        
        logger.info("AUDIT | Action: {} | Target: {}[{}] | User: {} | IP: {} | Status: {} | Details: {}", 
            action, targetType, targetId, user, ipAddress, status, details);
    }

    private String inferTargetType(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        // Remove "Service" or "Controller" suffix
        if (className.endsWith("Service")) {
            return className.substring(0, className.length() - 7).toUpperCase();
        }
        if (className.endsWith("Controller")) {
            return className.substring(0, className.length() - 10).toUpperCase();
        }
        return className.toUpperCase();
    }

    private Long extractTargetId(Object result, Object[] args) {
        // Try to extract ID from result
        if (result != null) {
            try {
                java.lang.reflect.Method getIdMethod = result.getClass().getMethod("getId");
                Object id = getIdMethod.invoke(result);
                if (id instanceof Long) {
                    return (Long) id;
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        
        // Try to extract ID from first argument (usually the ID parameter)
        if (args != null && args.length > 0) {
            Object firstArg = args[0];
            if (firstArg instanceof Long) {
                return (Long) firstArg;
            }
        }
        
        return null;
    }

    private Map<String, Object> buildDetails(JoinPoint joinPoint, Object result, boolean includeArgs) {
        Map<String, Object> details = new HashMap<>();
        details.put("method", joinPoint.getSignature().toShortString());
        
        if (includeArgs && joinPoint.getArgs() != null) {
            // Sanitize arguments to avoid logging sensitive data
            Object[] sanitizedArgs = Arrays.stream(joinPoint.getArgs())
                .map(this::sanitizeSensitiveData)
                .toArray();
            details.put("arguments", sanitizedArgs);
        }
        
        if (result != null) {
            details.put("resultType", result.getClass().getSimpleName());
        }
        
        return details;
    }

    private Object sanitizeSensitiveData(Object obj) {
        if (obj == null) return null;
        
        String str = obj.toString().toLowerCase();
        if (str.contains("password") || str.contains("token") || 
            str.contains("secret") || str.contains("key") || 
            str.contains("jwt") || str.contains("auth")) {
            return "[SENSITIVE_DATA]";
        }
        
        return obj;
    }

    private String getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getPrincipal())) {
                return authentication.getName();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "anonymous";
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                String xRealIp = request.getHeader("X-Real-IP");
                if (xRealIp != null && !xRealIp.isEmpty()) {
                    return xRealIp;
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "unknown";
    }
}

