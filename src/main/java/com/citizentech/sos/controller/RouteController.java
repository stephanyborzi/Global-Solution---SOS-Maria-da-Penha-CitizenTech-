package com.citizentech.sos.controller;

import com.citizentech.sos.dto.RouteDTO.RouteResponse;
import com.citizentech.sos.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/safe-route")
public class RouteController {

    private final RouteService service;

    public RouteController(RouteService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CITIZEN')")
    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    public RouteResponse rate(
            @RequestParam @DecimalMin("-90") @DecimalMax("90") double originLat,
            @RequestParam @DecimalMin("-180") @DecimalMax("180") double originLng,
            @RequestParam @DecimalMin("-90") @DecimalMax("90") double destLat,
            @RequestParam @DecimalMin("-180") @DecimalMax("180") double destLng) {

        return service.rate(originLat, originLng, destLat, destLng);
    }
}