package com.society.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "audit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100) private String action;
    @Column(name = "entity_type", length = 80) private String entityType;
    @Column(name = "entity_id", length = 80) private String entityId;
    @Column(columnDefinition = "TEXT") private String details;
    @Column(name = "ip_address", length = 45) private String ipAddress;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist void pre() { createdAt = OffsetDateTime.now(); }
}
