package com.citizentech.sos.dto;

import com.citizentech.sos.model.Profile;
import com.citizentech.sos.model.User;
import jakarta.validation.constraints.*;

public final class UserDTO {
    private UserDTO() {}

    public record UserCreateRequest(
            @NotBlank String name,
            @NotBlank @Email String email,
            @NotBlank String password) {}

    public record UserUpdateRequest(
            @NotBlank String name,
            @NotBlank @Email String email) {}

    public record UserPasswordRequest(
            @NotBlank String currentPassword,
            @NotBlank String newPassword) {}

    public record UserResponse(Long id, String name, String email, Profile profile) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getProfile());
        }
    }
}
