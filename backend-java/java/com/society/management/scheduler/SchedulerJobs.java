package com.society.management.scheduler;

import com.society.management.dto.request.MaintenanceGenerationRequest;
import com.society.management.service.MaintenanceService;
import com.society.management.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchedulerJobs {

    private final ReminderService reminderService;
    private final MaintenanceService maintenanceService;

    /** Daily at 09:00 - send due reminders (7d, 3d, on-due, overdue weekly). */
    @Scheduled(cron = "${app.maintenance.reminder-cron}")
    public void dailyReminderJob() {
        LocalDate today = LocalDate.now();
        reminderService.sendDueRemindersFor(today.plusDays(7));
        reminderService.sendDueRemindersFor(today.plusDays(3));
        reminderService.sendDueRemindersFor(today);
        // weekly reminder after due date
        if (today.getDayOfWeek().getValue() == 1) reminderService.sendLateFeeReminders();
        log.info("Daily reminder job completed");
    }

    /** 01:00 on the 1st of every month - auto-generate maintenance bills. */
    @Scheduled(cron = "${app.maintenance.generation-cron}")
    public void monthlyBillGenerationJob() {
        LocalDate today = LocalDate.now();
        maintenanceService.generateMonthlyBills(
                new MaintenanceGenerationRequest(today.getMonthValue(), today.getYear(), null, null));
        log.info("Monthly bill generation job completed");
    }
}
