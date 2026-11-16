package com.citizentech.sos.dto;
import com.citizentech.sos.dto.UserDTO.UserResponse;
import jakarta.validation.constraints.*;

public final class AuthDTO {
    private AuthDTO() {}
    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {}
    public record LoginResponse(String token, String tokenType, long expiresInMs, UserResponse user) {}
}
