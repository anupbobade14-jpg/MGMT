package com.society.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "email_templates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmailTemplate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 80) private String code;
    @Column(nullable = false, length = 200) private String subject;
    @Column(name = "body_html", nullable = false, columnDefinition = "TEXT")
    private String bodyHtml;
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist @PreUpdate void pre() { updatedAt = OffsetDateTime.now(); }
}
