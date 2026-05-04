package com.example.hr.service;

import com.example.hr.dto.EmployeeRequest;
import com.example.hr.dto.EmployeeResponse;
import com.example.hr.exception.DuplicateResourceException;
import com.example.hr.exception.EmployeeNotFoundException;
import com.example.hr.exception.InvalidStateException;
import com.example.hr.exception.ResourceNotFoundException;
import com.example.hr.model.Employee;
import com.example.hr.model.Organization;
import com.example.hr.repository.AttendanceRepository;
import com.example.hr.repository.EmployeeRepository;
import com.example.hr.repository.OrganizationRepository;
import com.example.hr.repository.SalaryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;
    private final AttendanceRepository attendanceRepository;
    private final SalaryRepository salaryRepository;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            OrganizationRepository organizationRepository,
            AttendanceRepository attendanceRepository,
            SalaryRepository salaryRepository) {
        this.employeeRepository = employeeRepository;
        this.organizationRepository = organizationRepository;
        this.attendanceRepository = attendanceRepository;
        this.salaryRepository = salaryRepository;
    }

    public List<EmployeeResponse> listResponses() {
        return employeeRepository.findAll().stream()
            .map(EmployeeResponse::from)
            .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public EmployeeResponse create(EmployeeRequest request) {
        employeeRepository.findByEmployeeNo(request.employeeNo)
            .ifPresent(existing -> {
                if ("在职".equals(existing.getStatus())) {
                    throw new DuplicateResourceException("员工", request.employeeNo);
                }
            });

        Employee employee = employeeRepository.findByEmployeeNo(request.employeeNo)
            .orElseGet(Employee::new);
        
        apply(employee, request);
        employee.setStatus("在职");
        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    @Transactional(rollbackFor = Exception.class)
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
        
        employeeRepository.findByEmployeeNo(request.employeeNo)
            .ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new DuplicateResourceException("员工工号", request.employeeNo);
                }
            });
        
        apply(employee, request);
        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    @Transactional(rollbackFor = Exception.class)
    public EmployeeResponse resign(String employeeNo) {
        Employee employee = employeeRepository.findByEmployeeNo(employeeNo)
            .orElseThrow(() -> new EmployeeNotFoundException(employeeNo));
        
        if (!"在职".equals(employee.getStatus())) {
            throw new InvalidStateException("员工", employee.getStatus(), "在职");
        }
        
        employee.setStatus("离职");
        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    @Transactional(rollbackFor = Exception.class)
    public EmployeeResponse rehire(String employeeNo) {
        Employee employee = employeeRepository.findByEmployeeNo(employeeNo)
            .orElseThrow(() -> new EmployeeNotFoundException(employeeNo));
        
        if (!"离职".equals(employee.getStatus())) {
            throw new InvalidStateException("员工", employee.getStatus(), "离职");
        }
        
        employee.setStatus("在职");
        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
        attendanceRepository.deleteByEmployee(employee);
        salaryRepository.deleteByEmployee(employee);
        employeeRepository.delete(employee);
    }

    private void apply(Employee employee, EmployeeRequest request) {
        employee.setEmployeeNo(request.employeeNo);
        employee.setName(request.name);
        employee.setPhone(request.phone);
        employee.setEmail(request.email);
        employee.setHireDate(request.hireDate);
        employee.setStatus(request.status);

        if (request.positionId != null) {
            Organization position = organizationRepository.findById(request.positionId)
                .orElseThrow(() -> new ResourceNotFoundException("岗位", request.positionId));
            employee.setPositionRef(position);
            if (position.getParent() != null) {
                employee.setOrgRef(position.getParent());
            }
        } else {
            employee.setPositionRef(null);
        }

        if (request.managerId != null) {
            Employee manager = employeeRepository.findById(request.managerId)
                .orElseThrow(() -> new EmployeeNotFoundException(request.managerId));
            employee.setManagerRef(manager);
        } else {
            employee.setManagerRef(null);
        }
    }
}
