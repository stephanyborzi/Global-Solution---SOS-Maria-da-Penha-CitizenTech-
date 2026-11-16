package com.citizentech.sos.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "contacts")
@Data
public class Contact {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "user_id") private User user;
    @Column(nullable = false, length = 600)  private String name;
    @Column(nullable = false) private String phone;

    protected Contact() {}
    public Contact(User user, String name, String phone) {
        this.user = user; this.name = name; this.phone = phone;
    }
}
