package com.citizentech.sos.controller;

import com.citizentech.sos.dto.ContactDTO.*;
import com.citizentech.sos.security.AuthUser;
import com.citizentech.sos.service.ContactService;
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
@RequestMapping("/contacts")
@PreAuthorize("hasRole('CITIZEN')")
public class ContactController {

    private final ContactService service;

    public ContactController(ContactService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ContactResponse> create(
            @Valid @RequestBody ContactCreateRequest request,
            @AuthenticationPrincipal AuthUser auth) {

        ContactResponse created = service.create(request, auth);

        return ResponseEntity.created(
                URI.create("/contacts/" + created.id())
        ).body(created);
    }

    @GetMapping
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public List<ContactResponse> listMine(
            @AuthenticationPrincipal AuthUser auth) {

        return service.listMine(auth);
    }

    @PutMapping("/{id}")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ContactResponse update(
            @PathVariable Long id,
            @Valid @RequestBody ContactUpdateRequest request,
            @AuthenticationPrincipal AuthUser auth) {

        return service.update(id, request, auth);
    }

    @DeleteMapping("/{id}")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> remove(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthUser auth) {

        service.remove(id, auth);

        return ResponseEntity.noContent().build();
    }
}