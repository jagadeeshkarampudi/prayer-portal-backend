package com.prayerportal.controller;

import com.prayerportal.dto.MessageResponse;
import com.prayerportal.model.*;
import com.prayerportal.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PrayerRequestRepository prayerRequestRepository;
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private ResourceRepository resourceRepository;
    
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        // User statistics
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByEnabledTrue();
        
        // Prayer request statistics
        long totalPrayerRequests = prayerRequestRepository.count();
        long activePrayerRequests = prayerRequestRepository.countByIsAnsweredFalse();
        long publicPrayerRequests = prayerRequestRepository.countPublicRequests();
        
        // Recent activity (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long recentPrayerRequests = prayerRequestRepository.countByCreatedAtAfter(thirtyDaysAgo);
        
        // Group statistics
        long totalGroups = groupRepository.count();
        
        analytics.put("totalUsers", totalUsers);
        analytics.put("activeUsers", activeUsers);
        analytics.put("totalPrayerRequests", totalPrayerRequests);
        analytics.put("activePrayerRequests", activePrayerRequests);
        analytics.put("publicPrayerRequests", publicPrayerRequests);
        analytics.put("recentPrayerRequests", recentPrayerRequests);
        analytics.put("totalGroups", totalGroups);
        
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/users")
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<User> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userRepository.findBySearchTerm(search, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        
        return ResponseEntity.ok(users);
    }
    
    @PatchMapping("/users/{id}/toggle-status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        
        String status = user.isEnabled() ? "enabled" : "disabled";
        return ResponseEntity.ok(new MessageResponse("User " + status + " successfully"));
    }
    
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Role role) {
        Optional<User> userOpt = userRepository.findById(id);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        user.setRole(role);
        userRepository.save(user);
        
        return ResponseEntity.ok(new MessageResponse("User role updated successfully"));
    }
    
    @GetMapping("/prayer-requests")
    public ResponseEntity<Page<PrayerRequest>> getAllPrayerRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String visibility) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<PrayerRequest> prayerRequests;
        if (visibility != null) {
            Visibility vis = Visibility.valueOf(visibility.toUpperCase());
            prayerRequests = prayerRequestRepository.findByVisibility(vis, pageable);
        } else {
            prayerRequests = prayerRequestRepository.findAll(pageable);
        }
        
        return ResponseEntity.ok(prayerRequests);
    }
    
    @DeleteMapping("/prayer-requests/{id}")
    public ResponseEntity<?> deletePrayerRequest(@PathVariable Long id) {
        Optional<PrayerRequest> prayerRequestOpt = prayerRequestRepository.findById(id);
        
        if (prayerRequestOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        prayerRequestRepository.delete(prayerRequestOpt.get());
        return ResponseEntity.ok(new MessageResponse("Prayer request deleted successfully"));
    }
    
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Long id) {
        Optional<Comment> commentOpt = commentRepository.findById(id);
        
        if (commentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        commentRepository.delete(commentOpt.get());
        return ResponseEntity.ok(new MessageResponse("Comment deleted successfully"));
    }
    
    @PostMapping("/resources")
    public ResponseEntity<?> createResource(@RequestBody Resource resource) {
        Resource savedResource = resourceRepository.save(resource);
        return ResponseEntity.ok(savedResource);
    }
    
    @PutMapping("/resources/{id}")
    public ResponseEntity<?> updateResource(@PathVariable Long id, @RequestBody Resource resourceData) {
        Optional<Resource> resourceOpt = resourceRepository.findById(id);
        
        if (resourceOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Resource resource = resourceOpt.get();
        resource.setTitle(resourceData.getTitle());
        resource.setContent(resourceData.getContent());
        resource.setType(resourceData.getType());
        resource.setAuthor(resourceData.getAuthor());
        resource.setActive(resourceData.isActive());
        
        Resource updatedResource = resourceRepository.save(resource);
        return ResponseEntity.ok(updatedResource);
    }
    
    @DeleteMapping("/resources/{id}")
    public ResponseEntity<?> deleteResource(@PathVariable Long id) {
        Optional<Resource> resourceOpt = resourceRepository.findById(id);
        
        if (resourceOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        resourceRepository.delete(resourceOpt.get());
        return ResponseEntity.ok(new MessageResponse("Resource deleted successfully"));
    }
    
    @GetMapping("/resources")
    public ResponseEntity<Page<Resource>> getAllResourcesAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Resource> resources = resourceRepository.findAll(pageable);
        
        return ResponseEntity.ok(resources);
    }
}