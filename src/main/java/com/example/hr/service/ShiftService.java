package com.example.hr.service;

import com.example.hr.dto.ShiftRequest;
import com.example.hr.model.Employee;
import com.example.hr.model.Shift;
import com.example.hr.repository.EmployeeRepository;
import com.example.hr.repository.ShiftRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Shift service for CRUD.
 */
@Service
public class ShiftService {
  private final ShiftRepository shiftRepository;
  private final EmployeeRepository employeeRepository;

  public ShiftService(ShiftRepository shiftRepository, EmployeeRepository employeeRepository) {
    this.shiftRepository = shiftRepository;
    this.employeeRepository = employeeRepository;
  }

  public List<Shift> list() {
    return shiftRepository.findAll();
  }

  public List<Shift> listByDate(LocalDate date) {
    return shiftRepository.findByWorkDate(date);
  }

  public Shift create(ShiftRequest request) {
    Shift shift = new Shift();
    apply(shift, request);
    return shiftRepository.save(shift);
  }

  public Shift update(Long id, ShiftRequest request) {
    Shift shift =
        shiftRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Shift not found"));
    apply(shift, request);
    return shiftRepository.save(shift);
  }

  public void delete(Long id) {
    shiftRepository.deleteById(id);
  }

  private void apply(Shift shift, ShiftRequest request) {
    Employee employee =
        employeeRepository
            .findById(request.employeeId)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
    shift.setEmployee(employee);
    shift.setWorkDate(request.workDate);
    shift.setStartTime(request.startTime);
    shift.setEndTime(request.endTime);
    shift.setNote(request.note);
  }
}
