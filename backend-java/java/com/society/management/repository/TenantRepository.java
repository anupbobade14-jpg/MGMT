package com.society.management.repository;

import com.society.management.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    List<Tenant> findByFlatId(Long flatId);
}
