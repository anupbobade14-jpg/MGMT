package com.society.management.repository;

import com.society.management.entity.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {
    Optional<ExpenseCategory> findByNameIgnoreCase(String name);
}
