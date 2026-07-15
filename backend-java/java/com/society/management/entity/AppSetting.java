package com.society.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "app_settings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AppSetting {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "setting_key", nullable = false, unique = true, length = 80)
    private String settingKey;

    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String settingValue;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist @PreUpdate void pre() { updatedAt = OffsetDateTime.now(); }
}
