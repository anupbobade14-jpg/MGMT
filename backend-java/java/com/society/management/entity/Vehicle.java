package com.society.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "vehicles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Vehicle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private Owner owner;

    @Column(name = "vehicle_number", nullable = false, length = 30)
    private String vehicleNumber;
    @Column(name = "vehicle_type", length = 30) private String vehicleType;
    @Column(name = "make_model", length = 120) private String makeModel;
    @Column(name = "parking_slot", length = 30) private String parkingSlot;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist void pre() { this.createdAt = OffsetDateTime.now(); }
}
