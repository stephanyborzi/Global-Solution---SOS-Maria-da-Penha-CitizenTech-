package com.citizentech.sos.controller;

import com.citizentech.sos.dto.AuthDTO.*;
import com.citizentech.sos.dto.UserDTO.*;
import com.citizentech.sos.service.AuthService;
import com.citizentech.sos.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody UserCreateRequest request) {

        UserResponse created = userService.create(request);

        return ResponseEntity.created(
                URI.create("/users/" + created.id())
        ).body(created);
    }

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request) {

        return authService.login(request);
    }
}