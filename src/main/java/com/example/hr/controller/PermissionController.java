package com.example.hr.controller;

import com.example.hr.model.Permission;
import com.example.hr.repository.RoleRepository;
import com.example.hr.service.PermissionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

/**
 * Permission management endpoints.
 * Only 管理员 can access these endpoints (enforced by AuthInterceptor).
 */
@RestController
@RequestMapping("/api/permissions")
public class PermissionController {
  private final PermissionService permissionService;
  private final RoleRepository roleRepository;

  public PermissionController(PermissionService permissionService, RoleRepository roleRepository) {
    this.permissionService = permissionService;
    this.roleRepository = roleRepository;
  }

  @GetMapping
  public List<Permission> list() {
    return permissionService.list();
  }

  @PostMapping
  public Permission create(@Valid @RequestBody PermissionRequest request) {
    validateRole(request.role);
    Permission permission = new Permission();
    permission.setRole(request.role);
    permission.setMethod(request.method);
    permission.setPathPrefix(request.pathPrefix);
    return permissionService.create(permission);
  }

  @PutMapping("/{id}")
  public Permission update(@PathVariable("id") Long id, @Valid @RequestBody PermissionRequest request) {
    validateRole(request.role);
    Permission permission = new Permission();
    permission.setRole(request.role);
    permission.setMethod(request.method);
    permission.setPathPrefix(request.pathPrefix);
    return permissionService.update(id, permission);
  }

  @DeleteMapping("/{id}")
  public Map<String, String> delete(@PathVariable("id") Long id) {
    permissionService.delete(id);
    return Map.of("message", "Permission deleted");
  }

  private void validateRole(String role) {
    boolean exists =
        roleRepository.findAll().stream().anyMatch(r -> r.getName().equals(role));
    if (!exists) {
      throw new IllegalArgumentException("Role not found");
    }
  }

  public static class PermissionRequest {
    @NotBlank public String role;
    @NotBlank public String method;
    @NotBlank public String pathPrefix;
  }
}
