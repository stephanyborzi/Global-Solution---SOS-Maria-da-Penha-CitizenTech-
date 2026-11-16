package com.citizentech.sos.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "sos_alerts")
@Data
public class SosAlert {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "user_id") private User user;
    @Column(nullable = false) private double latitude;
    @Column(nullable = false) private double longitude;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private AlertStatus status;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;

    protected SosAlert() {}
    public SosAlert(User user, double latitude, double longitude) {
        this.user = user; this.latitude = latitude; this.longitude = longitude;
        this.status = AlertStatus.OPEN; this.createdAt = LocalDateTime.now();
    }
}
