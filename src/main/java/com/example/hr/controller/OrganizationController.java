package com.example.hr.controller;

import com.example.hr.model.Organization;
import com.example.hr.repository.OrganizationRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {
  private final OrganizationRepository organizationRepository;

  public OrganizationController(OrganizationRepository organizationRepository) {
    this.organizationRepository = organizationRepository;
  }

  @GetMapping
  public List<Organization> list() {
    return organizationRepository.findAll();
  }

  @GetMapping("/tree")
  public List<OrganizationNode> tree() {
    List<Organization> all = organizationRepository.findByType("部门");
    return buildTree(all, null);
  }

  @GetMapping("/by-type")
  public List<Organization> getByType(@RequestParam String type) {
    return organizationRepository.findByType(type);
  }

  @PostMapping
  public Organization create(@Valid @RequestBody OrganizationRequest request) {
    Organization org = new Organization();
    org.setName(request.name);
    org.setType(request.type);
    org.setLevel(request.level);
    if ("部门".equals(request.type) && request.parentId != null) {
      org.setParent(organizationRepository.findById(request.parentId)
          .orElseThrow(() -> new IllegalArgumentException("父组织不存在")));
    }
    return organizationRepository.save(org);
  }

  @PutMapping("/{id}")
  public Organization update(@PathVariable("id") Long id, @Valid @RequestBody OrganizationRequest request) {
    Organization org = organizationRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("组织不存在"));
    org.setName(request.name);
    org.setType(request.type);
    org.setLevel(request.level);
    if ("部门".equals(request.type) && request.parentId != null) {
      org.setParent(organizationRepository.findById(request.parentId)
          .orElseThrow(() -> new IllegalArgumentException("父组织不存在")));
    } else {
      org.setParent(null);
    }
    return organizationRepository.save(org);
  }

  @DeleteMapping("/{id}")
  public Map<String, String> delete(@PathVariable("id") Long id) {
    organizationRepository.deleteById(id);
    return Map.of("message", "已删除");
  }

  private List<OrganizationNode> buildTree(List<Organization> orgs, Long parentId) {
    return orgs.stream()
        .filter(o -> (o.getParent() == null && parentId == null) || (o.getParent() != null && o.getParent().getId().equals(parentId)))
        .map(o -> {
          OrganizationNode node = new OrganizationNode();
          node.id = o.getId();
          node.name = o.getName();
          node.children = buildTree(orgs, o.getId());
          return node;
        })
        .toList();
  }

  public static class OrganizationNode {
    public Long id;
    public String name;
    public List<OrganizationNode> children;
  }

  public static class OrganizationRequest {
    @NotBlank public String name;
    @NotBlank public String type;
    public Long parentId;
    public Integer level;
  }
}
