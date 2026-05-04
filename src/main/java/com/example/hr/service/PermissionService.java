package com.example.hr.service;

import com.example.hr.model.Permission;
import com.example.hr.repository.PermissionRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {
  private final PermissionRepository permissionRepository;

  public PermissionService(PermissionRepository permissionRepository) {
    this.permissionRepository = permissionRepository;
  }

  public List<Permission> list() {
    return permissionRepository.findAll();
  }

  public List<Map<String, String>> listRolesWithMode() {
    List<Permission> all = permissionRepository.findAll();
    return all.stream()
        .collect(Collectors.groupingBy(
            Permission::getRole,
            Collectors.mapping(Permission::getRoleMode, Collectors.toList())
        ))
        .entrySet().stream()
        .filter(e -> !e.getKey().equals("管理员"))
        .map(e -> Map.of(
            "name", e.getKey(),
            "role", e.getKey(),
            "roleMode", e.getValue().stream().filter(m -> m != null).findFirst().orElse("whitelist")
        ))
        .collect(Collectors.toList());
  }

  public void createRoleWithMode(String role, String roleMode) {
    String mode = "blacklist".equals(roleMode) ? "deny" : "allow";
    Permission permission = new Permission();
    permission.setRole(role);
    permission.setMethod("GET");
    permission.setPathPrefix("/api/dashboard");
    permission.setMode(mode);
    permission.setRoleMode(roleMode);
    permissionRepository.save(permission);
  }

  public Permission createPermission(String role, String method, String pathPrefix) {
    String roleMode = getRoleMode(role);
    String mode = "blacklist".equals(roleMode) ? "deny" : "allow";

    Permission permission = new Permission();
    permission.setRole(role);
    permission.setMethod(method);
    permission.setPathPrefix(pathPrefix);
    permission.setMode(mode);
    permission.setRoleMode(roleMode);
    return permissionRepository.save(permission);
  }

  public Permission update(Long id, Permission permission) {
    Permission existing = permissionRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Permission not found"));
    existing.setRole(permission.getRole());
    existing.setMethod(permission.getMethod());
    existing.setPathPrefix(permission.getPathPrefix());
    String roleMode = getRoleMode(permission.getRole());
    existing.setMode("blacklist".equals(roleMode) ? "deny" : "allow");
    existing.setRoleMode(roleMode);
    return permissionRepository.save(existing);
  }

  public void delete(Long id) {
    permissionRepository.deleteById(id);
  }

  private String getRoleMode(String role) {
    List<Permission> perms = permissionRepository.findByRole(role);
    if (perms.isEmpty()) return "whitelist";
    String mode = perms.get(0).getRoleMode();
    return mode != null ? mode : "whitelist";
  }

  public boolean isAllowed(String role, String method, String path) {
    if ("管理员".equals(role)) return true;

    String roleMode = getRoleMode(role);

    if ("blacklist".equals(roleMode)) {
      List<Permission> denyRules = permissionRepository.findByRoleAndMethodAndMode(role, method, "deny");
      for (Permission p : denyRules) {
        if (path.startsWith(p.getPathPrefix())) return false;
      }
      return true;
    }

    List<Permission> allowRules = permissionRepository.findByRoleAndMethodAndMode(role, method, "allow");
    for (Permission p : allowRules) {
      if (path.startsWith(p.getPathPrefix())) return true;
    }

    return false;
  }
}
