package com.example.hr.controller;

import com.example.hr.model.Role;
import com.example.hr.repository.RoleRepository;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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

  @PostMapping
  public Role create(@RequestBody Map<String, String> request) {
    String name = request.get("name");
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("角色名称不能为空");
    }
    if (roleRepository.existsByName(name)) {
      throw new IllegalArgumentException("角色名称已存在");
    }
    Role role = new Role();
    role.setName(name);
    return roleRepository.save(role);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    roleRepository.deleteById(id);
  }
}
