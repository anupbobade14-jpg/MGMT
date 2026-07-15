package com.society.management.controller;

import com.society.management.config.AppProperties;
import com.society.management.entity.Document;
import com.society.management.entity.User;
import com.society.management.exception.ApiException;
import com.society.management.repository.DocumentRepository;
import com.society.management.repository.UserRepository;
import com.society.management.util.FileStorageService;
import com.society.management.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentRepository docRepo;
    private final UserRepository userRepo;
    private final FileStorageService fileStorage;
    private final AppProperties props;

    @GetMapping
    public List<Document> list(@RequestParam(required = false) String category) {
        return category != null ? docRepo.findByCategory(category) : docRepo.findAll();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SOCIETY_ADMIN','ACCOUNTANT','COMMITTEE')")
    public Document upload(@RequestParam String title, @RequestParam String category,
                           @RequestPart MultipartFile file) {
        User me = userRepo.findById(SecurityUtils.currentUserId()).orElse(null);
        String path = fileStorage.store(file, props.getUpload().getDocuments(), null);
        return docRepo.save(Document.builder()
                .title(title).category(category).filePath(path)
                .contentType(file.getContentType()).fileSize(file.getSize())
                .uploadedBy(me).build());
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<ByteArrayResource> download(@PathVariable Long id) {
        Document d = docRepo.findById(id).orElseThrow(() -> ApiException.notFound("Not found"));
        byte[] bytes = fileStorage.read(d.getFilePath());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + d.getTitle() + "\"")
                .contentType(MediaType.parseMediaType(d.getContentType() != null ? d.getContentType() : "application/octet-stream"))
                .body(new ByteArrayResource(bytes));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SOCIETY_ADMIN')")
    public void delete(@PathVariable Long id) { docRepo.deleteById(id); }
}
