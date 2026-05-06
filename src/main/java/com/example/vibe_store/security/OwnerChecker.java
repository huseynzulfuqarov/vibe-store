package com.example.vibe_store.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("ownerChecker")
public class OwnerChecker {

    public boolean isOwner(Integer employeeId, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return false;
        }

        if (!(authentication.getPrincipal() instanceof JwtPrincipal principal)) {
            return false;
        }

        if (principal.employeeId() == null) {
            return false;
        }

        return principal.employeeId().equals(employeeId);
    }

    public boolean isStoreManager(Integer storeId, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return false;
        }

        if (!(authentication.getPrincipal() instanceof JwtPrincipal principal)) {
            return false;
        }

        if (principal.storeId() == null) {
            return false;
        }

        return principal.storeId().equals(storeId);
    }
}