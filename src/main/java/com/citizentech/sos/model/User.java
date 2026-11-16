package com.citizentech.sos.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String name;
    @Column(nullable = false, unique = true) private String email;
    @Column(name = "password_hash", nullable = false) private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Profile profile;

    protected User() {}
    public User(String name, String email, String passwordHash, Profile profile) {
        this.name = name; this.email = email; this.passwordHash = passwordHash; this.profile = profile;
    }
}
