package com.medicare.service;

import com.medicare.entity.Notification;
import com.medicare.entity.User;
import com.medicare.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationService {

    @Autowired private NotificationRepository notificationRepository;

    public void send(User user, String message, String type) {
        Notification n = new Notification();
        n.setUser(user);
        n.setMessage(message);
        n.setType(type);
        notificationRepository.save(n);
    }

    public List<Notification> getAll(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public long countUnread(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    public void markAllRead(User user) {
        List<Notification> unread = notificationRepository.findByUserAndIsReadFalse(user);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
