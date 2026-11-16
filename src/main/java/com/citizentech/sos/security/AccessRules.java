package com.citizentech.sos.security;

import org.springframework.security.access.AccessDeniedException;

import java.util.Objects;

public final class AccessRules {
    private AccessRules() {}

    public static void requireSelfOrAdmin(AuthUser auth, Long ownerId) {
        if (!auth.isAdmin() && !Objects.equals(auth.id(), ownerId)) {
            throw new AccessDeniedException("Access denied");
        }
    }

    public static void requireSelf(AuthUser auth, Long ownerId) {
        if (!Objects.equals(auth.id(), ownerId)) {
            throw new AccessDeniedException("Access denied");
        }
    }
}
