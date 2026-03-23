package com.example.hr.repository;

import com.example.hr.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Position repository.
 */
public interface PositionRepository extends JpaRepository<Position, Long> {}
