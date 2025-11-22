package com.Shakwa.config;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.Shakwa.complaint.storage.ComplaintStorageProperties;
import com.Shakwa.user.entity.BaseUser;
import com.Shakwa.user.entity.Citizen;
import com.Shakwa.user.repository.CitizenRepo;
import com.Shakwa.user.repository.EmployeeRepository;
import com.Shakwa.user.repository.UserRepository;
import com.Shakwa.utils.auditing.ApplicationAuditingAware;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableConfigurationProperties(ComplaintStorageProperties.class)
public class ApplicationConfig {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final CitizenRepo citizenRepo;

    @Autowired
    public ApplicationConfig(@Lazy UserRepository userRepository, @Lazy EmployeeRepository employeeRepository, @Lazy CitizenRepo citizenRepo) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.citizenRepo = citizenRepo;
    }

    @Bean
    public UserDetailsService userDetailsService(){
        return email -> {
          BaseUser user =  userRepository.findByEmail(email).orElse(null);
            if(user == null) {
               user = employeeRepository.findByEmail(email).orElse(null);
               if(user == null) {
                   Citizen citizen = citizenRepo.findByEmail(email).orElse(null);
                   if(citizen != null) {
                       user = citizen;
                   } else {
                       throw new UsernameNotFoundException("User, Employee or Citizen not found");
                   }
               }
            }
            Logger logger = Logger.getLogger(ApplicationConfig.class.getName());
            logger.info("User Email is: " + user.getEmail());
            return user;
        };
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Bean
    public AuditorAware<Long> auditorAware(){
        return new ApplicationAuditingAware();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authProvider =  new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
