package com.society.management.controller;

import com.society.management.entity.Notification;
import com.society.management.service.NotificationService;
import com.society.management.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService svc;

    @GetMapping
    public Page<Notification> list(Pageable p) { return svc.forUser(SecurityUtils.currentUserId(), p); }

    @GetMapping("/unread-count")
    public Map<String, Long> unread() {
        return Map.of("count", svc.unread(SecurityUtils.currentUserId()));
    }

    @PatchMapping("/{id}/read")
    public void read(@PathVariable Long id) { svc.markRead(id); }
}
