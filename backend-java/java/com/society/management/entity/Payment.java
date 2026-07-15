package com.society.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maintenance_id")
    private MaintenanceBill maintenance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Owner owner;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false, length = 30)
    private PaymentMode paymentMode;

    @Column(name = "transaction_ref", length = 120)
    private String transactionRef;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "proof_file_path", length = 500) private String proofFilePath;
    @Column(name = "proof_content_type", length = 80) private String proofContentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.UNDER_VERIFICATION;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at") private OffsetDateTime reviewedAt;
    @Column(name = "review_notes", length = 500) private String reviewNotes;

    @Column(name = "receipt_number", unique = true, length = 50)
    private String receiptNumber;

    @Column(name = "receipt_file_path", length = 500)
    private String receiptFilePath;
}
