package com.example.hr.service;

import com.example.hr.model.Position;
import com.example.hr.repository.PositionRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Position service for CRUD.
 */
@Service
public class PositionService {
  private final PositionRepository positionRepository;

  public PositionService(PositionRepository positionRepository) {
    this.positionRepository = positionRepository;
  }

  public List<Position> list() {
    return positionRepository.findAll();
  }

  public Position create(Position position) {
    return positionRepository.save(position);
  }

  public Position update(Long id, Position position) {
    Position existing =
        positionRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Position not found"));
    existing.setName(position.getName());
    return positionRepository.save(existing);
  }

  public void delete(Long id) {
    positionRepository.deleteById(id);
  }
}
