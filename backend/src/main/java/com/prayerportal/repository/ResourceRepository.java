package com.prayerportal.repository;

import com.prayerportal.model.Resource;
import com.prayerportal.model.ResourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    Page<Resource> findByTypeAndIsActiveTrue(ResourceType type, Pageable pageable);
    
    Page<Resource> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT r FROM Resource r WHERE " +
           "r.isActive = true AND " +
           "(LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.content) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Resource> searchActiveResources(@Param("search") String search, Pageable pageable);
}