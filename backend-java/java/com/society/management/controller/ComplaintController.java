package com.society.management.controller;

import com.society.management.dto.request.ComplaintRequest;
import com.society.management.entity.Complaint;
import com.society.management.entity.ComplaintStatus;
import com.society.management.entity.Priority;
import com.society.management.entity.User;
import com.society.management.exception.ApiException;
import com.society.management.repository.ComplaintRepository;
import com.society.management.repository.FlatRepository;
import com.society.management.repository.UserRepository;
import com.society.management.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintRepository complaintRepo;
    private final UserRepository userRepo;
    private final FlatRepository flatRepo;

    @GetMapping
    public Page<Complaint> list(@RequestParam(required = false) ComplaintStatus status, Pageable p) {
        return status != null ? complaintRepo.findByStatus(status, p) : complaintRepo.findAll(p);
    }

    @GetMapping("/mine")
    public Page<Complaint> mine(Pageable p) {
        return complaintRepo.findByRaisedById(SecurityUtils.currentUserId(), p);
    }

    @PostMapping
    public Complaint create(@Valid @RequestBody ComplaintRequest r) {
        User me = userRepo.findById(SecurityUtils.currentUserId()).orElse(null);
        Complaint c = Complaint.builder()
                .ticketNumber("CMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .subject(r.subject()).description(r.description()).category(r.category())
                .priority(r.priority() != null ? Priority.valueOf(r.priority()) : Priority.MEDIUM)
                .status(ComplaintStatus.OPEN).raisedBy(me)
                .flat(r.flatId() != null ? flatRepo.findById(r.flatId()).orElse(null) : null)
                .build();
        return complaintRepo.save(c);
    }

    @PatchMapping("/{id}")
    public Complaint update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Complaint c = complaintRepo.findById(id).orElseThrow(() -> ApiException.notFound("Complaint not found"));
        if (body.get("status") != null) {
            ComplaintStatus st = ComplaintStatus.valueOf((String) body.get("status"));
            c.setStatus(st);
            if (st == ComplaintStatus.RESOLVED || st == ComplaintStatus.CLOSED)
                c.setResolvedAt(OffsetDateTime.now());
        }
        if (body.get("assignedTo") != null)
            c.setAssignedTo(userRepo.findById(Long.valueOf(body.get("assignedTo").toString())).orElse(null));
        if (body.get("rating") != null) c.setRating(Integer.valueOf(body.get("rating").toString()));
        if (body.get("feedback") != null) c.setFeedback(body.get("feedback").toString());
        return complaintRepo.save(c);
    }
}
