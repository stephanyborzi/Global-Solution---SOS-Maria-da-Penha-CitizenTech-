package com.citizentech.sos.dto;

import com.citizentech.sos.model.DangerousLocation;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public final class LocationDTO {
    private LocationDTO() {}

    public record LocationCreateRequest(
            @NotBlank @Size(max = 30) String type,
            @NotBlank @Size(max = 300) String description,
            @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
            @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude) {}

    public record LocationResponse(Long id, Long userId, String type, String description,
                                   double latitude, double longitude, LocalDateTime createdAt) {
        public static LocationResponse from(DangerousLocation l) {
            return new LocationResponse(l.getId(), l.getUser().getId(), l.getType(), l.getDescription(),
                    l.getLatitude(), l.getLongitude(), l.getCreatedAt());
        }
    }
}
