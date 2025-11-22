package com.Shakwa.utils.auditing;


import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.Shakwa.user.entity.BaseUser;

import java.util.Optional;

public class ApplicationAuditingAware implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();
        if(authentication == null ||
        ! authentication.isAuthenticated() ||
         authentication instanceof AnonymousAuthenticationToken){
            return Optional.of(1L);
        }
        // Use BaseUser instead of User since both User and Citizen extend BaseUser
        BaseUser currentUser = (BaseUser) authentication.getPrincipal();
        return Optional.ofNullable(currentUser.getId());
    }
}
