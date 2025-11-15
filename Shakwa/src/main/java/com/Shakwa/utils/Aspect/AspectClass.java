package com.Shakwa.utils.Aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;

@Component
@Aspect
@EnableAspectJAutoProxy
public class AspectClass {

    private static final Logger logger = LoggerFactory.getLogger(AspectClass.class);

    @Before("" +
            "execution(* com.Teryaq.product.service.*.*(..))" +
            "|| execution(* com.Teryaq.product.controller.*.*(..))" +
            "|| execution(* com.Teryaq.user.service.*.*(..))" +
            "|| execution(* com.Teryaq.user.controller.*.*(..))" +
            "|| execution(* com.Teryaq.purchase.service.*.*(..))" +
            "|| execution(* com.Teryaq.purchase.controller.*.*(..))" +
            "|| execution(* com.Teryaq.sale.service.*.*(..))" +
            "|| execution(* com.Teryaq.sale.controller.*.*(..))" +
            "|| execution(* com.Teryaq.language.LanguageService.*(..))" +
            "|| execution(* com.Teryaq.language.LanguageController.*(..))"
    )
    public void logBeforeMethod(JoinPoint joinPoint) {
        logger.info("Method called : " + joinPoint.getSignature().toShortString());
    }

    @AfterReturning(
            pointcut = "" +
                    "execution(* com.Teryaq.product.service.*.*(..))" +
                    "|| execution(* com.Teryaq.product.controller.*.*(..))" +
                    "|| execution(* com.Teryaq.user.service.*.*(..))" +
                    "|| execution(* com.Teryaq.user.controller.*.*(..))" +
                    "|| execution(* com.Teryaq.purchase.service.*.*(..))" +
                    "|| execution(* com.Teryaq.purchase.controller.*.*(..))" +
                    "|| execution(* com.Teryaq.sale.service.*.*(..))" +
                    "|| execution(* com.Teryaq.sale.controller.*.*(..))" +
                    "|| execution(* com.Teryaq.language.LanguageService.*(..))" +
                    "|| execution(* com.Teryaq.language.LanguageController.*(..))",
            returning = "result"
    )
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().toShortString();
        if (result != null) {
            String sanitizedResult = sanitizeSensitiveData(result);
            logger.info("Method completed successfully: {} - Return value: {}", methodName, sanitizedResult);
        } else {
            logger.info("Method completed successfully: {} - No return value", methodName);
        }
    }

    /**
     * Sanitizes sensitive data before logging
     * @param obj The object to sanitize
     * @return Sanitized string representation
     */
    private String sanitizeSensitiveData(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        String resultString = obj.toString();
        
        // Hide JWT tokens, passwords, and other sensitive data
        if (resultString.contains("token") || resultString.contains("Token") || 
            resultString.contains("password") || resultString.contains("Password") ||
            resultString.contains("secret") || resultString.contains("Secret") ||
            resultString.contains("key") || resultString.contains("Key") ||
            resultString.contains("auth") || resultString.contains("Auth") ||
            resultString.contains("jwt") || resultString.contains("JWT")) {
            
            // Check if it's a simple string that contains sensitive info
            if (obj instanceof String) {
                String str = (String) obj;
                if (str.length() > 20) {
                    // For long strings (likely tokens), show only first and last few characters
                    return str.substring(0, 8) + "..." + str.substring(str.length() - 8) + " [SENSITIVE]";
                } else {
                    return "[SENSITIVE_DATA]";
                }
            }
            
            // For objects, show class name but mark as sensitive
            return obj.getClass().getSimpleName() + " [SENSITIVE_DATA]";
        }
        
        // For collections and arrays, limit the output size
        if (obj instanceof java.util.Collection) {
            java.util.Collection<?> collection = (java.util.Collection<?>) obj;
            if (collection.size() > 10) {
                return collection.getClass().getSimpleName() + " with " + collection.size() + " items [TRUNCATED]";
            }
        }
        
        if (obj.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(obj);
            if (length > 10) {
                return obj.getClass().getSimpleName() + " with " + length + " items [TRUNCATED]";
            }
        }
        
        return resultString;
    }

    @AfterThrowing("" +
            "execution(* com.Teryaq.product.service.*.*(..))" +
            "|| execution(* com.Teryaq.product.controller.*.*(..))" +
            "|| execution(* com.Teryaq.user.service.*.*(..))" +
            "|| execution(* com.Teryaq.user.controller.*.*(..))" +
            "|| execution(* com.Teryaq.purchase.service.*.*(..))" +
            "|| execution(* com.Teryaq.purchase.controller.*.*(..))" +
            "|| execution(* com.Teryaq.sale.service.*.*(..))" +
            "|| execution(* com.Teryaq.sale.controller.*.*(..))" +
            "|| execution(* com.Teryaq.language.LanguageService.*(..))" +
            "|| execution(* com.Teryaq.language.LanguageController.*(..))"
    )
    public void logAfterThrowing(JoinPoint joinPoint) {
        logger.error("Exception thrown in method: {}", joinPoint.getSignature().getName());
    }

    @Around("" +
            "execution(* com.Teryaq.product.service.*.*(..))" +
            "|| execution(* com.Teryaq.product.controller.*.*(..))" +
            "|| execution(* com.Teryaq.user.service.*.*(..))" +
            "|| execution(* com.Teryaq.user.controller.*.*(..))" +
            "|| execution(* com.Teryaq.purchase.service.*.*(..))" +
            "|| execution(* com.Teryaq.purchase.controller.*.*(..))" +
            "|| execution(* com.Teryaq.sale.service.*.*(..))" +
            "|| execution(* com.Teryaq.sale.controller.*.*(..))" +
            "|| execution(* com.Teryaq.language.LanguageService.*(..))" +
            "|| execution(* com.Teryaq.language.LanguageController.*(..))"
    )
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;
            logger.info("Execution time of " + joinPoint.getSignature().toShortString() + " : " + duration + "ms");
        }
    }
}

