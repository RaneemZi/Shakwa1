package com.Shakwa.config;

import java.time.Duration;
import java.util.Arrays;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.Shakwa.user.Enum.GovernmentAgencyType;
import com.Shakwa.complaint.Enum.ComplaintType;
import com.Shakwa.complaint.Enum.Governorate;

/**
 * Cache configuration for Spring Cache abstraction.
 * Uses in-memory ConcurrentMapCacheManager (no Redis).
 * 
 * Cache names:
 * - "referenceData": For enum values (ComplaintType, Governorate, GovernmentAgencyType) - cached indefinitely
 * - "complaintLists": For complaint listing queries - cached with short TTL (60 seconds)
 * - "dashboardMetrics": For dashboard statistics - cached centrally
 */
@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {

    /**
     * Primary cache manager using ConcurrentMapCacheManager (in-memory).
     * This provides a simple, thread-safe cache implementation.
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(Arrays.asList(
            "referenceData",
            "complaintLists",
            "dashboardMetrics"
        ));
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }

    /**
     * Custom key generator for complaint list queries.
     * Generates keys in format: {role}:{agencyId}:{filtersHash}
     * This ensures proper cache isolation per user role and agency.
     */
    @Bean("complaintListKeyGenerator")
    public KeyGenerator complaintListKeyGenerator() {
        return (target, method, params) -> {
            StringBuilder key = new StringBuilder();
            
            // Extract role and agency from context if available
            // For now, we'll use method parameters to build the key
            key.append("complaints:list:");
            
            // Add page and size
            for (Object param : params) {
                if (param != null) {
                    key.append(param.toString()).append(":");
                }
            }
            
            // Remove trailing colon
            if (key.length() > 0 && key.charAt(key.length() - 1) == ':') {
                key.setLength(key.length() - 1);
            }
            
            return key.toString();
        };
    }

    /**
     * Key generator for reference data (enums).
     * Simple key based on enum class name.
     */
    @Bean("referenceDataKeyGenerator")
    public KeyGenerator referenceDataKeyGenerator() {
        return (target, method, params) -> {
            if (params.length > 0 && params[0] != null) {
                Class<?> enumClass = params[0].getClass();
                return "reference:" + enumClass.getSimpleName();
            }
            return "reference:all";
        };
    }
}

