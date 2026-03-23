package com.example.hr.model;

import jakarta.persistence.*;

/**
 * Position catalog.
 */
@Entity
@Table(name = "positions")
public class Position {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
