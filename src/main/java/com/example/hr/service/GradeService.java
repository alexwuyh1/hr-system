package com.example.hr.service;

import com.example.hr.model.Grade;
import com.example.hr.repository.GradeRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Grade service for CRUD.
 */
@Service
public class GradeService {
  private final GradeRepository gradeRepository;

  public GradeService(GradeRepository gradeRepository) {
    this.gradeRepository = gradeRepository;
  }

  public List<Grade> list() {
    return gradeRepository.findAll();
  }

  public Grade create(Grade grade) {
    return gradeRepository.save(grade);
  }

  public Grade update(Long id, Grade grade) {
    Grade existing =
        gradeRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Grade not found"));
    existing.setName(grade.getName());
    existing.setLevel(grade.getLevel());
    return gradeRepository.save(existing);
  }

  public void delete(Long id) {
    gradeRepository.deleteById(id);
  }
}
