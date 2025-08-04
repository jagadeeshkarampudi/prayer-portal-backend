package com.prayerportal.dto;

import jakarta.validation.constraints.NotBlank;

public class CommentDto {
    @NotBlank
    private String content;
    
    // Constructors
    public CommentDto() {}
    
    public CommentDto(String content) {
        this.content = content;
    }
    
    // Getters and Setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}