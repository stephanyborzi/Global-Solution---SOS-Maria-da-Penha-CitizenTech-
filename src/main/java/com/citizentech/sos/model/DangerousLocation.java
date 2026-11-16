package com.citizentech.sos.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "dangerous_locations")
@Data
public class DangerousLocation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "user_id") private User user;
    @Column(nullable = false) private String type;
    @Column(nullable = false) private String description;
    @Column(nullable = false) private double latitude;
    @Column(nullable = false) private double longitude;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(nullable = false) private boolean active = true;

    protected DangerousLocation() {}
    public DangerousLocation(User user, String type, String description, double latitude, double longitude) {
        this.user = user; this.type = type; this.description = description;
        this.latitude = latitude; this.longitude = longitude; this.createdAt = LocalDateTime.now();
    }
}
