package com.example.hr.model;

import jakarta.persistence.*;

/**
 * Department entity with self-referenced parent for tree structure.
 */
@Entity
@Table(name = "departments")
public class Department {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "parent_id")
  private Department parent;

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Department getParent() {
    return parent;
  }

  public void setParent(Department parent) {
    this.parent = parent;
  }
}
