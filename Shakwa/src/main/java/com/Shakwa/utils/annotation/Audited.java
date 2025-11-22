package com.Shakwa.utils.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that should be automatically audited
 * Used by AuditAspect to record audit events
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {
    
    /**
     * Action name for the audit event (e.g., "CREATE_COMPLAINT", "UPDATE_USER")
     */
    String action();
    
    /**
     * Target type for the audit event (e.g., "COMPLAINT", "USER")
     */
    String targetType() default "";
    
    /**
     * Whether to include method arguments in audit details
     */
    boolean includeArgs() default false;
}

