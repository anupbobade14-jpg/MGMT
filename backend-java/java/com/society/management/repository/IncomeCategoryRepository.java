package com.society.management.repository;

import com.society.management.entity.IncomeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IncomeCategoryRepository extends JpaRepository<IncomeCategory, Long> {
    Optional<IncomeCategory> findByNameIgnoreCase(String name);
}
