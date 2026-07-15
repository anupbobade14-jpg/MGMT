package com.society.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "visitors")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Visitor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "visitor_name", nullable = false, length = 150)
    private String visitorName;

    @Column(length = 30) private String phone;
    @Column(length = 200) private String purpose;
    @Column(name = "visit_type", length = 30) private String visitType;
    @Column(name = "vehicle_number", length = 30) private String vehicleNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flat_id")
    private Flat flat;

    @Column(name = "entry_time", nullable = false)
    private OffsetDateTime entryTime;

    @Column(name = "exit_time")
    private OffsetDateTime exitTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 30)
    @Builder.Default
    private VisitorApprovalStatus approvalStatus = VisitorApprovalStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by")
    private User addedBy;

    @Column(name = "photo_path", length = 500) private String photoPath;

    @PrePersist void pre() { if (entryTime == null) entryTime = OffsetDateTime.now(); }
}
