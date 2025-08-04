package com.prayerportal.service;

import com.prayerportal.model.Notification;
import com.prayerportal.model.NotificationType;
import com.prayerportal.model.User;
import com.prayerportal.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Async
    public void createNotification(User user, String message, NotificationType type, Long relatedEntityId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRelatedEntityId(relatedEntityId);
        
        notificationRepository.save(notification);
    }
    
    @Async
    public void createNotification(User user, String message, NotificationType type) {
        createNotification(user, message, type, null);
    }
}