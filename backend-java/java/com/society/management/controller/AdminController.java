package com.society.management.controller;

import com.society.management.entity.*;
import com.society.management.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN','SOCIETY_ADMIN')")
public class AdminController {

    private final UserRepository userRepo;
    private final EmailTemplateRepository templateRepo;
    private final AuditLogRepository auditRepo;
    private final LoginHistoryRepository loginRepo;
    private final AppSettingRepository settingRepo;

    /* --- Users --- */
    @GetMapping("/users")
    public List<User> users() { return userRepo.findAll(); }

    @PatchMapping("/users/{id}/role")
    public User setRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User u = userRepo.findById(id).orElseThrow();
        u.setRole(Role.valueOf(body.get("role")));
        return userRepo.save(u);
    }

    @PatchMapping("/users/{id}/active")
    public User activate(@PathVariable Long id, @RequestParam boolean active) {
        User u = userRepo.findById(id).orElseThrow();
        u.setActive(active);
        return userRepo.save(u);
    }

    /* --- Email templates --- */
    @GetMapping("/email-templates")
    public List<EmailTemplate> templates() { return templateRepo.findAll(); }

    @PutMapping("/email-templates/{id}")
    public EmailTemplate updateTemplate(@PathVariable Long id, @RequestBody EmailTemplate t) {
        EmailTemplate ex = templateRepo.findById(id).orElseThrow();
        ex.setSubject(t.getSubject()); ex.setBodyHtml(t.getBodyHtml()); ex.setActive(t.isActive());
        return templateRepo.save(ex);
    }

    /* --- Settings --- */
    @GetMapping("/settings")
    public List<AppSetting> settings() { return settingRepo.findAll(); }

    @PutMapping("/settings/{key}")
    public AppSetting update(@PathVariable String key, @RequestBody Map<String, String> body) {
        AppSetting s = settingRepo.findBySettingKey(key)
                .orElseGet(() -> AppSetting.builder().settingKey(key).build());
        s.setSettingValue(body.get("value"));
        return settingRepo.save(s);
    }

    /* --- Audit & login history --- */
    @GetMapping("/audit-logs")
    public Page<AuditLog> logs(Pageable p) { return auditRepo.findAll(p); }

    @GetMapping("/login-history")
    public Page<LoginHistory> loginHistory(Pageable p) { return loginRepo.findAll(p); }
}
