package com.emergent.pos.repository;

import com.emergent.pos.model.AppConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppConfigRepository extends JpaRepository<AppConfig, Integer> {
    Optional<AppConfig> findByConfigKey(String configKey);
}
