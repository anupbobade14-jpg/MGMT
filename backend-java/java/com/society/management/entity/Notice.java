package com.society.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "notices")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false, length = 40)
    @Builder.Default
    private String category = "GENERAL";

    @Column(name = "is_pinned", nullable = false)
    @Builder.Default
    private boolean pinned = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by")
    private User publishedBy;

    @Column(name = "published_at", nullable = false)
    private OffsetDateTime publishedAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    @PrePersist void pre() { if (publishedAt == null) publishedAt = OffsetDateTime.now(); }
}
