package com.society.management.service;

import com.society.management.entity.Notification;
import com.society.management.entity.User;
import com.society.management.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repo;

    public void notifyUser(User user, String title, String message, String category, String link) {
        repo.save(Notification.builder().user(user).title(title).message(message)
                .category(category).linkUrl(link).build());
    }

    public Page<Notification> forUser(Long userId, Pageable p) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId, p);
    }

    public long unread(Long userId) { return repo.countByUserIdAndReadFalse(userId); }

    public void markRead(Long id) {
        repo.findById(id).ifPresent(n -> { n.setRead(true); repo.save(n); });
    }
}
