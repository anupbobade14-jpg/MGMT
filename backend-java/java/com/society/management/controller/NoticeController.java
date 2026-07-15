package com.society.management.controller;

import com.society.management.dto.request.NoticeRequest;
import com.society.management.entity.Notice;
import com.society.management.entity.Event;
import com.society.management.entity.User;
import com.society.management.exception.ApiException;
import com.society.management.repository.NoticeRepository;
import com.society.management.repository.EventRepository;
import com.society.management.repository.UserRepository;
import com.society.management.service.EmailService;
import com.society.management.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeRepository noticeRepo;
    private final EventRepository eventRepo;
    private final UserRepository userRepo;
    private final EmailService emailService;

    @GetMapping
    public List<Notice> list() { return noticeRepo.findAllByOrderByPinnedDescPublishedAtDesc(); }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SOCIETY_ADMIN','COMMITTEE')")
    public Notice create(@Valid @RequestBody NoticeRequest r) {
        User me = userRepo.findById(SecurityUtils.currentUserId()).orElse(null);
        Notice n = Notice.builder()
                .title(r.title()).body(r.body())
                .category(r.category() != null ? r.category() : "GENERAL")
                .pinned(r.pinned())
                .publishedBy(me).publishedAt(OffsetDateTime.now())
                .expiresAt(r.expiresAt() != null ? OffsetDateTime.parse(r.expiresAt()) : null)
                .build();
        n = noticeRepo.save(n);

        // Optionally mail all active owners (kept simple: broadcast placeholder)
        return n;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SOCIETY_ADMIN')")
    public void delete(@PathVariable Long id) { noticeRepo.deleteById(id); }

    /* Events */
    @GetMapping("/events")
    public List<Event> events() {
        return eventRepo.findByEventDateAfterOrderByEventDateAsc(OffsetDateTime.now());
    }

    @PostMapping("/events")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SOCIETY_ADMIN','COMMITTEE')")
    public Event createEvent(@RequestBody Event e) {
        e.setCreatedBy(userRepo.findById(SecurityUtils.currentUserId()).orElse(null));
        return eventRepo.save(e);
    }
}
