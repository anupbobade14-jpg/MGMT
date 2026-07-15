package com.society.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "expenses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Expense {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ExpenseCategory category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(length = 150) private String vendor;
    @Column(length = 500) private String description;
    @Column(name = "invoice_no", length = 120) private String invoiceNo;
    @Column(name = "invoice_file_path", length = 500) private String invoiceFilePath;
    @Column(name = "payment_mode", length = 30) private String paymentMode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist void pre() { this.createdAt = OffsetDateTime.now(); }
}
