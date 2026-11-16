
package com.citizentech.sos.controller;

import com.citizentech.sos.dto.UserDTO.*;
import com.citizentech.sos.security.AuthUser;
import com.citizentech.sos.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public List<UserResponse> list() {
        return service.list();
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN','CITIZEN')")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public UserResponse me(@AuthenticationPrincipal AuthUser auth) {
        return service.getById(auth.id(), auth);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CITIZEN')")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public UserResponse getById(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthUser auth) {
        return service.getById(id, auth);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CITIZEN')")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public UserResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal AuthUser auth) {
        return service.update(id, request, auth);
    }

    @PatchMapping("/{id}/password")
    @PreAuthorize("hasAnyRole('ADMIN','CITIZEN')")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody UserPasswordRequest request,
            @AuthenticationPrincipal AuthUser auth) {
        service.changePassword(id, request, auth);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CITIZEN')")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> remove(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthUser auth) {
        service.remove(id, auth);
        return ResponseEntity.noContent().build();
    }
}
