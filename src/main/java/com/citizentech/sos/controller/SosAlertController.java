package com.citizentech.sos.controller;

import com.citizentech.sos.dto.SosAlertDTO.*;
import com.citizentech.sos.security.AuthUser;
import com.citizentech.sos.service.SosAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/sos-alerts")
public class SosAlertController {

    private final SosAlertService service;

    public SosAlertController(SosAlertService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SosAlertResponse> create(
            @Valid @RequestBody SosAlertCreateRequest request,
            @AuthenticationPrincipal AuthUser auth) {

        SosAlertResponse created = service.create(request, auth);

        return ResponseEntity.created(
                URI.create("/sos-alerts/" + created.id())
        ).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CITIZEN')")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public List<SosAlertResponse> list(
            @RequestParam(required = false) Long userId,
            @AuthenticationPrincipal AuthUser auth) {

        return service.list(auth, userId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CITIZEN')")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public SosAlertResponse getById(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthUser auth) {

        return service.getById(id, auth);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public SosAlertResponse updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody SosAlertStatusRequest request) {

        return service.updateStatus(id, request);
    }
}