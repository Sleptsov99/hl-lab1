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

@Entity
@Table(name = "spam_log")
public class SpamLogEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "victim_contact", nullable = false, length = 256)
    private String victimContact;

    @Column(name = "message_body", columnDefinition = "text")
    private String messageBody;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private SpamStatus status;

    @Column(name = "sent_at")
    private Instant sentAt;

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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getVictimContact() {
        return victimContact;
    }

    public void setVictimContact(String victimContact) {
        this.victimContact = victimContact;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public SpamStatus getStatus() {
        return status;
    }

    public void setStatus(SpamStatus status) {
        this.status = status;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public void setSentAt(Instant sentAt) {
        this.sentAt = sentAt;
    }

    public Set<ProxyEntity> getProxies() {
        return proxies;
    }

    public void setProxies(Set<ProxyEntity> proxies) {
        this.proxies = proxies;
    }
}
