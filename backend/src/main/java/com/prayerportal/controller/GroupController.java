package com.prayerportal.controller;

import com.prayerportal.dto.GroupDto;
import com.prayerportal.dto.MessageResponse;
import com.prayerportal.model.Group;
import com.prayerportal.model.PrayerRequest;
import com.prayerportal.model.User;
import com.prayerportal.repository.GroupRepository;
import com.prayerportal.repository.PrayerRequestRepository;
import com.prayerportal.repository.UserRepository;
import com.prayerportal.security.services.UserDetailsImpl;
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
@RequestMapping("/groups")
public class GroupController {
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PrayerRequestRepository prayerRequestRepository;
    
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<Group>> getAllGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<Group> groups;
        if (search != null && !search.trim().isEmpty()) {
            groups = groupRepository.findBySearchTerm(search, pageable);
        } else {
            groups = groupRepository.findAll(pageable);
        }
        
        return ResponseEntity.ok(groups);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Group> getGroupById(@PathVariable Long id) {
        Optional<Group> group = groupRepository.findById(id);
        return group.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/my-groups")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<Group>> getMyGroups(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Group> groups = groupRepository.findByMemberId(userDetails.getId(), pageable);
        
        return ResponseEntity.ok(groups);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> createGroup(@Valid @RequestBody GroupDto groupDto,
                                       Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest().body(new MessageResponse("User not found"));
        }
        
        if (groupRepository.existsByName(groupDto.getName())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Group name already exists"));
        }
        
        Group group = new Group();
        group.setName(groupDto.getName());
        group.setDescription(groupDto.getDescription());
        group.setLeader(user);
        
        // Add creator as a member
        group.addMember(user);
        
        Group savedGroup = groupRepository.save(group);
        return ResponseEntity.ok(savedGroup);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateGroup(@PathVariable Long id,
                                       @Valid @RequestBody GroupDto groupDto,
                                       Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<Group> groupOpt = groupRepository.findById(id);
        
        if (groupOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Group group = groupOpt.get();
        
        // Check if user is the leader or admin
        if (!group.getLeader().getId().equals(userDetails.getId()) && 
            !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.forbidden().build();
        }
        
        group.setName(groupDto.getName());
        group.setDescription(groupDto.getDescription());
        
        Group updatedGroup = groupRepository.save(group);
        return ResponseEntity.ok(updatedGroup);
    }
    
    @PostMapping("/{id}/join")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> joinGroup(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        Optional<Group> groupOpt = groupRepository.findById(id);
        
        if (user == null || groupOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("User or Group not found"));
        }
        
        Group group = groupOpt.get();
        
        if (group.getMembers().contains(user)) {
            return ResponseEntity.badRequest().body(new MessageResponse("User is already a member of this group"));
        }
        
        group.addMember(user);
        groupRepository.save(group);
        
        return ResponseEntity.ok(new MessageResponse("Successfully joined the group"));
    }
    
    @PostMapping("/{id}/leave")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> leaveGroup(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElse(null);
        Optional<Group> groupOpt = groupRepository.findById(id);
        
        if (user == null || groupOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("User or Group not found"));
        }
        
        Group group = groupOpt.get();
        
        if (!group.getMembers().contains(user)) {
            return ResponseEntity.badRequest().body(new MessageResponse("User is not a member of this group"));
        }
        
        // Don't allow leader to leave unless they transfer leadership
        if (group.getLeader().getId().equals(user.getId())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Group leader cannot leave. Please transfer leadership first."));
        }
        
        group.removeMember(user);
        groupRepository.save(group);
        
        return ResponseEntity.ok(new MessageResponse("Successfully left the group"));
    }
    
    @GetMapping("/{id}/prayers")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<PrayerRequest>> getGroupPrayers(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<Group> groupOpt = groupRepository.findById(id);
        
        if (groupOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Group group = groupOpt.get();
        
        // Check if user is a member of the group
        if (!group.getMembers().stream().anyMatch(member -> member.getId().equals(userDetails.getId()))) {
            return ResponseEntity.forbidden().build();
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PrayerRequest> prayerRequests = prayerRequestRepository.findByGroupId(id, pageable);
        
        return ResponseEntity.ok(prayerRequests);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteGroup(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Optional<Group> groupOpt = groupRepository.findById(id);
        
        if (groupOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Group group = groupOpt.get();
        
        // Check if user is the leader or admin
        if (!group.getLeader().getId().equals(userDetails.getId()) && 
            !userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.forbidden().build();
        }
        
        groupRepository.delete(group);
        return ResponseEntity.ok(new MessageResponse("Group deleted successfully"));
    }
}