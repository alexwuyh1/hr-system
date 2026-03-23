package com.example.hr.repository;

import com.example.hr.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Role repository for role catalog.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {}
