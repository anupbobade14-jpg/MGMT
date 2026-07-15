package com.society.management.service;

import com.society.management.config.AppProperties;
import com.society.management.entity.EmailTemplate;
import com.society.management.repository.EmailTemplateRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateRepository templateRepo;
    private final AppProperties props;

    @Async
    public void sendTemplate(String toEmail, String templateCode, Map<String, String> vars) {
        if (!props.getMail().isEnabled()) { log.debug("Mail disabled, skipping {}", templateCode); return; }
        Optional<EmailTemplate> opt = templateRepo.findByCode(templateCode);
        if (opt.isEmpty()) { log.warn("Template not found: {}", templateCode); return; }
        EmailTemplate tpl = opt.get();
        String subject = replace(tpl.getSubject(), vars);
        String body = replace(tpl.getBodyHtml(), vars);
        sendHtml(toEmail, subject, body);
    }

    @Async
    public void sendHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, true, StandardCharsets.UTF_8.name());
            h.setFrom(props.getMail().getFrom(), props.getMail().getFromName());
            h.setTo(to);
            h.setSubject(subject);
            h.setText(htmlBody, true);
            mailSender.send(msg);
            log.info("Email sent to {} - {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String replace(String tpl, Map<String, String> vars) {
        String out = tpl;
        if (vars == null) return out;
        for (Map.Entry<String, String> e : vars.entrySet()) {
            out = out.replace("{{" + e.getKey() + "}}", e.getValue() == null ? "" : e.getValue());
        }
        return out;
    }
}
