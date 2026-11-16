
package com.citizentech.sos.controller;

import com.citizentech.sos.dto.SensorReadingDTO.*;
import com.citizentech.sos.service.SensorService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sensors")
public class SensorController {

    private final SensorService service;

    public SensorController(SensorService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SensorResponse> create(
            @Valid @RequestBody SensorRequest request) {
        SensorResponse created = service.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CITIZEN')")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public List<SensorResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}/readings")
    @PreAuthorize("hasAnyRole('ADMIN', 'CITIZEN')")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public List<ReadingResponse> listReadings(
            @PathVariable Long id
    ) {
        return service.listReadings(id);
    }

    @PostMapping("/{id}/readings")
    @PreAuthorize("hasAnyRole('ADMIN', 'SENSOR')")
    @Operation(security = {
            @SecurityRequirement(name = "bearerAuth"),
            @SecurityRequirement(name = "iotApiKey")
    })
    public ResponseEntity<ReadingResponse> registerReading(
            @PathVariable Long id,
            @Valid @RequestBody ReadingRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.registerReading(id, request));
    }
}
