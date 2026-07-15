package com.society.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "other_incomes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OtherIncome {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private IncomeCategory category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "income_date", nullable = false)
    private LocalDate incomeDate;

    @Column(length = 500) private String description;
    @Column(name = "reference_no", length = 120) private String referenceNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist void pre() { this.createdAt = OffsetDateTime.now(); }
}
