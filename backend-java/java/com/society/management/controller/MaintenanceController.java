package com.society.management.controller;

import com.society.management.dto.request.MaintenanceGenerationRequest;
import com.society.management.entity.MaintenanceBill;
import com.society.management.entity.MaintenanceStatus;
import com.society.management.repository.MaintenanceBillRepository;
import com.society.management.service.MaintenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceBillRepository billRepo;
    private final MaintenanceService service;

    @GetMapping
    public Page<MaintenanceBill> list(@RequestParam(required = false) MaintenanceStatus status, Pageable pageable) {
        return status != null ? billRepo.findByStatus(status, pageable) : billRepo.findAll(pageable);
    }

    @GetMapping("/flat/{flatId}")
    public List<MaintenanceBill> byFlat(@PathVariable Long flatId) { return service.findByFlat(flatId); }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SOCIETY_ADMIN','ACCOUNTANT')")
    public Map<String, Object> generate(@Valid @RequestBody MaintenanceGenerationRequest req) {
        int n = service.generateMonthlyBills(req);
        return Map.of("created", n, "month", req.month(), "year", req.year());
    }
}
