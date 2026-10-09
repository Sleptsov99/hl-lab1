package com.example.spamer.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "services")
public class ServiceEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 128)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal pricePerMessage;

    // Column is is_active; the field name "active" would map to "active".
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
