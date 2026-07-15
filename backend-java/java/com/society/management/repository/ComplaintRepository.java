package com.society.management.repository;

import com.society.management.entity.Complaint;
import com.society.management.entity.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    Page<Complaint> findByStatus(ComplaintStatus status, Pageable p);
    Page<Complaint> findByRaisedById(Long userId, Pageable p);
}
