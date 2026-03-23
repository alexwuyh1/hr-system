package com.example.hr.repository;

import com.example.hr.model.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Grade repository.
 */
public interface GradeRepository extends JpaRepository<Grade, Long> {}
