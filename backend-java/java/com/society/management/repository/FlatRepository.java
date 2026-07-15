package com.society.management.repository;

import com.society.management.entity.Flat;
import com.society.management.entity.OccupancyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlatRepository extends JpaRepository<Flat, Long> {
    List<Flat> findByBuildingId(Long buildingId);
    long countByOccupancy(OccupancyStatus occupancy);
}
