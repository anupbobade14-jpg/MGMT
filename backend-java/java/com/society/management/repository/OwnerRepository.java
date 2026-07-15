package com.society.management.repository;

import com.society.management.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
    List<Owner> findByFlatId(Long flatId);
    Optional<Owner> findByUserId(Long userId);
    Optional<Owner> findFirstByFlatIdAndPrimaryOwnerTrue(Long flatId);
}
