package com.prayerportal.controller;

import com.prayerportal.dto.MessageResponse;
import com.prayerportal.model.User;
import com.prayerportal.repository.UserRepository;
import com.prayerportal.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder encoder;
    
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<User> getUserProfile(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<User> user = userRepository.findById(userDetails.getId());
        
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateUserProfile(@RequestBody User userData, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findById(userDetails.getId());
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        
        // Update allowed fields
        user.setFirstName(userData.getFirstName());
        user.setLastName(userData.getLastName());
        user.setBio(userData.getBio());
        
        // Check if email is being changed and if it's unique
        if (!user.getEmail().equals(userData.getEmail())) {
            if (userRepository.existsByEmail(userData.getEmail())) {
                return ResponseEntity.badRequest().body(new MessageResponse("Email is already in use!"));
            }
            user.setEmail(userData.getEmail());
        }
        
        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(updatedUser);
    }
    
    @PatchMapping("/change-password")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request, 
                                          Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findById(userDetails.getId());
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = userOpt.get();
        
        // Verify current password
        if (!encoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Current password is incorrect"));
        }
        
        // Update password
        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    // Inner class for password change request
    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;
        
        // Getters and Setters
        public String getCurrentPassword() { return currentPassword; }
        public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
        
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}