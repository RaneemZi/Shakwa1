package com.Shakwa.user.config;

/**
 * Constants for system roles to avoid hardcoding role names
 */
public final class RoleConstants {
    
    public static final String PLATFORM_ADMIN = "PLATFORM_ADMIN";
    public static final String PHARMACY_MANAGER = "PHARMACY_MANAGER";
    public static final String PHARMACY_EMPLOYEE = "PHARMACY_EMPLOYEE";
    public static final String PHARMACY_TRAINEE = "PHARMACY_TRAINEE";
//    public static final String SALES_AGENT = "SALES_AGENT";
//    public static final String SUPPORT_AGENT = "SUPPORT_AGENT";
    
    private RoleConstants() {
        // Utility class, prevent instantiation
    }
} 