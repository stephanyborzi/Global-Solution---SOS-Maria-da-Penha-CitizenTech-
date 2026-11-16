package com.citizentech.sos.dto;

import com.citizentech.sos.model.Sensor;
import com.citizentech.sos.model.SensorReading;
import com.citizentech.sos.model.SensorType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public final class SensorReadingDTO {
    private SensorReadingDTO() {}

    public record SensorRequest(
            @NotNull SensorType type,
            @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
            @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude) {}

    public record SensorResponse(Long id, SensorType type, double latitude, double longitude, boolean active) {
        public static SensorResponse from(Sensor s) {
            return new SensorResponse(s.getId(), s.getType(), s.getLatitude(), s.getLongitude(), s.isActive());
        }
    }

    public record ReadingRequest(@NotNull Double value) {}

    public record ReadingResponse(Long id, Long sensorId, double value, LocalDateTime createdAt, boolean alert) {
        public static ReadingResponse from(SensorReading r, boolean alert) {
            return new ReadingResponse(r.getId(), r.getSensor().getId(), r.getValue(), r.getCreatedAt(), alert);
        }
    }
}
