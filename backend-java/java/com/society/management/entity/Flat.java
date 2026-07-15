package com.society.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "flats",
       uniqueConstraints = @UniqueConstraint(columnNames = {"building_id", "flat_number"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Flat extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "building_id")
    private Building building;

    @Column(name = "flat_number", nullable = false, length = 30)
    private String flatNumber;

    private Integer floor;

    @Column(name = "area_sqft", precision = 10, scale = 2)
    private BigDecimal areaSqft;

    @Column(length = 10)
    private String bhk;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OccupancyStatus occupancy = OccupancyStatus.VACANT;

    @Column(name = "monthly_maintenance", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal monthlyMaintenance = BigDecimal.ZERO;
}
