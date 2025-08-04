package com.prayerportal.controller;

import com.prayerportal.model.Resource;
import com.prayerportal.model.ResourceType;
import com.prayerportal.repository.ResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/resources")
public class ResourceController {
    @Autowired
    private ResourceRepository resourceRepository;
    
    @GetMapping
    public ResponseEntity<Page<Resource>> getAllResources(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ResourceType type,
            @RequestParam(required = false) String search) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Page<Resource> resources;
        
        if (search != null && !search.trim().isEmpty()) {
            resources = resourceRepository.searchActiveResources(search, pageable);
        } else if (type != null) {
            resources = resourceRepository.findByTypeAndIsActiveTrue(type, pageable);
        } else {
            resources = resourceRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);
        }
        
        return ResponseEntity.ok(resources);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Resource> getResourceById(@PathVariable Long id) {
        Optional<Resource> resource = resourceRepository.findById(id);
        return resource.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/types")
    public ResponseEntity<ResourceType[]> getResourceTypes() {
        return ResponseEntity.ok(ResourceType.values());
    }
}