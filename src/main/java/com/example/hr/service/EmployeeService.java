package com.example.hr.service;

import com.example.hr.dto.EmployeeRequest;
import com.example.hr.exception.DuplicateResourceException;
import com.example.hr.exception.EmployeeNotFoundException;
import com.example.hr.exception.InvalidStateException;
import com.example.hr.exception.ResourceNotFoundException;
import com.example.hr.model.Employee;
import com.example.hr.model.Organization;
import com.example.hr.repository.EmployeeRepository;
import com.example.hr.repository.OrganizationRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final OrganizationRepository organizationRepository;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            OrganizationRepository organizationRepository) {
        this.employeeRepository = employeeRepository;
        this.organizationRepository = organizationRepository;
    }

    public List<Employee> list() {
        return employeeRepository.findAll();
    }

    @Transactional(rollbackFor = Exception.class)
    public Employee create(EmployeeRequest request) {
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
        return employeeRepository.save(employee);
    }

    @Transactional(rollbackFor = Exception.class)
    public Employee update(Long id, EmployeeRequest request) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException(id));
        
        employeeRepository.findByEmployeeNo(request.employeeNo)
            .ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new DuplicateResourceException("员工工号", request.employeeNo);
                }
            });
        
        apply(employee, request);
        return employeeRepository.save(employee);
    }

    @Transactional(rollbackFor = Exception.class)
    public Employee resign(String employeeNo) {
        Employee employee = employeeRepository.findByEmployeeNo(employeeNo)
            .orElseThrow(() -> new EmployeeNotFoundException(employeeNo));
        
        if (!"在职".equals(employee.getStatus())) {
            throw new InvalidStateException("员工", employee.getStatus(), "在职");
        }
        
        employee.setStatus("离职");
        return employeeRepository.save(employee);
    }

    @Transactional(rollbackFor = Exception.class)
    public Employee rehire(String employeeNo) {
        Employee employee = employeeRepository.findByEmployeeNo(employeeNo)
            .orElseThrow(() -> new EmployeeNotFoundException(employeeNo));
        
        if (!"离职".equals(employee.getStatus())) {
            throw new InvalidStateException("员工", employee.getStatus(), "离职");
        }
        
        employee.setStatus("在职");
        return employeeRepository.save(employee);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        employeeRepository.deleteById(id);
    }

    private void apply(Employee employee, EmployeeRequest request) {
        employee.setEmployeeNo(request.employeeNo);
        employee.setName(request.name);
        employee.setDepartment(request.department);
        employee.setTitle(request.title);
        employee.setPhone(request.phone);
        employee.setEmail(request.email);
        employee.setHireDate(request.hireDate);
        employee.setStatus(request.status);

        if (request.orgId != null) {
            Organization org = organizationRepository.findById(request.orgId)
                .orElseThrow(() -> new ResourceNotFoundException("组织", request.orgId));
            employee.setOrgRef(org);
            if ("部门".equals(org.getType())) {
                employee.setDepartment(org.getName());
            }
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
