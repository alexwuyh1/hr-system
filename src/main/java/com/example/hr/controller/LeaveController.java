package com.example.hr.controller;

import com.example.hr.dto.LeaveRequestDto;
import com.example.hr.model.LeaveRequest;
import com.example.hr.service.LeaveRequestService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.*;

/**
 * Leave request endpoints.
 */
@RestController
@RequestMapping("/api/leaves")
public class LeaveController {
  private final LeaveRequestService leaveRequestService;

  public LeaveController(LeaveRequestService leaveRequestService) {
    this.leaveRequestService = leaveRequestService;
  }

  @GetMapping
  public List<LeaveRequest> list() {
    return leaveRequestService.list();
  }

  @PostMapping
  public LeaveRequest create(@Valid @RequestBody LeaveRequestDto request) {
    return leaveRequestService.create(request);
  }

  @PutMapping("/{id}")
  public LeaveRequest update(@PathVariable("id") Long id, @Valid @RequestBody LeaveRequestDto request) {
    return leaveRequestService.update(id, request);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable("id") Long id) {
    leaveRequestService.delete(id);
  }
}
