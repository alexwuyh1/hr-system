package com.example.hr.service;

import com.example.hr.exception.ResourceNotFoundException;
import com.example.hr.model.Grade;
import com.example.hr.repository.GradeRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GradeService {
    private final GradeRepository gradeRepository;

    public GradeService(GradeRepository gradeRepository) {
        this.gradeRepository = gradeRepository;
    }

    public List<Grade> list() {
        return gradeRepository.findAll();
    }

    @Transactional(rollbackFor = Exception.class)
    public Grade create(Grade grade) {
        return gradeRepository.save(grade);
    }

    @Transactional(rollbackFor = Exception.class)
    public Grade update(Long id, Grade grade) {
        Grade existing = gradeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("职级", id));
        existing.setName(grade.getName());
        existing.setLevel(grade.getLevel());
        return gradeRepository.save(existing);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        gradeRepository.deleteById(id);
    }
}
