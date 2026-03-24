package com.omnicharge.operatorservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "plans")
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private BigDecimal price;
    private Integer validityDays;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Operator operator;

    public Plan() {}

    public Plan(String name, BigDecimal price, Integer validityDays, String description, Operator operator) {
        this.name = name;
        this.price = price;
        this.validityDays = validityDays;
        this.description = description;
        this.operator = operator;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getValidityDays() { return validityDays; }
    public void setValidityDays(Integer validityDays) { this.validityDays = validityDays; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    @com.fasterxml.jackson.annotation.JsonIgnore
    public Operator getOperator() { return operator; }
    
    public void setOperator(Operator operator) { this.operator = operator; }
}
