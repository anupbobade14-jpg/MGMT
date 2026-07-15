package com.society.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "documents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Document {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 50)
    private String category;  // AGM, AUDIT, RULES, VENDOR, RECEIPT, OTHER

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "content_type", length = 80) private String contentType;
    @Column(name = "file_size") private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by")
    private User uploadedBy;

    @Column(name = "uploaded_at", updatable = false)
    private OffsetDateTime uploadedAt;

    @PrePersist void pre() { this.uploadedAt = OffsetDateTime.now(); }
}
