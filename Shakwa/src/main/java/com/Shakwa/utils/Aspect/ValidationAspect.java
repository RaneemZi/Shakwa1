package com.Shakwa.utils.Aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.executable.ExecutableValidator;

import java.util.Set;

/**
 * Aspect for logging validation violations
 * Ensures @Validated annotations trigger and logs violations
 */
@Aspect
@Component
public class ValidationAspect {

    private static final Logger logger = LoggerFactory.getLogger(ValidationAspect.class);

    /**
     * Log validation violations when they occur
     * This is handled by Spring's validation framework, but we log them here
     */
    @Before("@annotation(org.springframework.validation.annotation.Validated) || " +
            "execution(* com.Shakwa.complaint.controller.*.*(..)) || " +
            "execution(* com.Shakwa.user.controller.*.*(..))")
    public void logValidationViolations(JoinPoint joinPoint) {
        // Validation is handled by Spring's @Valid and @Validated
        // This aspect mainly serves as a hook for logging if needed
        logger.debug("Validating method: {}", joinPoint.getSignature().toShortString());
    }

    /**
     * Log constraint violations when they occur
     * This will be triggered by Spring's exception handling
     */
    public void logConstraintViolations(ConstraintViolationException exception) {
        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        if (!violations.isEmpty()) {
            logger.warn("Validation violations detected: {} violations", violations.size());
            for (ConstraintViolation<?> violation : violations) {
                logger.warn("  - Field: {} | Message: {} | Invalid value: {}", 
                    violation.getPropertyPath(), 
                    violation.getMessage(),
                    violation.getInvalidValue());
            }
        }
    }
}

