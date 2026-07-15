package com.society.management.repository;

import com.society.management.entity.MaintenanceBill;
import com.society.management.entity.MaintenanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MaintenanceBillRepository extends JpaRepository<MaintenanceBill, Long> {

    Optional<MaintenanceBill> findByFlatIdAndBillMonthAndBillYear(Long flatId, Integer m, Integer y);

    List<MaintenanceBill> findByFlatId(Long flatId);

    Page<MaintenanceBill> findByStatus(MaintenanceStatus status, Pageable pageable);

    long countByStatus(MaintenanceStatus status);

    List<MaintenanceBill> findByStatusInAndDueDateBefore(List<MaintenanceStatus> statuses, LocalDate date);

    List<MaintenanceBill> findByStatusInAndDueDate(List<MaintenanceStatus> statuses, LocalDate date);

    @Query("select coalesce(sum(m.totalAmount),0) from MaintenanceBill m where m.status = :status")
    BigDecimal sumByStatus(MaintenanceStatus status);

    @Query("select coalesce(sum(m.totalAmount),0) from MaintenanceBill m " +
           "where m.status = com.society.management.entity.MaintenanceStatus.APPROVED " +
           "and m.billYear = :year")
    BigDecimal sumApprovedForYear(int year);

    @Query("select m.billMonth, coalesce(sum(m.totalAmount),0) from MaintenanceBill m " +
           "where m.status = com.society.management.entity.MaintenanceStatus.APPROVED " +
           "and m.billYear = :year group by m.billMonth order by m.billMonth")
    List<Object[]> monthlyCollectionForYear(int year);
}
