package com.appgestor.dto;

import java.math.BigDecimal;

import com.appgestor.models.Category;

public class MovementDTO {

    private String description;
    private BigDecimal amount;
    private Category category;
    private String date;

    // Getters and Setters
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}