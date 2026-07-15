package com.society.management.controller;

import com.society.management.dto.request.VisitorRequest;
import com.society.management.entity.User;
import com.society.management.entity.Visitor;
import com.society.management.entity.VisitorApprovalStatus;
import com.society.management.exception.ApiException;
import com.society.management.repository.FlatRepository;
import com.society.management.repository.UserRepository;
import com.society.management.repository.VisitorRepository;
import com.society.management.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/visitors")
@RequiredArgsConstructor
public class VisitorController {

    private final VisitorRepository visitorRepo;
    private final FlatRepository flatRepo;
    private final UserRepository userRepo;

    @GetMapping
    public Page<Visitor> list(@RequestParam(required = false) Long flatId, Pageable p) {
        return flatId != null ? visitorRepo.findByFlatId(flatId, p) : visitorRepo.findAll(p);
    }

    @PostMapping
    public Visitor create(@Valid @RequestBody VisitorRequest r) {
        User me = userRepo.findById(SecurityUtils.currentUserId()).orElse(null);
        return visitorRepo.save(Visitor.builder()
                .visitorName(r.visitorName()).phone(r.phone()).purpose(r.purpose())
                .visitType(r.visitType()).vehicleNumber(r.vehicleNumber())
                .flat(r.flatId() != null ? flatRepo.findById(r.flatId()).orElse(null) : null)
                .addedBy(me).approvalStatus(VisitorApprovalStatus.PENDING)
                .build());
    }

    @PatchMapping("/{id}/approve")
    public Visitor approve(@PathVariable Long id, @RequestParam boolean approve) {
        Visitor v = visitorRepo.findById(id).orElseThrow(() -> ApiException.notFound("Visitor not found"));
        v.setApprovalStatus(approve ? VisitorApprovalStatus.APPROVED : VisitorApprovalStatus.REJECTED);
        v.setApprovedBy(userRepo.findById(SecurityUtils.currentUserId()).orElse(null));
        return visitorRepo.save(v);
    }

    @PatchMapping("/{id}/exit")
    public Visitor exit(@PathVariable Long id) {
        Visitor v = visitorRepo.findById(id).orElseThrow(() -> ApiException.notFound("Not found"));
        v.setExitTime(OffsetDateTime.now());
        return visitorRepo.save(v);
    }
}
