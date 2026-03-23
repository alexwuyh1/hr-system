package com.example.hr.controller;

import com.example.hr.model.Position;
import com.example.hr.service.PositionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

/**
 * Position management endpoints.
 */
@RestController
@RequestMapping("/api/positions")
public class PositionController {
  private final PositionService positionService;

  public PositionController(PositionService positionService) {
    this.positionService = positionService;
  }

  @GetMapping
  public List<Position> list() {
    return positionService.list();
  }

  @PostMapping
  public Position create(@Valid @RequestBody PositionRequest request) {
    Position position = new Position();
    position.setName(request.name);
    return positionService.create(position);
  }

  @PutMapping("/{id}")
  public Position update(@PathVariable Long id, @Valid @RequestBody PositionRequest request) {
    Position position = new Position();
    position.setName(request.name);
    return positionService.update(id, position);
  }

  @DeleteMapping("/{id}")
  public Map<String, String> delete(@PathVariable Long id) {
    positionService.delete(id);
    return Map.of("message", "Position deleted");
  }

  public static class PositionRequest {
    @NotBlank public String name;
  }
}
