package com.society.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "family_members")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FamilyMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private Owner owner;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;
    @Column(length = 50) private String relation;
    private Integer age;
    @Column(length = 30) private String phone;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist void pre() { this.createdAt = OffsetDateTime.now(); }
}
