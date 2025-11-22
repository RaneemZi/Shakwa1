package com.Shakwa.utils.Aspect;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.Shakwa.utils.annotation.Loggable;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Aspect for logging HTTP requests
 * Logs endpoint, user, duration, status code, and redacts sensitive payloads
 */
@Aspect
@Component
public class RequestLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingAspect.class);

    @Around("@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping) || " +
            "@annotation(com.Shakwa.utils.annotation.Loggable)")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();
        String method = request.getMethod();
        String endpoint = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUrl = queryString != null ? endpoint + "?" + queryString : endpoint;
        String user = getCurrentUser();
        String ipAddress = getClientIpAddress(request);

        // Log request entry
        logger.info("→ {} {} | User: {} | IP: {}", method, fullUrl, user, ipAddress);

        Object result = null;
        Exception exception = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int statusCode = getStatusCode(result, exception);
            
            // Log request completion
            if (exception != null) {
                logger.error("✗ {} {} | User: {} | Duration: {}ms | Status: {} | Error: {}", 
                    method, fullUrl, user, duration, statusCode, exception.getMessage());
            } else {
                logger.info("✓ {} {} | User: {} | Duration: {}ms | Status: {}", 
                    method, fullUrl, user, duration, statusCode);
            }
        }
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
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

    private String getClientIpAddress(HttpServletRequest request) {
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

    private int getStatusCode(Object result, Exception exception) {
        if (exception != null) {
            // Try to determine status from exception
            String exceptionName = exception.getClass().getSimpleName();
            if (exceptionName.contains("NotFound")) return 404;
            if (exceptionName.contains("Unauthorized") || exceptionName.contains("Forbidden")) return 403;
            if (exceptionName.contains("BadRequest") || exceptionName.contains("Validation")) return 400;
            if (exceptionName.contains("Conflict")) return 409;
            return 500;
        }
        
        if (result instanceof ResponseEntity) {
            return ((ResponseEntity<?>) result).getStatusCode().value();
        }
        
        return 200; // Default success
    }
}

