package com.Shakwa.utils.auditing;


import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.Shakwa.user.entity.User;

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
        User dealerPrincipal = (User) authentication.getPrincipal();
        return Optional.ofNullable(dealerPrincipal.getId());
    }
}
