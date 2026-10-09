package com.example.spamer.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "spam_log")
public class SpamLogEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private UserEntity user;

    @Column(nullable = false, length = 256)
    private String victimContact;

    @Column(columnDefinition = "text")
    private String messageBody;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SpamStatus status;

    private Instant sentAt;

    // Implicit names would be spam_log_proxies and proxies_id.
    @ManyToMany
    @JoinTable(
            name = "spam_log_proxy",
            joinColumns = @JoinColumn(name = "spam_log_id"),
            inverseJoinColumns = @JoinColumn(name = "proxy_id"))
    private Set<ProxyEntity> proxies = new HashSet<>();

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
