package com.prayerportal.repository;

import com.prayerportal.model.PrayerRequest;
import com.prayerportal.model.User;
import com.prayerportal.model.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PrayerRequestRepository extends JpaRepository<PrayerRequest, Long> {
    Page<PrayerRequest> findByAuthor(User author, Pageable pageable);
    
    Page<PrayerRequest> findByVisibility(Visibility visibility, Pageable pageable);
    
    Page<PrayerRequest> findByIsAnswered(boolean isAnswered, Pageable pageable);
    
    @Query("SELECT pr FROM PrayerRequest pr WHERE " +
           "pr.visibility = 'PUBLIC' OR " +
           "(pr.visibility = 'GROUP_ONLY' AND pr.group.id IN " +
           "(SELECT g.id FROM Group g JOIN g.members m WHERE m.id = :userId)) " +
           "ORDER BY pr.createdAt DESC")
    Page<PrayerRequest> findVisibleToUser(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT pr FROM PrayerRequest pr WHERE " +
           "pr.group.id = :groupId " +
           "ORDER BY pr.createdAt DESC")
    Page<PrayerRequest> findByGroupId(@Param("groupId") Long groupId, Pageable pageable);
    
    @Query("SELECT pr FROM PrayerRequest pr WHERE " +
           "(LOWER(pr.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(pr.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(pr.visibility = 'PUBLIC' OR " +
           "(pr.visibility = 'GROUP_ONLY' AND pr.group.id IN " +
           "(SELECT g.id FROM Group g JOIN g.members m WHERE m.id = :userId)))")
    Page<PrayerRequest> searchVisibleToUser(@Param("search") String search, 
                                          @Param("userId") Long userId, 
                                          Pageable pageable);
    
    long countByIsAnsweredFalse();
    
    long countByCreatedAtAfter(LocalDateTime date);
    
    @Query("SELECT COUNT(pr) FROM PrayerRequest pr WHERE pr.visibility = 'PUBLIC'")
    long countPublicRequests();
}