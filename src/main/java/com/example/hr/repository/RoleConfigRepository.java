package com.example.hr.repository;

import com.example.hr.model.RoleConfig;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleConfigRepository extends JpaRepository<RoleConfig, Long> {
  Optional<RoleConfig> findByRole(String role);
  boolean existsByRole(String role);
}
