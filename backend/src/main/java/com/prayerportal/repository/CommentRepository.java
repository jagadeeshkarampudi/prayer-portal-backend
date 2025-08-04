package com.prayerportal.repository;

import com.prayerportal.model.Comment;
import com.prayerportal.model.PrayerRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByPrayerRequest(PrayerRequest prayerRequest, Pageable pageable);
    
    Page<Comment> findByPrayerRequestIdOrderByCreatedAtAsc(Long prayerRequestId, Pageable pageable);
    
    long countByPrayerRequest(PrayerRequest prayerRequest);
}