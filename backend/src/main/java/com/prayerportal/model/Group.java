package com.prayerportal.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 100)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    private User leader;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "group_members",
        joinColumns = @JoinColumn(name = "group_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();
    
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<PrayerRequest> prayerRequests = new HashSet<>();
    
    // Constructors
    public Group() {}
    
    public Group(String name, String description, User leader) {
        this.name = name;
        this.description = description;
        this.leader = leader;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public User getLeader() { return leader; }
    public void setLeader(User leader) { this.leader = leader; }
    
    public Set<User> getMembers() { return members; }
    public void setMembers(Set<User> members) { this.members = members; }
    
    public Set<PrayerRequest> getPrayerRequests() { return prayerRequests; }
    public void setPrayerRequests(Set<PrayerRequest> prayerRequests) { this.prayerRequests = prayerRequests; }
    
    // Helper methods
    public void addMember(User user) {
        this.members.add(user);
        user.getGroups().add(this);
    }
    
    public void removeMember(User user) {
        this.members.remove(user);
        user.getGroups().remove(this);
    }
}