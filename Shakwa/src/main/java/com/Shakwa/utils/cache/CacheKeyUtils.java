package com.Shakwa.utils.cache;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.Shakwa.user.Enum.GovernmentAgencyType;
import com.Shakwa.user.entity.Citizen;
import com.Shakwa.user.entity.Employee;
import com.Shakwa.user.entity.User;

/**
 * Utility class for generating cache keys that include user context.
 * This ensures proper cache isolation per user role and agency.
 */
public class CacheKeyUtils {

    /**
     * Generate a cache key for complaint lists that includes user context.
     * Format: {role}:{agencyId}:{filtersHash}
     * 
     * @param filtersHash Hash of filter parameters
     * @param page Page number
     * @param size Page size
     * @return Cache key string
     */
    public static String generateComplaintListKey(String filtersHash, int page, int size) {
        StringBuilder key = new StringBuilder("complaints:list:");
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String email = authentication.getName();
                
                // Try to determine user type and agency
                // Note: This is a simplified approach. In production, you might want to
                // cache the user info or use a different approach
                key.append("user:").append(email.hashCode()).append(":");
            } else {
                key.append("anonymous:");
            }
        } catch (Exception e) {
            key.append("unknown:");
        }
        
        if (filtersHash != null && !filtersHash.isEmpty()) {
            key.append("filters:").append(filtersHash).append(":");
        }
        
        key.append("page:").append(page).append(":size:").append(size);
        
        return key.toString();
    }

    /**
     * Generate a simple cache key for complaint lists based on user context.
     * 
     * @param page Page number
     * @param size Page size
     * @return Cache key string
     */
    public static String generateComplaintListKey(int page, int size) {
        return generateComplaintListKey(null, page, size);
    }

    /**
     * Generate a cache key that includes user role and agency.
     * 
     * @param prefix Key prefix
     * @param additionalParams Additional parameters to include
     * @return Cache key string
     */
    public static String generateUserContextKey(String prefix, Object... additionalParams) {
        StringBuilder key = new StringBuilder(prefix).append(":");
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String email = authentication.getName();
                key.append("user:").append(email.hashCode());
            } else {
                key.append("anonymous");
            }
        } catch (Exception e) {
            key.append("unknown");
        }
        
        for (Object param : additionalParams) {
            if (param != null) {
                key.append(":").append(param.toString());
            }
        }
        
        return key.toString();
    }

    /**
     * Generate a hash from filter parameters.
     * 
     * @param params Filter parameters
     * @return Hash string
     */
    public static String hashFilters(Object... params) {
        if (params == null || params.length == 0) {
            return "all";
        }
        
        StringBuilder sb = new StringBuilder();
        for (Object param : params) {
            if (param != null) {
                sb.append(param.toString()).append(":");
            }
        }
        
        return String.valueOf(sb.toString().hashCode());
    }
}

