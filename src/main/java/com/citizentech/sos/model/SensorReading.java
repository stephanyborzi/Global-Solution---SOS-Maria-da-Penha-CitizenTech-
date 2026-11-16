package com.citizentech.sos.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_readings")
@Data
public class SensorReading {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "sensor_id") private Sensor sensor;
    @Column(name = "reading_value", nullable = false) private double value;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;

    protected SensorReading() {}
    public SensorReading(Sensor sensor, double value) {
        this.sensor = sensor; this.value = value; this.createdAt = LocalDateTime.now();
    }
}
