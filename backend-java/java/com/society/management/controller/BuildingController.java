package com.society.management.controller;

import com.society.management.dto.request.BuildingRequest;
import com.society.management.dto.request.FlatRequest;
import com.society.management.entity.Building;
import com.society.management.entity.Flat;
import com.society.management.exception.ApiException;
import com.society.management.repository.BuildingRepository;
import com.society.management.repository.FlatRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingRepository buildingRepo;
    private final FlatRepository flatRepo;

    @GetMapping
    public List<Building> list() { return buildingRepo.findAll(); }

    @PostMapping
    public Building create(@Valid @RequestBody BuildingRequest r) {
        return buildingRepo.save(Building.builder()
                .name(r.name()).wing(r.wing()).address(r.address())
                .totalFloors(r.totalFloors()).build());
    }

    @PutMapping("/{id}")
    public Building update(@PathVariable Long id, @Valid @RequestBody BuildingRequest r) {
        Building b = buildingRepo.findById(id).orElseThrow(() -> ApiException.notFound("Building not found"));
        b.setName(r.name()); b.setWing(r.wing()); b.setAddress(r.address()); b.setTotalFloors(r.totalFloors());
        return buildingRepo.save(b);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { buildingRepo.deleteById(id); }

    /* -- Flats under this building -- */
    @GetMapping("/{id}/flats")
    public List<Flat> flats(@PathVariable Long id) { return flatRepo.findByBuildingId(id); }

    @PostMapping("/flats")
    public Flat createFlat(@Valid @RequestBody FlatRequest r) {
        Building b = buildingRepo.findById(r.buildingId())
                .orElseThrow(() -> ApiException.notFound("Building not found"));
        return flatRepo.save(Flat.builder()
                .building(b).flatNumber(r.flatNumber()).floor(r.floor())
                .areaSqft(r.areaSqft()).bhk(r.bhk())
                .occupancy(r.occupancy() != null ? r.occupancy() : com.society.management.entity.OccupancyStatus.VACANT)
                .monthlyMaintenance(r.monthlyMaintenance() != null ? r.monthlyMaintenance() : BigDecimal.ZERO)
                .build());
    }
}
