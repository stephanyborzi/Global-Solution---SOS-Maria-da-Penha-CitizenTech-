package com.citizentech.sos.controller;

import com.citizentech.sos.dto.LocationDTO.*;
import com.citizentech.sos.security.AuthUser;
import com.citizentech.sos.service.DangerousLocationService;
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
@RequestMapping("/dangerous-locations")
public class DangerousLocationController {

    private final DangerousLocationService service;

    public DangerousLocationController(DangerousLocationService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<LocationResponse> create(
            @Valid @RequestBody LocationCreateRequest request,
            @AuthenticationPrincipal AuthUser auth) {

        LocationResponse created = service.create(request, auth);

        return ResponseEntity.created(
                URI.create("/dangerous-locations/" + created.id())
        ).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CITIZEN')")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public List<LocationResponse> list() {
        return service.listActive();
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