package com.example.hr.controller;

import com.example.hr.model.Organization;
import com.example.hr.service.OrganizationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {
  private final OrganizationService organizationService;

  public OrganizationController(OrganizationService organizationService) {
    this.organizationService = organizationService;
  }

  @GetMapping
  public List<Organization> list() {
    return organizationService.listResponses();
  }

  @GetMapping("/position-tree")
  public List<OrganizationNode> positionTree() {
    return organizationService.buildPositionTree();
  }

  @PostMapping
  public Organization create(@Valid @RequestBody OrganizationRequest request) {
    return organizationService.create(request);
  }

  @PutMapping("/{id}")
  public Organization update(@PathVariable("id") Long id, @Valid @RequestBody OrganizationRequest request) {
    return organizationService.update(id, request);
  }

  @DeleteMapping("/{id}")
  public Map<String, String> delete(@PathVariable("id") Long id) {
    return organizationService.delete(id);
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
    public Long gradeId;
    public Integer level;
  }
}
