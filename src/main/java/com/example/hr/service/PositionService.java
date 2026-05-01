package com.example.hr.service;

import com.example.hr.exception.ResourceNotFoundException;
import com.example.hr.model.Position;
import com.example.hr.repository.PositionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PositionService {
    private final PositionRepository positionRepository;

    public PositionService(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    public List<Position> list() {
        return positionRepository.findAll();
    }

    @Transactional(rollbackFor = Exception.class)
    public Position create(Position position) {
        return positionRepository.save(position);
    }

    @Transactional(rollbackFor = Exception.class)
    public Position update(Long id, Position position) {
        Position existing = positionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("岗位", id));
        existing.setName(position.getName());
        return positionRepository.save(existing);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        positionRepository.deleteById(id);
    }
}
