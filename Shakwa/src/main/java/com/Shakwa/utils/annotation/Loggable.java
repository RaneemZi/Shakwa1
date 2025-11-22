package com.Shakwa.utils.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that should have enhanced logging
 * Used by RequestLoggingAspect
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Loggable {
    
    /**
     * Whether to log method entry
     */
    boolean entry() default true;
    
    /**
     * Whether to log method exit
     */
    boolean exit() default true;
    
    /**
     * Whether to log method arguments
     */
    boolean args() default false;
    
    /**
     * Whether to log return value
     */
    boolean result() default false;
    
    /**
     * Log level (INFO, DEBUG, TRACE)
     */
    String level() default "INFO";
}

