package com.example.hr.service;

import com.example.hr.controller.OrganizationController.OrganizationNode;
import com.example.hr.controller.OrganizationController.OrganizationRequest;
import com.example.hr.model.Organization;
import com.example.hr.repository.OrganizationRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrganizationService {
    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public List<Organization> list() {
        return organizationRepository.findAll();
    }

    public List<Organization> listResponses() {
        List<Organization> orgs = list();
        for (Organization org : orgs) {
            if (org.getParent() != null) org.getParent().getId();
            if (org.getGrade() != null) org.getGrade().getId();
        }
        return orgs;
    }

    public List<OrganizationNode> buildPositionTree() {
        List<Organization> depts = organizationRepository.findByType("部门");
        List<Organization> positions = organizationRepository.findByType("岗位");
        return doBuildPositionTree(depts, positions, null);
    }

    private List<OrganizationNode> doBuildPositionTree(List<Organization> depts, List<Organization> positions, Long parentId) {
        return depts.stream()
            .filter(d -> (d.getParent() == null && parentId == null)
                || (d.getParent() != null && d.getParent().getId().equals(parentId)))
            .map(d -> {
                OrganizationNode node = new OrganizationNode();
                node.id = d.getId();
                node.name = d.getName();
                List<OrganizationNode> positionNodes = positions.stream()
                    .filter(p -> p.getParent() != null && p.getParent().getId().equals(d.getId()))
                    .map(p -> {
                        OrganizationNode pNode = new OrganizationNode();
                        pNode.id = p.getId();
                        pNode.name = p.getName() + (p.getGrade() != null ? " (" + p.getGrade().getName() + ")" : "");
                        pNode.children = List.of();
                        return pNode;
                    })
                    .toList();
                node.children = java.util.stream.Stream.concat(
                    doBuildPositionTree(depts, positions, d.getId()).stream(),
                    positionNodes.stream()
                ).toList();
                return node;
            })
            .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public Organization create(OrganizationRequest request) {
        Organization org = new Organization();
        org.setName(request.name);
        org.setType(request.type);
        applyRelations(org, request);
        return organizationRepository.save(org);
    }

    @Transactional(rollbackFor = Exception.class)
    public Organization update(Long id, OrganizationRequest request) {
        Organization org = organizationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("组织不存在"));
        org.setName(request.name);
        org.setType(request.type);
        applyRelations(org, request);
        return organizationRepository.save(org);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> delete(Long id) {
        organizationRepository.deleteById(id);
        return Map.of("message", "已删除");
    }

    private void applyRelations(Organization org, OrganizationRequest request) {
        if ("部门".equals(request.type)) {
            if (request.parentId != null) {
                Organization parent = organizationRepository.findById(request.parentId)
                    .orElseThrow(() -> new IllegalArgumentException("父组织不存在"));
                if (!"部门".equals(parent.getType())) {
                    throw new IllegalArgumentException("父组织必须是部门类型");
                }
                org.setParent(parent);
            } else {
                org.setParent(null);
            }
            org.setLevel(null);
            org.setGrade(null);
        } else if ("岗位".equals(request.type)) {
            org.setLevel(null);
            if (request.parentId == null) {
                throw new IllegalArgumentException("岗位必须关联部门");
            }
            Organization dept = organizationRepository.findById(request.parentId)
                .orElseThrow(() -> new IllegalArgumentException("部门不存在"));
            if (!"部门".equals(dept.getType())) {
                throw new IllegalArgumentException("岗位所属的必须是部门类型");
            }
            org.setParent(dept);
            if (request.gradeId == null) {
                throw new IllegalArgumentException("岗位必须关联职级");
            }
            Organization grade = organizationRepository.findById(request.gradeId)
                .orElseThrow(() -> new IllegalArgumentException("职级不存在"));
            if (!"职级".equals(grade.getType())) {
                throw new IllegalArgumentException("关联的必须是职级类型");
            }
            org.setGrade(grade);
        } else if ("职级".equals(request.type)) {
            org.setParent(null);
            org.setGrade(null);
            if (request.level == null) {
                throw new IllegalArgumentException("职级等级不能为空");
            }
            org.setLevel(request.level);
        }
    }
}
