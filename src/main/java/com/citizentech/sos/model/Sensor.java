package com.citizentech.sos.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "sensors")
@Data
public class Sensor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private SensorType type;
    @Column(nullable = false) private double latitude;
    @Column(nullable = false) private double longitude;
    @Column(nullable = false) private boolean active = true;

    protected Sensor() {}
    public Sensor(SensorType type, double latitude, double longitude) {
        this.type = type; this.latitude = latitude; this.longitude = longitude;
    }
}
