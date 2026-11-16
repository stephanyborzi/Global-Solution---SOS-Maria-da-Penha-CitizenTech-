package com.citizentech.sos.security;

public record AuthUser(Long id, String email, String role) {
    public boolean isAdmin() { return "ADMIN".equals(role); }
}
