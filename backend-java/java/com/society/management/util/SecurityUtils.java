package com.society.management.util;

import com.society.management.security.AppUserDetails;
import com.society.management.exception.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {}

    public static AppUserDetails currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AppUserDetails ud))
            throw ApiException.forbidden("Not authenticated");
        return ud;
    }

    public static Long currentUserId() { return currentUser().getUserId(); }

    public static boolean hasAnyRole(String... roles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        for (String r : roles) {
            String need = r.startsWith("ROLE_") ? r : "ROLE_" + r;
            if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(need))) return true;
        }
        return false;
    }
}
