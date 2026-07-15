package com.society.management.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "buildings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Building extends BaseEntity {
    @Column(nullable = false, length = 150)
    private String name;
    @Column(length = 50)
    private String wing;
    @Column(length = 500)
    private String address;
    @Column(name = "total_floors")
    private Integer totalFloors;
}
