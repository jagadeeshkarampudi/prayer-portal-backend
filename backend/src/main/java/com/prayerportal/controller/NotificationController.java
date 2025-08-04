package com.prayerportal.controller;

import com.prayerportal.dto.MessageResponse;
import com.prayerportal.model.Notification;
import com.prayerportal.model.User;
import com.prayerportal.repository.NotificationRepository;
import com.prayerportal.repository.UserRepository;
import com.prayerportal.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<Notification>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        
        long unreadCount = notificationRepository.countByUserAndIsReadFalse(user);
        return ResponseEntity.ok(unreadCount);
    }
    
    @PatchMapping("/{id}/mark-read")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<Notification> notificationOpt = notificationRepository.findById(id);
        
        if (notificationOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Notification notification = notificationOpt.get();
        
        // Check if user owns the notification
        if (!notification.getUser().getId().equals(userDetails.getId())) {
            return ResponseEntity.forbidden().build();
        }
        
        notification.setRead(true);
        notificationRepository.save(notification);
        
        return ResponseEntity.ok(new MessageResponse("Notification marked as read"));
    }
    
    @PatchMapping("/mark-all-read")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> markAllAsRead(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Page<Notification> unreadNotifications = notificationRepository.findByUserOrderByCreatedAtDesc(user, 
                                                                        PageRequest.of(0, Integer.MAX_VALUE));
        
        unreadNotifications.getContent().forEach(notification -> {
            if (!notification.isRead()) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        });
        
        return ResponseEntity.ok(new MessageResponse("All notifications marked as read"));
    }
    
    @DeleteMapping("/clear-read")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> clearReadNotifications(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        
        notificationRepository.deleteByUserAndIsReadTrue(user);
        return ResponseEntity.ok(new MessageResponse("Read notifications cleared"));
    }
}