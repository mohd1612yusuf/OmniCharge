package com.omnicharge.operatorservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "operators")
public class Operator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    public Operator() {}

    public Operator(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
