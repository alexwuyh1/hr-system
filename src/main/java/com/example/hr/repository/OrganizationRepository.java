package com.example.hr.repository;

import com.example.hr.model.Organization;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
  List<Organization> findByType(String type);
  List<Organization> findByTypeAndName(String type, String name);
  boolean existsByTypeAndName(String type, String name);
}
