package com.society.management.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "expense_categories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExpenseCategory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 80)
    private String name;
}
