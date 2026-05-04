package com.example.hr.model;

import jakarta.persistence.*;

@Entity
@Table(name = "organizations")
public class Organization {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(columnDefinition = "INTEGER")
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String type;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "parent_id")
  private Organization parent;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "grade_id")
  private Organization grade;

  @Column
  private Integer level;

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Organization getParent() {
    return parent;
  }

  public void setParent(Organization parent) {
    this.parent = parent;
  }

  public Organization getGrade() {
    return grade;
  }

  public void setGrade(Organization grade) {
    this.grade = grade;
  }

  public Integer getLevel() {
    return level;
  }

  public void setLevel(Integer level) {
    this.level = level;
  }
}
