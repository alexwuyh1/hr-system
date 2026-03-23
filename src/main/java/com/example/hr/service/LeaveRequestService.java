package com.example.hr.service;

import com.example.hr.dto.LeaveRequestDto;
import com.example.hr.model.Employee;
import com.example.hr.model.LeaveRequest;
import com.example.hr.repository.EmployeeRepository;
import com.example.hr.repository.LeaveRequestRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Leave request service.
 */
@Service
public class LeaveRequestService {
  private final LeaveRequestRepository leaveRequestRepository;
  private final EmployeeRepository employeeRepository;

  public LeaveRequestService(
      LeaveRequestRepository leaveRequestRepository, EmployeeRepository employeeRepository) {
    this.leaveRequestRepository = leaveRequestRepository;
    this.employeeRepository = employeeRepository;
  }

  public List<LeaveRequest> list() {
    return leaveRequestRepository.findAll();
  }

  public LeaveRequest create(LeaveRequestDto request) {
    LeaveRequest leave = new LeaveRequest();
    apply(leave, request);
    return leaveRequestRepository.save(leave);
  }

  public LeaveRequest update(Long id, LeaveRequestDto request) {
    LeaveRequest leave =
        leaveRequestRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Leave request not found"));
    apply(leave, request);
    return leaveRequestRepository.save(leave);
  }

  public void delete(Long id) {
    leaveRequestRepository.deleteById(id);
  }

  private void apply(LeaveRequest leave, LeaveRequestDto request) {
    Employee employee =
        employeeRepository
            .findById(request.employeeId)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
    leave.setEmployee(employee);
    leave.setStartDate(request.startDate);
    leave.setEndDate(request.endDate);
    leave.setType(request.type);
    leave.setStatus(request.status);
    leave.setNote(request.note);
  }
}
