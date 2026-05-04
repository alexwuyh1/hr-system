package com.example.hr.service;

import com.example.hr.model.Permission;
import com.example.hr.model.RoleConfig;
import com.example.hr.repository.PermissionRepository;
import com.example.hr.repository.RoleConfigRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {
  private final PermissionRepository permissionRepository;
  private final RoleConfigRepository roleConfigRepository;

  public PermissionService(
      PermissionRepository permissionRepository,
      RoleConfigRepository roleConfigRepository) {
    this.permissionRepository = permissionRepository;
    this.roleConfigRepository = roleConfigRepository;
  }

  public List<Permission> list() {
    return permissionRepository.findAll();
  }

  public List<Map<String, String>> listRolesWithMode() {
    return roleConfigRepository.findAll().stream()
        .filter(rc -> !"管理员".equals(rc.getRole()))
        .map(rc -> Map.of(
            "name", rc.getRole(),
            "role", rc.getRole(),
            "roleMode", rc.getRoleMode()
        ))
        .collect(Collectors.toList());
  }

  public void createRoleWithMode(String role, String roleMode) {
    if (!"whitelist".equals(roleMode) && !"blacklist".equals(roleMode)) {
      throw new IllegalArgumentException("权限模式必须是 whitelist 或 blacklist");
    }
    if (roleConfigRepository.existsByRole(role)) {
      throw new IllegalArgumentException("角色已存在");
    }
    RoleConfig rc = new RoleConfig();
    rc.setRole(role);
    rc.setRoleMode(roleMode);
    roleConfigRepository.save(rc);
  }

  public Permission createPermission(String role, String method, String pathPrefix) {
    RoleConfig rc = roleConfigRepository.findByRole(role)
        .orElseThrow(() -> new IllegalArgumentException("角色不存在，请先创建角色"));

    if (permissionRepository.existsByRoleAndMethodAndPathPrefix(role, method, pathPrefix)) {
      throw new IllegalArgumentException("权限规则已存在");
    }

    Permission permission = new Permission();
    permission.setRole(role);
    permission.setMethod(method);
    permission.setPathPrefix(pathPrefix);
    return permissionRepository.save(permission);
  }

  public Permission update(Long id, String role, String method, String pathPrefix) {
    Permission existing = permissionRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("权限规则不存在"));

    RoleConfig rc = roleConfigRepository.findByRole(role)
        .orElseThrow(() -> new IllegalArgumentException("角色不存在"));

    existing.setRole(role);
    existing.setMethod(method);
    existing.setPathPrefix(pathPrefix);
    return permissionRepository.save(existing);
  }

  public void delete(Long id) {
    permissionRepository.deleteById(id);
  }

  public void deleteRole(String role) {
    List<Permission> perms = permissionRepository.findByRole(role);
    permissionRepository.deleteAll(perms);
    roleConfigRepository.findByRole(role).ifPresent(roleConfigRepository::delete);
  }

  public String getRoleMode(String role) {
    return roleConfigRepository.findByRole(role)
        .map(RoleConfig::getRoleMode)
        .orElse("whitelist");
  }

  public boolean isAllowed(String role, String method, String path) {
    if ("管理员".equals(role)) return true;

    String roleMode = getRoleMode(role);

    if ("blacklist".equals(roleMode)) {
      List<Permission> rules = permissionRepository.findByRoleAndMethod(role, method);
      for (Permission p : rules) {
        if (path.startsWith(p.getPathPrefix())) return false;
      }
      return true;
    }

    List<Permission> rules = permissionRepository.findByRoleAndMethod(role, method);
    for (Permission p : rules) {
      if (path.startsWith(p.getPathPrefix())) return true;
    }

    return false;
  }
}
