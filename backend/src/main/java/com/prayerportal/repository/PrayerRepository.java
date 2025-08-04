package com.prayerportal.repository;

import com.prayerportal.model.Prayer;
import com.prayerportal.model.PrayerRequest;
import com.prayerportal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrayerRepository extends JpaRepository<Prayer, Long> {
    Optional<Prayer> findByUserAndPrayerRequest(User user, PrayerRequest prayerRequest);
    
    long countByPrayerRequest(PrayerRequest prayerRequest);
    
    boolean existsByUserAndPrayerRequest(User user, PrayerRequest prayerRequest);
}