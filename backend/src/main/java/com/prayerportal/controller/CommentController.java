package com.prayerportal.controller;

import com.prayerportal.dto.CommentDto;
import com.prayerportal.dto.MessageResponse;
import com.prayerportal.model.*;
import com.prayerportal.repository.CommentRepository;
import com.prayerportal.repository.PrayerRequestRepository;
import com.prayerportal.repository.UserRepository;
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

import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/comments")
public class CommentController {
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private PrayerRequestRepository prayerRequestRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @GetMapping("/prayer-request/{prayerRequestId}")
    public ResponseEntity<Page<Comment>> getCommentsByPrayerRequest(
            @PathVariable Long prayerRequestId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<Comment> comments = commentRepository.findByPrayerRequestIdOrderByCreatedAtAsc(prayerRequestId, pageable);
        
        return ResponseEntity.ok(comments);
    }
    
    @PostMapping("/prayer-request/{prayerRequestId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> createComment(@PathVariable Long prayerRequestId,
                                         @Valid @RequestBody CommentDto commentDto,
                                         Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        Optional<PrayerRequest> prayerRequestOpt = prayerRequestRepository.findById(prayerRequestId);
        
        if (user == null || prayerRequestOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("User or Prayer Request not found"));
        }
        
        PrayerRequest prayerRequest = prayerRequestOpt.get();
        
        Comment comment = new Comment();
        comment.setContent(commentDto.getContent());
        comment.setAuthor(user);
        comment.setPrayerRequest(prayerRequest);
        
        Comment savedComment = commentRepository.save(comment);
        
        // Send notification to the prayer request author
        if (!prayerRequest.getAuthor().getId().equals(user.getId())) {
            String message = String.format("%s %s commented on your prayer request: %s", 
                                         user.getFirstName(), user.getLastName(), 
                                         prayerRequest.getTitle());
            notificationService.createNotification(prayerRequest.getAuthor(), message, 
                                                  NotificationType.COMMENT_RECEIVED, prayerRequest.getId());
        }
        
        return ResponseEntity.ok(savedComment);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateComment(@PathVariable Long id,
                                         @Valid @RequestBody CommentDto commentDto,
                                         Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<Comment> commentOpt = commentRepository.findById(id);
        
        if (commentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Comment comment = commentOpt.get();
        
        // Check if user owns the comment or is admin
        if (!comment.getAuthor().getId().equals(userDetails.getId()) && 
            !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.forbidden().build();
        }
        
        comment.setContent(commentDto.getContent());
        Comment updatedComment = commentRepository.save(comment);
        
        return ResponseEntity.ok(updatedComment);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteComment(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<Comment> commentOpt = commentRepository.findById(id);
        
        if (commentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Comment comment = commentOpt.get();
        
        // Check if user owns the comment or is admin
        if (!comment.getAuthor().getId().equals(userDetails.getId()) && 
            !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.forbidden().build();
        }
        
        commentRepository.delete(comment);
        return ResponseEntity.ok(new MessageResponse("Comment deleted successfully"));
    }
}