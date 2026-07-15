package com.society.management.repository;

import com.society.management.entity.OtherIncome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface OtherIncomeRepository extends JpaRepository<OtherIncome, Long> {

    List<OtherIncome> findByIncomeDateBetween(LocalDate from, LocalDate to);

    @Query("select coalesce(sum(i.amount),0) from OtherIncome i where i.incomeDate between :from and :to")
    BigDecimal sumBetween(LocalDate from, LocalDate to);
}
