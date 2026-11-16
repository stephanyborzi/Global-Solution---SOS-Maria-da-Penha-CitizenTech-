package com.citizentech.sos.dto;

import com.citizentech.sos.model.AlertStatus;
import com.citizentech.sos.model.SosAlert;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public final class SosAlertDTO {
    private SosAlertDTO() {}

    public record SosAlertCreateRequest(
            @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
            @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude) {}

    public record SosAlertStatusRequest(@NotNull AlertStatus status) {}

    public record SosAlertResponse(Long id, Long userId, double latitude, double longitude,
                                   AlertStatus status, LocalDateTime createdAt) {
        public static SosAlertResponse from(SosAlert a) {
            return new SosAlertResponse(a.getId(), a.getUser().getId(), a.getLatitude(),
                    a.getLongitude(), a.getStatus(), a.getCreatedAt());
        }
    }
}
