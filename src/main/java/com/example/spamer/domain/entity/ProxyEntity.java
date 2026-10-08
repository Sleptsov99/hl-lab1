package com.example.spamer.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "proxy")
public class ProxyEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 128)
    private String host;

    @Column(nullable = false)
    private int port;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ProxyProtocol protocol;

    @Column(length = 64)
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ProxyStatus status;

    // nullable: a proxy may exist before it's attributed to a provider service
    @ManyToOne
    @JoinColumn(name = "provider_id")
    private ServiceEntity provider;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ProxyProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(ProxyProtocol protocol) {
        this.protocol = protocol;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public ProxyStatus getStatus() {
        return status;
    }

    public void setStatus(ProxyStatus status) {
        this.status = status;
    }

    public ServiceEntity getProvider() {
        return provider;
    }

    public void setProvider(ServiceEntity provider) {
        this.provider = provider;
    }
}
