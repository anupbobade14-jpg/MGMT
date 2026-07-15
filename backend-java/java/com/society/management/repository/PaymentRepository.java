package com.society.management.repository;

import com.society.management.entity.Payment;
import com.society.management.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByMaintenanceId(Long maintenanceId);
    Page<Payment> findByStatus(PaymentStatus status, Pageable p);

    @Query("select coalesce(sum(p.amount),0) from Payment p " +
           "where p.status = com.society.management.entity.PaymentStatus.APPROVED " +
           "and p.paymentDate = :date")
    BigDecimal totalCollectedOn(LocalDate date);

    @Query("select coalesce(sum(p.amount),0) from Payment p " +
           "where p.status = com.society.management.entity.PaymentStatus.APPROVED " +
           "and p.paymentDate between :from and :to")
    BigDecimal totalCollectedBetween(LocalDate from, LocalDate to);

    long countByStatus(PaymentStatus status);
}
