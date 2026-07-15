package com.society.management.controller;

import com.society.management.dto.request.ExpenseRequest;
import com.society.management.entity.Expense;
import com.society.management.entity.ExpenseCategory;
import com.society.management.entity.IncomeCategory;
import com.society.management.entity.OtherIncome;
import com.society.management.entity.User;
import com.society.management.repository.*;
import com.society.management.service.FinanceService;
import com.society.management.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService finance;
    private final ExpenseRepository expenseRepo;
    private final ExpenseCategoryRepository expenseCatRepo;
    private final OtherIncomeRepository incomeRepo;
    private final IncomeCategoryRepository incomeCatRepo;
    private final UserRepository userRepo;

    @GetMapping("/summary")
    public Map<String, Object> summary(@RequestParam String from, @RequestParam String to) {
        return finance.summary(LocalDate.parse(from), LocalDate.parse(to));
    }

    @GetMapping("/expenses")
    public List<Expense> expenses(@RequestParam String from, @RequestParam String to) {
        return expenseRepo.findByExpenseDateBetween(LocalDate.parse(from), LocalDate.parse(to));
    }

    @PostMapping("/expenses")
    public Expense createExpense(@Valid @RequestBody ExpenseRequest r) {
        User by = userRepo.findById(SecurityUtils.currentUserId()).orElse(null);
        return finance.createExpense(r.categoryId(), r.amount(), LocalDate.parse(r.expenseDate()),
                r.vendor(), r.description(), r.invoiceNo(), r.paymentMode(), by);
    }

    @GetMapping("/expense-categories")
    public List<ExpenseCategory> expenseCats() { return expenseCatRepo.findAll(); }

    @PostMapping("/expense-categories")
    public ExpenseCategory addExpenseCat(@RequestBody ExpenseCategory c) { return expenseCatRepo.save(c); }

    @GetMapping("/incomes")
    public List<OtherIncome> incomes(@RequestParam String from, @RequestParam String to) {
        return incomeRepo.findByIncomeDateBetween(LocalDate.parse(from), LocalDate.parse(to));
    }

    @PostMapping("/incomes")
    public OtherIncome addIncome(@RequestBody OtherIncome i) {
        i.setCreatedBy(userRepo.findById(SecurityUtils.currentUserId()).orElse(null));
        return incomeRepo.save(i);
    }

    @GetMapping("/income-categories")
    public List<IncomeCategory> incomeCats() { return incomeCatRepo.findAll(); }

    @GetMapping("/category-expense-report")
    public List<Map<String, Object>> categoryReport(@RequestParam String from, @RequestParam String to) {
        return finance.categoryExpenseReport(LocalDate.parse(from), LocalDate.parse(to));
    }
}
