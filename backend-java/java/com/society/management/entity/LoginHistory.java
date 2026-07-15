package com.society.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "login_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 150) private String email;
    @Column(name = "ip_address", length = 45) private String ipAddress;
    @Column(name = "user_agent", length = 500) private String userAgent;
    @Column(nullable = false) private boolean success;
    @Column(name = "failure_reason", length = 255) private String failureReason;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist void pre() { createdAt = OffsetDateTime.now(); }
}
