package com.Shakwa.user.config;

/**
 * Constants for system roles to avoid hardcoding role names
 */
public final class RoleConstants {
    
    public static final String PLATFORM_ADMIN = "PLATFORM_ADMIN";
    public static final String SUPERVISOR = "SUPERVISOR";
    public static final String VIEWER = "VIEWER";
    public static final String CITIZEN = "CITIZEN";
    
    private RoleConstants() {
        // Utility class, prevent instantiation
    }
} 