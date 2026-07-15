package com.society.management.controller;

import com.society.management.entity.MaintenanceStatus;
import com.society.management.entity.Owner;
import com.society.management.entity.MaintenanceBill;
import com.society.management.repository.MaintenanceBillRepository;
import com.society.management.repository.OwnerRepository;
import com.society.management.repository.ExpenseRepository;
import com.society.management.repository.PaymentRepository;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','SOCIETY_ADMIN','ACCOUNTANT','COMMITTEE')")
public class ReportController {

    private final MaintenanceBillRepository billRepo;
    private final OwnerRepository ownerRepo;
    private final ExpenseRepository expenseRepo;
    private final PaymentRepository paymentRepo;

    /** Owner ledger: full maintenance history for a given owner. */
    @GetMapping("/owner-ledger/{ownerId}")
    public Map<String, Object> ownerLedger(@PathVariable Long ownerId) {
        Owner o = ownerRepo.findById(ownerId).orElseThrow();
        List<MaintenanceBill> bills = billRepo.findByFlatId(o.getFlat().getId());
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("owner", Map.of("id", o.getId(), "name", o.getFullName(), "flat", o.getFlat().getFlatNumber()));
        res.put("bills", bills);
        return res;
    }

    @GetMapping("/pending")
    public List<MaintenanceBill> pending() {
        return billRepo.findByStatusInAndDueDateBefore(
                List.of(MaintenanceStatus.PENDING, MaintenanceStatus.OVERDUE), LocalDate.now().plusYears(1));
    }

    @GetMapping("/collection")
    public List<MaintenanceBill> collection() {
        return billRepo.findAll().stream()
                .filter(b -> b.getStatus() == MaintenanceStatus.APPROVED).toList();
    }

    @GetMapping(value = "/collection.csv", produces = "text/csv")
    public ResponseEntity<byte[]> collectionCsv() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CSVWriter w = new CSVWriter(new OutputStreamWriter(out))) {
            w.writeNext(new String[]{"Flat", "Owner", "Month", "Year", "Amount", "Status"});
            for (MaintenanceBill b : billRepo.findAll()) {
                w.writeNext(new String[]{
                        b.getFlat().getFlatNumber(),
                        b.getOwner() == null ? "" : b.getOwner().getFullName(),
                        String.valueOf(b.getBillMonth()),
                        String.valueOf(b.getBillYear()),
                        b.getTotalAmount().toString(),
                        b.getStatus().name()});
            }
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"collection.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(out.toByteArray());
    }
}
