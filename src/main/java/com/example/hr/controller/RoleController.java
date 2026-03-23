package com.example.hr.controller;

import com.example.hr.model.Role;
import com.example.hr.repository.RoleRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Role catalog endpoints.
 * Only ADMIN can access (enforced by AuthInterceptor).
 */
@RestController
@RequestMapping("/api/roles")
public class RoleController {
  private final RoleRepository roleRepository;

  public RoleController(RoleRepository roleRepository) {
    this.roleRepository = roleRepository;
  }

  @GetMapping
  public List<Role> list() {
    return roleRepository.findAll();
  }
}
