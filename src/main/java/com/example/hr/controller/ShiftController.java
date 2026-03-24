package com.example.hr.controller;

import com.example.hr.dto.ShiftRequest;
import com.example.hr.model.Shift;
import com.example.hr.service.ShiftService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.*;

/**
 * Shift schedule endpoints.
 */
@RestController
@RequestMapping("/api/shifts")
public class ShiftController {
  private final ShiftService shiftService;

  public ShiftController(ShiftService shiftService) {
    this.shiftService = shiftService;
  }

  @GetMapping
  public List<Shift> list(@RequestParam(required = false) String date) {
    if (date != null && !date.isBlank()) {
      return shiftService.listByDate(LocalDate.parse(date));
    }
    return shiftService.list();
  }

  @PostMapping
  public Shift create(@Valid @RequestBody ShiftRequest request) {
    return shiftService.create(request);
  }

  @PutMapping("/{id}")
  public Shift update(@PathVariable("id") Long id, @Valid @RequestBody ShiftRequest request) {
    return shiftService.update(id, request);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable("id") Long id) {
    shiftService.delete(id);
  }
}
