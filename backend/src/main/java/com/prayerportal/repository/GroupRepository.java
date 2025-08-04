package com.prayerportal.repository;

import com.prayerportal.model.Group;
import com.prayerportal.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    Page<Group> findByLeader(User leader, Pageable pageable);
    
    @Query("SELECT g FROM Group g JOIN g.members m WHERE m.id = :userId")
    Page<Group> findByMemberId(@Param("userId") Long userId, Pageable pageable);
    
    @Query("SELECT g FROM Group g WHERE " +
           "LOWER(g.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(g.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Group> findBySearchTerm(@Param("search") String search, Pageable pageable);
    
    boolean existsByName(String name);
}