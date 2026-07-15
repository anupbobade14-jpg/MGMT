package com.society.management.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "complaints")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Complaint extends BaseEntity {

    @Column(name = "ticket_number", nullable = false, unique = true, length = 30)
    private String ticketNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raised_by_user_id")
    private User raisedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flat_id")
    private Flat flat;

    @Column(length = 60) private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @Column(nullable = false, length = 200) private String subject;
    @Column(columnDefinition = "TEXT")       private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ComplaintStatus status = ComplaintStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    private Integer rating;
    @Column(length = 500) private String feedback;

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;
}
