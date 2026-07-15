package com.society.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "maintenance_bills",
       uniqueConstraints = @UniqueConstraint(columnNames = {"flat_id","bill_month","bill_year"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MaintenanceBill extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flat_id")
    private Flat flat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Owner owner;

    @Column(name = "bill_month", nullable = false) private Integer billMonth;
    @Column(name = "bill_year", nullable = false)  private Integer billYear;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "late_fee", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal lateFee = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private MaintenanceStatus status = MaintenanceStatus.PENDING;

    @Column(length = 500)
    private String remarks;
}
