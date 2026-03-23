package com.example.hr.controller;

import com.example.hr.dto.OvertimeRequestDto;
import com.example.hr.model.OvertimeRequest;
import com.example.hr.service.OvertimeRequestService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

/**
 * Overtime request endpoints.
 */
@RestController
@RequestMapping("/api/overtime")
public class OvertimeController {
  private final OvertimeRequestService overtimeRequestService;

  public OvertimeController(OvertimeRequestService overtimeRequestService) {
    this.overtimeRequestService = overtimeRequestService;
  }

  @GetMapping
  public List<OvertimeRequest> list() {
    return overtimeRequestService.list();
  }

  @PostMapping
  public OvertimeRequest create(@Valid @RequestBody OvertimeRequestDto request) {
    return overtimeRequestService.create(request);
  }

  @PutMapping("/{id}")
  public OvertimeRequest update(@PathVariable Long id, @Valid @RequestBody OvertimeRequestDto request) {
    return overtimeRequestService.update(id, request);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    overtimeRequestService.delete(id);
  }
}
