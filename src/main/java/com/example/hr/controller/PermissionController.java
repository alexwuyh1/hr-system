package com.example.hr.controller;

import com.example.hr.model.Permission;
import com.example.hr.service.PermissionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {
  private final PermissionService permissionService;

  public PermissionController(PermissionService permissionService) {
    this.permissionService = permissionService;
  }

  @GetMapping
  public List<Permission> list() {
    return permissionService.list();
  }

  @GetMapping("/roles")
  public List<Map<String, String>> listRoles() {
    return permissionService.listRolesWithMode();
  }

  @PostMapping
  public Permission create(@Valid @RequestBody PermissionRequest request) {
    return permissionService.createPermission(request.role, request.method, request.pathPrefix);
  }

  @PostMapping("/role")
  public Map<String, Object> createRole(@Valid @RequestBody RoleRequest request) {
    permissionService.createRoleWithMode(request.role, request.roleMode);
    return Map.of("message", "角色创建成功", "role", request.role, "roleMode", request.roleMode);
  }

  @PutMapping("/{id}")
  public Permission update(@PathVariable("id") Long id, @Valid @RequestBody PermissionRequest request) {
    return permissionService.update(id, request.role, request.method, request.pathPrefix);
  }

  @DeleteMapping("/{id}")
  public Map<String, String> delete(@PathVariable("id") Long id) {
    permissionService.delete(id);
    return Map.of("message", "权限已删除");
  }

  @DeleteMapping("/role/{role}")
  public Map<String, String> deleteRole(@PathVariable("role") String role) {
    permissionService.deleteRole(role);
    return Map.of("message", "角色已删除");
  }

  public static class PermissionRequest {
    @NotBlank public String role;
    @NotBlank public String method;
    @NotBlank public String pathPrefix;
  }

  public static class RoleRequest {
    @NotBlank public String role;
    @NotBlank public String roleMode;
  }
}
