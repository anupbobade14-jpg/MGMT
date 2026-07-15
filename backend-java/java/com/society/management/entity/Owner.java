package com.society.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "owners")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Owner extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flat_id")
    private Flat flat;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(length = 150)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(name = "alternate_phone", length = 30)
    private String alternatePhone;

    @Column(name = "emergency_contact", length = 150)
    private String emergencyContact;

    @Column(name = "move_in_date")
    private LocalDate moveInDate;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private boolean primaryOwner = true;
}
