package com.Shakwa.utils.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that should have performance metrics recorded
 * Used by PerformanceMetricsAspect
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Measured {
    
    /**
     * Custom metric name (defaults to method name)
     */
    String name() default "";
    
    /**
     * Additional tags for the metric
     */
    String[] tags() default {};
}

