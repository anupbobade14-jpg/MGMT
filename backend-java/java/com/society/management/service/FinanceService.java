package com.society.management.service;

import com.society.management.entity.*;
import com.society.management.exception.ApiException;
import com.society.management.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final ExpenseRepository expenseRepo;
    private final ExpenseCategoryRepository expenseCatRepo;
    private final OtherIncomeRepository incomeRepo;
    private final IncomeCategoryRepository incomeCatRepo;
    private final MaintenanceBillRepository billRepo;

    public Map<String, Object> summary(LocalDate from, LocalDate to) {
        BigDecimal maintCol = billRepo.sumApprovedForYear(from.getYear());
        BigDecimal otherIncome = incomeRepo.sumBetween(from, to);
        BigDecimal expenses = expenseRepo.sumBetween(from, to);
        BigDecimal totalIncome = maintCol.add(otherIncome);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("period", Map.of("from", from.toString(), "to", to.toString()));
        m.put("maintenanceCollection", maintCol);
        m.put("otherIncome", otherIncome);
        m.put("totalIncome", totalIncome);
        m.put("totalExpenses", expenses);
        m.put("netBalance", totalIncome.subtract(expenses));
        return m;
    }

    public List<Map<String,Object>> categoryExpenseReport(LocalDate from, LocalDate to) {
        List<Map<String,Object>> out = new ArrayList<>();
        for (Object[] row : expenseRepo.categoryReport(from, to)) {
            out.add(Map.of("category", row[0], "amount", row[1]));
        }
        return out;
    }

    public Expense createExpense(Long categoryId, BigDecimal amount, LocalDate date, String vendor,
                                 String description, String invoiceNo, String paymentMode, User by) {
        ExpenseCategory cat = expenseCatRepo.findById(categoryId)
                .orElseThrow(() -> ApiException.notFound("Category not found"));
        return expenseRepo.save(Expense.builder()
                .category(cat).amount(amount).expenseDate(date).vendor(vendor)
                .description(description).invoiceNo(invoiceNo).paymentMode(paymentMode)
                .createdBy(by).build());
    }
}
