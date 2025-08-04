package com.prayerportal.dto;

import com.prayerportal.model.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PrayerRequestDto {
    @NotBlank
    @Size(max = 200)
    private String title;
    
    @NotBlank
    private String description;
    
    private Visibility visibility = Visibility.PUBLIC;
    
    private boolean isAnonymous = false;
    
    private Long groupId;
    
    // Constructors
    public PrayerRequestDto() {}
    
    public PrayerRequestDto(String title, String description) {
        this.title = title;
        this.description = description;
    }
    
    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }
    
    public boolean isAnonymous() { return isAnonymous; }
    public void setAnonymous(boolean anonymous) { isAnonymous = anonymous; }
    
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
}