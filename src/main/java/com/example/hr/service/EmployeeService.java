package com.example.hr.service;

import com.example.hr.dto.EmployeeRequest;
import com.example.hr.dto.EmployeeResponse;
import com.example.hr.exception.ConflictException;
import com.example.hr.exception.NotFoundException;
import com.example.hr.exception.BadRequestException;
import com.example.hr.model.Employee;
import com.example.hr.model.Organization;
import com.example.hr.repository.AttendanceRepository;
import com.example.hr.repository.EmployeeRepository;
import com.example.hr.repository.OrganizationRepository;
import com.example.hr.repository.SalaryRepository;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
                    throw new ConflictException("员工", request.employeeNo);
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
            .orElseThrow(() -> new NotFoundException("员工", id));
        
        employeeRepository.findByEmployeeNo(request.employeeNo)
            .ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new ConflictException("员工工号", request.employeeNo);
                }
            });
        
        apply(employee, request);
        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    @Transactional(rollbackFor = Exception.class)
    public EmployeeResponse resign(String employeeNo) {
        Employee employee = employeeRepository.findByEmployeeNo(employeeNo)
            .orElseThrow(() -> new NotFoundException("员工", employeeNo));
        
        if (!"在职".equals(employee.getStatus())) {
            throw new BadRequestException("员工", employee.getStatus(), "在职");
        }
        
        employee.setStatus("离职");
        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    @Transactional(rollbackFor = Exception.class)
    public EmployeeResponse rehire(String employeeNo) {
        Employee employee = employeeRepository.findByEmployeeNo(employeeNo)
            .orElseThrow(() -> new NotFoundException("员工", employeeNo));
        
        if (!"离职".equals(employee.getStatus())) {
            throw new BadRequestException("员工", employee.getStatus(), "离职");
        }
        
        employee.setStatus("在职");
        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("员工", id));
        attendanceRepository.deleteByEmployee(employee);
        salaryRepository.deleteByEmployee(employee);
        employeeRepository.delete(employee);
    }

    @Transactional(rollbackFor = Exception.class)
    public EmployeeResponse uploadAvatar(Long id, byte[] fileBytes, String originalFilename) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("员工", id));
        if (fileBytes.length == 0) {
            throw new BadRequestException("文件为空");
        }
        try {
            Path dir = Paths.get("data", "avatars");
            Files.createDirectories(dir);
            Path target = dir.resolve("emp_" + id + ".jpg");
            Files.write(target, fileBytes);
            employee.setAvatarPath(target.toString());
            return EmployeeResponse.from(employeeRepository.save(employee));
        } catch (Exception e) {
            throw new RuntimeException("头像上传失败", e);
        }
    }

    public byte[] getAvatarBytes(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("员工", id));
        if (employee.getAvatarPath() == null) {
            return null;
        }
        File file = new File(employee.getAvatarPath());
        if (!file.exists()) {
            return null;
        }
        try (FileInputStream in = new FileInputStream(file)) {
            return in.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("读取头像失败", e);
        }
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
                .orElseThrow(() -> new NotFoundException("岗位", request.positionId));
            employee.setPositionRef(position);
            if (position.getParent() != null) {
                employee.setOrgRef(position.getParent());
            }
        } else {
            employee.setPositionRef(null);
        }

        if (request.managerId != null) {
            Employee manager = employeeRepository.findById(request.managerId)
                .orElseThrow(() -> new NotFoundException("员工", request.managerId));
            employee.setManagerRef(manager);
        } else {
            employee.setManagerRef(null);
        }
    }
}
