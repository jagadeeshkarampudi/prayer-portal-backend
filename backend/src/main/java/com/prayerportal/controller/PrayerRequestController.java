package com.prayerportal.controller;

import com.prayerportal.dto.MessageResponse;
import com.prayerportal.dto.PrayerRequestDto;
import com.prayerportal.model.*;
import com.prayerportal.repository.*;
import com.prayerportal.security.services.UserDetailsImpl;
import com.prayerportal.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/prayer-requests")
public class PrayerRequestController {
    @Autowired
    private PrayerRequestRepository prayerRequestRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private PrayerRepository prayerRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @GetMapping
    public ResponseEntity<Page<PrayerRequest>> getAllPrayerRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : 
                   Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<PrayerRequest> prayerRequests;
        
        if (search != null && !search.trim().isEmpty()) {
            prayerRequests = prayerRequestRepository.searchVisibleToUser(search, userDetails.getId(), pageable);
        } else {
            prayerRequests = prayerRequestRepository.findVisibleToUser(userDetails.getId(), pageable);
        }
        
        return ResponseEntity.ok(prayerRequests);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PrayerRequest> getPrayerRequestById(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<PrayerRequest> prayerRequest = prayerRequestRepository.findById(id);
        
        if (prayerRequest.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        PrayerRequest request = prayerRequest.get();
        
        // Check visibility permissions
        if (!canUserViewPrayerRequest(request, userDetails.getId())) {
            return ResponseEntity.forbidden().build();
        }
        
        return ResponseEntity.ok(request);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> createPrayerRequest(@Valid @RequestBody PrayerRequestDto prayerRequestDto,
                                               Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("User not found"));
        }
        
        PrayerRequest prayerRequest = new PrayerRequest();
        prayerRequest.setTitle(prayerRequestDto.getTitle());
        prayerRequest.setDescription(prayerRequestDto.getDescription());
        prayerRequest.setVisibility(prayerRequestDto.getVisibility());
        prayerRequest.setAnonymous(prayerRequestDto.isAnonymous());
        prayerRequest.setAuthor(user);
        
        // Handle group assignment
        if (prayerRequestDto.getGroupId() != null) {
            Optional<Group> group = groupRepository.findById(prayerRequestDto.getGroupId());
            if (group.isPresent() && group.get().getMembers().contains(user)) {
                prayerRequest.setGroup(group.get());
                prayerRequest.setVisibility(Visibility.GROUP_ONLY);
            }
        }
        
        PrayerRequest savedRequest = prayerRequestRepository.save(prayerRequest);
        return ResponseEntity.ok(savedRequest);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updatePrayerRequest(@PathVariable Long id,
                                               @Valid @RequestBody PrayerRequestDto prayerRequestDto,
                                               Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<PrayerRequest> prayerRequestOpt = prayerRequestRepository.findById(id);
        
        if (prayerRequestOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        PrayerRequest prayerRequest = prayerRequestOpt.get();
        
        // Check if user owns the prayer request or is admin
        if (!prayerRequest.getAuthor().getId().equals(userDetails.getId()) && 
            !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.forbidden().build();
        }
        
        prayerRequest.setTitle(prayerRequestDto.getTitle());
        prayerRequest.setDescription(prayerRequestDto.getDescription());
        prayerRequest.setVisibility(prayerRequestDto.getVisibility());
        prayerRequest.setAnonymous(prayerRequestDto.isAnonymous());
        prayerRequest.setUpdatedAt(LocalDateTime.now());
        
        PrayerRequest updatedRequest = prayerRequestRepository.save(prayerRequest);
        return ResponseEntity.ok(updatedRequest);
    }
    
    @PostMapping("/{id}/pray")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> prayForRequest(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        Optional<PrayerRequest> prayerRequestOpt = prayerRequestRepository.findById(id);
        
        if (user == null || prayerRequestOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("User or Prayer Request not found"));
        }
        
        PrayerRequest prayerRequest = prayerRequestOpt.get();
        
        // Check if user already prayed for this request
        if (prayerRepository.existsByUserAndPrayerRequest(user, prayerRequest)) {
            return ResponseEntity.badRequest().body(new MessageResponse("You have already prayed for this request"));
        }
        
        // Create prayer record
        Prayer prayer = new Prayer(user, prayerRequest);
        prayerRepository.save(prayer);
        
        // Update prayer count
        prayerRequest.setPrayedForCount(prayerRequest.getPrayedForCount() + 1);
        prayerRequestRepository.save(prayerRequest);
        
        // Send notification to the author
        if (!prayerRequest.getAuthor().getId().equals(user.getId())) {
            String message = String.format("%s %s prayed for your request: %s", 
                                         user.getFirstName(), user.getLastName(), 
                                         prayerRequest.getTitle());
            notificationService.createNotification(prayerRequest.getAuthor(), message, 
                                                  NotificationType.PRAYER_RECEIVED, prayerRequest.getId());
        }
        
        return ResponseEntity.ok(new MessageResponse("Prayer recorded successfully"));
    }
    
    @PostMapping("/{id}/answer")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> markAsAnswered(@PathVariable Long id,
                                          @RequestBody String answeredDescription,
                                          Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<PrayerRequest> prayerRequestOpt = prayerRequestRepository.findById(id);
        
        if (prayerRequestOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        PrayerRequest prayerRequest = prayerRequestOpt.get();
        
        // Check if user owns the prayer request or is admin
        if (!prayerRequest.getAuthor().getId().equals(userDetails.getId()) && 
            !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.forbidden().build();
        }
        
        prayerRequest.setAnswered(true);
        prayerRequest.setAnsweredDescription(answeredDescription);
        prayerRequest.setAnsweredAt(LocalDateTime.now());
        
        PrayerRequest updatedRequest = prayerRequestRepository.save(prayerRequest);
        return ResponseEntity.ok(updatedRequest);
    }
    
    @GetMapping("/my-requests")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<PrayerRequest>> getMyPrayerRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PrayerRequest> prayerRequests = prayerRequestRepository.findByAuthor(user, pageable);
        
        return ResponseEntity.ok(prayerRequests);
    }
    
    @GetMapping("/answered")
    public ResponseEntity<Page<PrayerRequest>> getAnsweredPrayers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("answeredAt").descending());
        Page<PrayerRequest> answeredPrayers = prayerRequestRepository.findByIsAnswered(true, pageable);
        
        return ResponseEntity.ok(answeredPrayers);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deletePrayerRequest(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<PrayerRequest> prayerRequestOpt = prayerRequestRepository.findById(id);
        
        if (prayerRequestOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        PrayerRequest prayerRequest = prayerRequestOpt.get();
        
        // Check if user owns the prayer request or is admin
        if (!prayerRequest.getAuthor().getId().equals(userDetails.getId()) && 
            !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.forbidden().build();
        }
        
        prayerRequestRepository.delete(prayerRequest);
        return ResponseEntity.ok(new MessageResponse("Prayer request deleted successfully"));
    }
    
    private boolean canUserViewPrayerRequest(PrayerRequest request, Long userId) {
        switch (request.getVisibility()) {
            case PUBLIC:
                return true;
            case PRIVATE:
                return request.getAuthor().getId().equals(userId);
            case GROUP_ONLY:
                return request.getGroup() != null && 
                       request.getGroup().getMembers().stream()
                           .anyMatch(member -> member.getId().equals(userId));
            case ADMIN_ONLY:
                // This would need admin role check, simplified for now
                return request.getAuthor().getId().equals(userId);
            default:
                return false;
        }
    }
}