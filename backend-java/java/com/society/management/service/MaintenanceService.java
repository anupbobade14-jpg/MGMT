package com.society.management.service;

import com.society.management.config.AppProperties;
import com.society.management.dto.request.MaintenanceGenerationRequest;
import com.society.management.entity.*;
import com.society.management.exception.ApiException;
import com.society.management.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceService {

    private final FlatRepository flatRepo;
    private final OwnerRepository ownerRepo;
    private final MaintenanceBillRepository billRepo;
    private final EmailService emailService;
    private final AppProperties props;

    @Transactional
    public int generateMonthlyBills(MaintenanceGenerationRequest req) {
        int month = req.month();
        int year = req.year();
        LocalDate dueDate = req.dueDate() != null
                ? LocalDate.parse(req.dueDate())
                : LocalDate.of(year, month, Math.min(props.getMaintenance().getDefaultDueDay(), 28));

        int created = 0;
        for (Flat flat : flatRepo.findAll()) {
            if (flat.getOccupancy() == OccupancyStatus.VACANT) continue;
            Optional<MaintenanceBill> exists = billRepo.findByFlatIdAndBillMonthAndBillYear(flat.getId(), month, year);
            if (exists.isPresent()) continue;

            BigDecimal amount = req.overrideAmount() != null
                    ? req.overrideAmount()
                    : (flat.getMonthlyMaintenance() != null && flat.getMonthlyMaintenance().signum() > 0
                        ? flat.getMonthlyMaintenance()
                        : new BigDecimal("2500"));

            Owner owner = ownerRepo.findFirstByFlatIdAndPrimaryOwnerTrue(flat.getId()).orElse(null);
            MaintenanceBill bill = MaintenanceBill.builder()
                    .flat(flat).owner(owner)
                    .billMonth(month).billYear(year)
                    .amount(amount).lateFee(BigDecimal.ZERO).totalAmount(amount)
                    .dueDate(dueDate).status(MaintenanceStatus.PENDING).build();
            bill = billRepo.save(bill);
            created++;

            if (owner != null && owner.getEmail() != null) {
                Map<String, String> vars = new HashMap<>();
                vars.put("ownerName", owner.getFullName());
                vars.put("month", java.time.Month.of(month).name());
                vars.put("year", String.valueOf(year));
                vars.put("amount", amount.toString());
                vars.put("dueDate", dueDate.toString());
                emailService.sendTemplate(owner.getEmail(), "MAINT_GENERATED", vars);
            }
        }
        log.info("Generated {} maintenance bills for {}/{}", created, month, year);
        return created;
    }

    public MaintenanceBill applyLateFeeIfOverdue(MaintenanceBill bill) {
        if (bill.getStatus() != MaintenanceStatus.PENDING) return bill;
        if (bill.getDueDate().isBefore(LocalDate.now()) && bill.getLateFee().signum() == 0) {
            bill.setLateFee(props.getMaintenance().getDefaultLateFee());
            bill.setTotalAmount(bill.getAmount().add(bill.getLateFee()));
            bill.setStatus(MaintenanceStatus.OVERDUE);
            billRepo.save(bill);
        }
        return bill;
    }

    public List<MaintenanceBill> findByFlat(Long flatId) { return billRepo.findByFlatId(flatId); }
}
