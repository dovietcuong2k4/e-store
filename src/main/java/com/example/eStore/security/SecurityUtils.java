package com.example.eStore.security;

import com.example.eStore.dto.constants.Constants;
import com.example.eStore.exception.AppException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Resolves the current user id from the authenticated principal (JWT → {@link AuthenticatedUser}).
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AppException(
                    "Authentication required",
                    Constants.ErrorCode.Security.UNAUTHENTICATED);
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof AuthenticatedUser user) {
            return user.getId();
        }
        throw new AppException(
                "Invalid authentication principal",
                Constants.ErrorCode.Security.UNAUTHENTICATED);
    }

    public static Long getCurrentUserIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof AuthenticatedUser user) {
            return user.getId();
        }
        return null;
    }

    public static Set<String> getCurrentUserRoles() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AppException(
                    "Authentication required",
                    Constants.ErrorCode.Security.UNAUTHENTICATED);
        }

        return auth.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    public static boolean hasRole(String role) {
        return getCurrentUserRoles().contains(role);
    }
}
