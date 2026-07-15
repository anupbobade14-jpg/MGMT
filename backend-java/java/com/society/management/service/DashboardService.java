package com.society.management.service;

import com.society.management.dto.response.DashboardResponse;
import com.society.management.entity.*;
import com.society.management.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FlatRepository flatRepo;
    private final UserRepository userRepo;
    private final MaintenanceBillRepository billRepo;
    private final PaymentRepository paymentRepo;
    private final ExpenseRepository expenseRepo;
    private final OtherIncomeRepository incomeRepo;
    private final NoticeRepository noticeRepo;
    private final EventRepository eventRepo;

    public DashboardResponse buildDashboard() {
        LocalDate today = LocalDate.now();
        LocalDate first = today.withDayOfMonth(1);
        LocalDate last = today.withDayOfMonth(today.lengthOfMonth());
        int year = today.getYear();

        long totalFlats = flatRepo.count();
        long occupied = flatRepo.countByOccupancy(OccupancyStatus.OCCUPIED) + flatRepo.countByOccupancy(OccupancyStatus.RENTED);
        long vacant = flatRepo.countByOccupancy(OccupancyStatus.VACANT);
        long owners = userRepo.countOwners();
        long pending = billRepo.countByStatus(MaintenanceStatus.PENDING) + billRepo.countByStatus(MaintenanceStatus.OVERDUE);
        long paid = billRepo.countByStatus(MaintenanceStatus.APPROVED);

        BigDecimal todaysCol = paymentRepo.totalCollectedOn(today);
        BigDecimal monthlyCol = paymentRepo.totalCollectedBetween(first, last);
        BigDecimal expensesYtd = expenseRepo.sumBetween(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
        BigDecimal incomeYtd = billRepo.sumApprovedForYear(year)
                .add(incomeRepo.sumBetween(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31)));
        BigDecimal balance = incomeYtd.subtract(expensesYtd);

        // Charts (monthly)
        Map<String, BigDecimal> monthlyCollectionChart = emptyMonthMap();
        for (Object[] row : billRepo.monthlyCollectionForYear(year)) {
            int m = ((Number) row[0]).intValue();
            monthlyCollectionChart.put(java.time.Month.of(m).name(), (BigDecimal) row[1]);
        }
        Map<String, BigDecimal> monthlyExpenseChart = emptyMonthMap();
        for (Object[] row : expenseRepo.monthlyExpensesForYear(year)) {
            int m = ((Number) row[0]).intValue();
            monthlyExpenseChart.put(java.time.Month.of(m).name(), (BigDecimal) row[1]);
        }

        List<DashboardResponse.NoticeSummary> notices = noticeRepo
                .findAllByOrderByPinnedDescPublishedAtDesc()
                .stream().limit(5)
                .map(n -> new DashboardResponse.NoticeSummary(n.getId(), n.getTitle(),
                        n.getPublishedAt() == null ? "" : n.getPublishedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
                .toList();

        List<DashboardResponse.EventSummary> events = eventRepo
                .findByEventDateAfterOrderByEventDateAsc(OffsetDateTime.now())
                .stream().limit(5)
                .map(e -> new DashboardResponse.EventSummary(e.getId(), e.getTitle(),
                        e.getEventDate().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), e.getLocation()))
                .toList();

        return new DashboardResponse(totalFlats, occupied, vacant, owners,
                pending, paid, todaysCol, monthlyCol, expensesYtd, balance,
                notices, events, monthlyCollectionChart, monthlyExpenseChart);
    }

    private Map<String, BigDecimal> emptyMonthMap() {
        Map<String, BigDecimal> m = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++) m.put(java.time.Month.of(i).name(), BigDecimal.ZERO);
        return m;
    }
}
