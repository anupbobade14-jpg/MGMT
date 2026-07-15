package com.society.management.repository;

import com.society.management.entity.Visitor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitorRepository extends JpaRepository<Visitor, Long> {
    Page<Visitor> findByFlatId(Long flatId, Pageable p);
}
