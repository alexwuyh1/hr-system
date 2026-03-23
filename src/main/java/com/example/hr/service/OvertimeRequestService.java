package com.example.hr.service;

import com.example.hr.dto.OvertimeRequestDto;
import com.example.hr.model.Employee;
import com.example.hr.model.OvertimeRequest;
import com.example.hr.repository.EmployeeRepository;
import com.example.hr.repository.OvertimeRequestRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Overtime request service.
 */
@Service
public class OvertimeRequestService {
  private final OvertimeRequestRepository overtimeRequestRepository;
  private final EmployeeRepository employeeRepository;

  public OvertimeRequestService(
      OvertimeRequestRepository overtimeRequestRepository, EmployeeRepository employeeRepository) {
    this.overtimeRequestRepository = overtimeRequestRepository;
    this.employeeRepository = employeeRepository;
  }

  public List<OvertimeRequest> list() {
    return overtimeRequestRepository.findAll();
  }

  public OvertimeRequest create(OvertimeRequestDto request) {
    OvertimeRequest overtime = new OvertimeRequest();
    apply(overtime, request);
    return overtimeRequestRepository.save(overtime);
  }

  public OvertimeRequest update(Long id, OvertimeRequestDto request) {
    OvertimeRequest overtime =
        overtimeRequestRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Overtime request not found"));
    apply(overtime, request);
    return overtimeRequestRepository.save(overtime);
  }

  public void delete(Long id) {
    overtimeRequestRepository.deleteById(id);
  }

  private void apply(OvertimeRequest overtime, OvertimeRequestDto request) {
    Employee employee =
        employeeRepository
            .findById(request.employeeId)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
    overtime.setEmployee(employee);
    overtime.setWorkDate(request.workDate);
    overtime.setMinutes(request.minutes);
    overtime.setStatus(request.status);
    overtime.setNote(request.note);
  }
}
