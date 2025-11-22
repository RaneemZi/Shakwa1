package com.Shakwa.utils.Aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.Shakwa.utils.annotation.Measured;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.lang.reflect.Method;

/**
 * Aspect for recording performance metrics using Micrometer
 * Records execution time for service methods and emits metrics: shakwa.service.<method>.duration
 */
@Aspect
@Component
public class PerformanceMetricsAspect {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMetricsAspect.class);
    private final MeterRegistry meterRegistry;

    public PerformanceMetricsAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Around("execution(* com.Shakwa.complaint.service.*.*(..)) || " +
            "execution(* com.Shakwa.user.service.*.*(..)) || " +
            "@annotation(com.Shakwa.utils.annotation.Measured)")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String metricName = "shakwa.service." + className.toLowerCase() + "." + methodName + ".duration";
        
        // Check for custom metric name from @Measured annotation
        Measured measured = null;
        if (joinPoint.getSignature() instanceof MethodSignature methodSignature) {
            Method method = methodSignature.getMethod();
            measured = method.getAnnotation(Measured.class);
            if (measured == null) {
                measured = method.getDeclaringClass().getAnnotation(Measured.class);
            }
        }
        if (measured != null && !measured.name().isEmpty()) {
            metricName = "shakwa.service." + measured.name() + ".duration";
        }

        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Object result = joinPoint.proceed();
            return result;
        } finally {
            sample.stop(Timer.builder(metricName)
                    .description("Execution time for " + className + "." + methodName)
                    .register(meterRegistry));
            
            logger.debug("Recorded metric: {} for method: {}.{}", metricName, className, methodName);
        }
    }
}

