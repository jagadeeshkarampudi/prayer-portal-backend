package com.prayerportal.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "prayers")
public class Prayer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private LocalDateTime prayedAt = LocalDateTime.now();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prayer_request_id")
    private PrayerRequest prayerRequest;
    
    // Constructors
    public Prayer() {}
    
    public Prayer(User user, PrayerRequest prayerRequest) {
        this.user = user;
        this.prayerRequest = prayerRequest;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalDateTime getPrayedAt() { return prayedAt; }
    public void setPrayedAt(LocalDateTime prayedAt) { this.prayedAt = prayedAt; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public PrayerRequest getPrayerRequest() { return prayerRequest; }
    public void setPrayerRequest(PrayerRequest prayerRequest) { this.prayerRequest = prayerRequest; }
}