package com.society.management.service;

import com.society.management.entity.MaintenanceBill;
import com.society.management.entity.MaintenanceStatus;
import com.society.management.entity.Owner;
import com.society.management.repository.MaintenanceBillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderService {

    private final MaintenanceBillRepository billRepo;
    private final EmailService emailService;

    public void sendDueRemindersFor(LocalDate targetDate) {
        List<MaintenanceBill> bills = billRepo.findByStatusInAndDueDate(
                List.of(MaintenanceStatus.PENDING, MaintenanceStatus.OVERDUE), targetDate);
        for (MaintenanceBill bill : bills) {
            Owner owner = bill.getOwner();
            if (owner == null || owner.getEmail() == null) continue;
            Map<String, String> vars = new HashMap<>();
            vars.put("ownerName", owner.getFullName());
            vars.put("month", java.time.Month.of(bill.getBillMonth()).name());
            vars.put("year", String.valueOf(bill.getBillYear()));
            vars.put("amount", bill.getTotalAmount().toString());
            vars.put("dueDate", bill.getDueDate().toString());
            emailService.sendTemplate(owner.getEmail(), "DUE_REMINDER", vars);
        }
        log.info("Due reminders sent: {} for date {}", bills.size(), targetDate);
    }

    public void sendLateFeeReminders() {
        List<MaintenanceBill> overdue = billRepo.findByStatusInAndDueDateBefore(
                List.of(MaintenanceStatus.PENDING, MaintenanceStatus.OVERDUE), LocalDate.now());
        for (MaintenanceBill bill : overdue) {
            Owner owner = bill.getOwner();
            if (owner == null || owner.getEmail() == null) continue;
            Map<String, String> vars = new HashMap<>();
            vars.put("ownerName", owner.getFullName());
            vars.put("month", java.time.Month.of(bill.getBillMonth()).name());
            vars.put("year", String.valueOf(bill.getBillYear()));
            vars.put("lateFee", bill.getLateFee().toString());
            emailService.sendTemplate(owner.getEmail(), "LATE_FEE_REMINDER", vars);
        }
    }
}
