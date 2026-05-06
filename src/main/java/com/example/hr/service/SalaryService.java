package com.example.hr.service;

import com.example.hr.dto.SalaryRequest;
import com.example.hr.exception.BadRequestException;
import com.example.hr.exception.NotFoundException;
import com.example.hr.model.Employee;
import com.example.hr.model.Salary;
import com.example.hr.repository.EmployeeRepository;
import com.example.hr.repository.SalaryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SalaryService {
    private final SalaryRepository salaryRepository;
    private final EmployeeRepository employeeRepository;

    public SalaryService(
            SalaryRepository salaryRepository, 
            EmployeeRepository employeeRepository) {
        this.salaryRepository = salaryRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<Salary> list() {
        List<Salary> salaries = salaryRepository.findAll();
        salaries.forEach(s -> initEmployeeForSerialization(s.getEmployee()));
        return salaries;
    }

    @Transactional(rollbackFor = Exception.class)
    public Salary create(SalaryRequest request) {
        Salary salary = new Salary();
        apply(salary, request);
        salary = salaryRepository.save(salary);
        initEmployeeForSerialization(salary.getEmployee());
        return salary;
    }

    @Transactional(rollbackFor = Exception.class)
    public Salary update(Long id, SalaryRequest request) {
        Salary salary = salaryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("薪资记录", id));
        apply(salary, request);
        salary = salaryRepository.save(salary);
        initEmployeeForSerialization(salary.getEmployee());
        return salary;
    }

    private void initEmployeeForSerialization(Employee employee) {
        if (employee == null) return;
        employee.getName();
        if (employee.getOrgRef() != null) employee.getOrgRef().getName();
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        salaryRepository.deleteById(id);
    }

    private void apply(Salary salary, SalaryRequest request) {
        Employee employee = employeeRepository.findById(request.employeeId)
            .orElseThrow(() -> new NotFoundException("员工", request.employeeId));
        
        validateAmounts(request.baseSalary, request.bonus, request.deduction);
        
        salary.setEmployee(employee);
        salary.setSalaryMonth(request.salaryMonth);
        salary.setBaseSalary(request.baseSalary);
        salary.setBonus(request.bonus);
        salary.setDeduction(request.deduction);
        salary.setNote(request.note);
    }

    private void validateAmounts(Double base, Double bonus, Double deduction) {
        if (base == null || bonus == null || deduction == null) {
            throw new BadRequestException("基本工资、奖金、扣款均为必填项");
        }
        if (base < 0) {
            throw new BadRequestException("基本工资不能为负数");
        }
        if (bonus < 0) {
            throw new BadRequestException("奖金不能为负数");
        }
        if (deduction < 0) {
            throw new BadRequestException("扣款不能为负数");
        }
        double total = base + bonus - deduction;
        if (total < 0) {
            throw new BadRequestException("薪资总额不能为负数");
        }
    }
}
