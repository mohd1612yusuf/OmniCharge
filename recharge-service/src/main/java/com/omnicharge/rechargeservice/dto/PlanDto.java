package com.omnicharge.rechargeservice.dto;

import java.math.BigDecimal;

public class PlanDto {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer validityDays;
    private String description;

    public PlanDto() {}

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
}
