package com.prayerportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GroupDto {
    @NotBlank
    @Size(max = 100)
    private String name;
    
    private String description;
    
    // Constructors
    public GroupDto() {}
    
    public GroupDto(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}