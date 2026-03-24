package com.example.hr.controller;

import com.example.hr.model.Grade;
import com.example.hr.service.GradeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

/**
 * Grade management endpoints.
 */
@RestController
@RequestMapping("/api/grades")
public class GradeController {
  private final GradeService gradeService;

  public GradeController(GradeService gradeService) {
    this.gradeService = gradeService;
  }

  @GetMapping
  public List<Grade> list() {
    return gradeService.list();
  }

  @PostMapping
  public Grade create(@Valid @RequestBody GradeRequest request) {
    Grade grade = new Grade();
    grade.setName(request.name);
    grade.setLevel(request.level);
    return gradeService.create(grade);
  }

  @PutMapping("/{id}")
  public Grade update(@PathVariable("id") Long id, @Valid @RequestBody GradeRequest request) {
    Grade grade = new Grade();
    grade.setName(request.name);
    grade.setLevel(request.level);
    return gradeService.update(id, grade);
  }

  @DeleteMapping("/{id}")
  public Map<String, String> delete(@PathVariable("id") Long id) {
    gradeService.delete(id);
    return Map.of("message", "Grade deleted");
  }

  public static class GradeRequest {
    @NotBlank public String name;
    @NotNull public Integer level;
  }
}
