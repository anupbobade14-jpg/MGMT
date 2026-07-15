package com.society.management.repository;

import com.society.management.entity.AppSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AppSettingRepository extends JpaRepository<AppSetting, Long> {
    Optional<AppSetting> findBySettingKey(String key);
}
