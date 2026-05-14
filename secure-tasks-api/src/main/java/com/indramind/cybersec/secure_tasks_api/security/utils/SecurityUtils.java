package com.indramind.cybersec.secure_tasks_api.security.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.indramind.cybersec.secure_tasks_api.security.UserDetailsImpl;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static Long getLoggedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null &&
            authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
            return userDetails.getId();
        }

        return null;
    }
}