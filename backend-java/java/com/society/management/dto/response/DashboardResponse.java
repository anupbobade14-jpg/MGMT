package com.society.management.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardResponse(
        long totalFlats,
        long occupiedFlats,
        long vacantFlats,
        long totalOwners,
        long pendingMaintenance,
        long paidMaintenance,
        BigDecimal todaysCollection,
        BigDecimal monthlyCollection,
        BigDecimal totalExpenses,
        BigDecimal availableBalance,
        List<NoticeSummary> recentNotices,
        List<EventSummary> upcomingEvents,
        Map<String, BigDecimal> monthlyCollectionChart,
        Map<String, BigDecimal> monthlyExpenseChart) {

    public record NoticeSummary(Long id, String title, String publishedAt) {}
    public record EventSummary(Long id, String title, String eventDate, String location) {}
}
