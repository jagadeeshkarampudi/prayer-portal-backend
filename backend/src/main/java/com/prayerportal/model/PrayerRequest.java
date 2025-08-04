package com.prayerportal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "prayer_requests")
public class PrayerRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 200)
    private String title;
    
    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    private Visibility visibility = Visibility.PUBLIC;
    
    private boolean isAnonymous = false;
    
    private boolean isAnswered = false;
    
    @Column(columnDefinition = "TEXT")
    private String answeredDescription;
    
    private LocalDateTime answeredAt;
    
    private int prayedForCount = 0;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;
    
    @OneToMany(mappedBy = "prayerRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Comment> comments = new HashSet<>();
    
    @OneToMany(mappedBy = "prayerRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Prayer> prayers = new HashSet<>();
    
    // Constructors
    public PrayerRequest() {}
    
    public PrayerRequest(String title, String description, User author) {
        this.title = title;
        this.description = description;
        this.author = author;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }
    
    public boolean isAnonymous() { return isAnonymous; }
    public void setAnonymous(boolean anonymous) { isAnonymous = anonymous; }
    
    public boolean isAnswered() { return isAnswered; }
    public void setAnswered(boolean answered) { isAnswered = answered; }
    
    public String getAnsweredDescription() { return answeredDescription; }
    public void setAnsweredDescription(String answeredDescription) { this.answeredDescription = answeredDescription; }
    
    public LocalDateTime getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(LocalDateTime answeredAt) { this.answeredAt = answeredAt; }
    
    public int getPrayedForCount() { return prayedForCount; }
    public void setPrayedForCount(int prayedForCount) { this.prayedForCount = prayedForCount; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    
    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }
    
    public Set<Comment> getComments() { return comments; }
    public void setComments(Set<Comment> comments) { this.comments = comments; }
    
    public Set<Prayer> getPrayers() { return prayers; }
    public void setPrayers(Set<Prayer> prayers) { this.prayers = prayers; }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}