package com.society.management.repository;

import com.society.management.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByExpenseDateBetween(LocalDate from, LocalDate to);

    @Query("select coalesce(sum(e.amount),0) from Expense e where e.expenseDate between :from and :to")
    BigDecimal sumBetween(LocalDate from, LocalDate to);

    @Query("select month(e.expenseDate), coalesce(sum(e.amount),0) from Expense e " +
           "where year(e.expenseDate) = :year group by month(e.expenseDate) order by month(e.expenseDate)")
    List<Object[]> monthlyExpensesForYear(int year);

    @Query("select e.category.name, coalesce(sum(e.amount),0) from Expense e " +
           "where e.expenseDate between :from and :to group by e.category.name")
    List<Object[]> categoryReport(LocalDate from, LocalDate to);
}
