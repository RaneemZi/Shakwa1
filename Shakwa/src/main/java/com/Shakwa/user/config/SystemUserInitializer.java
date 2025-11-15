package com.Shakwa.user.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.Shakwa.user.Enum.UserStatus;
import com.Shakwa.user.entity.User;
import com.Shakwa.user.repository.RoleRepository;
import com.Shakwa.user.repository.UserRepository;
import com.Shakwa.user.config.RoleConstants;

@Component
@RequiredArgsConstructor
@Order(2) // Run after SystemRolesInitializer
public class SystemUserInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(SystemUserInitializer.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_ADMIN_PASSWORD = "Password!1";

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Initializing system users...");
        
        // Delete old admin users if they exist
        deleteOldAdminUsers();
        
        // Create new admin users (will check if they exist first)
        createSystemUsers();
        
        log.info("System users initialized successfully");
    }
    
    /**
     * Delete old admin users with old email addresses
     */
    private void deleteOldAdminUsers() {
        // Delete old admin emails
        userRepository.findByEmail("admin@teryaq.com").ifPresent(user -> {
            log.info("Deleting old admin user: admin@teryaq.com");
            userRepository.delete(user);
        });
        
        userRepository.findByEmail("super.admin@teryaq.com").ifPresent(user -> {
            log.info("Deleting old super admin user: super.admin@teryaq.com");
            userRepository.delete(user);
        });
        
        // Also delete old lowercase versions if they exist
        userRepository.findByEmail("admin@shakwa.com").ifPresent(user -> {
            log.info("Deleting old admin user: admin@shakwa.com");
            userRepository.delete(user);
        });
        
        userRepository.findByEmail("super.admin@shakwa.com").ifPresent(user -> {
            log.info("Deleting old super admin user: super.admin@shakwa.com");
            userRepository.delete(user);
        });
    }

    private void createSystemUsers() {
        // Create Super Admin user
        createSuperAdmin();
        
        // Create Platform Admin user
        createPlatformAdmin();
    }

    private void createSuperAdmin() {
        if (userRepository.findByEmail("super.admin@Shakwa.com").isPresent()) {
            log.info("Super admin user already exists");
            return;
        }

        var superAdminRole = roleRepository.findByName(RoleConstants.PLATFORM_ADMIN)
                .orElseThrow(() -> new RuntimeException("PLATFORM_ADMIN role not found"));

        User superAdmin = User.builder()
                .firstName("Wassem")
                .lastName("Tenbakji")
                .email("super.admin@Shakwa.com")
                .password(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                .role(superAdminRole)
                .status(UserStatus.ACTIVE)
                .position(
                        "SUPER_ADMIN"
                )
                .build();

        userRepository.save(superAdmin);
        log.info("Super admin user created:");
        log.info("Email: {}", superAdmin.getEmail());
        log.info("Password: {}", DEFAULT_ADMIN_PASSWORD);
        log.info("Role: {}", superAdmin.getRole());
    }

    private void createPlatformAdmin() {
        if (userRepository.findByEmail("admin@Shakwa.com").isPresent()) {
            log.info("Platform admin user already exists");
            return;
        }

        var platformAdminRole = roleRepository.findByName(RoleConstants.PLATFORM_ADMIN)
                .orElseThrow(() -> new RuntimeException("PLATFORM_ADMIN role not found"));

        User platformAdmin = User.builder()
                .firstName("platform")
                .lastName("admin")
                .email("admin@Shakwa.com")
                .password(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                .role(platformAdminRole)
                .position(
                        "PLATFORM_ADMIN"
                )
                .status(UserStatus.ACTIVE)
                .build();

        userRepository.save(platformAdmin);
        log.info("Platform admin user created:");
        log.info("Email: {}", platformAdmin.getEmail());
        log.info("Password: {}", DEFAULT_ADMIN_PASSWORD);
        log.info("Role: {}", platformAdmin.getRole());
    }
} 