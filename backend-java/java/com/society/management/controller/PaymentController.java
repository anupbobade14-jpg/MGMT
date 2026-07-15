package com.society.management.controller;

import com.society.management.dto.request.PaymentReviewRequest;
import com.society.management.dto.request.PaymentSubmitRequest;
import com.society.management.entity.Payment;
import com.society.management.entity.PaymentStatus;
import com.society.management.exception.ApiException;
import com.society.management.repository.PaymentRepository;
import com.society.management.service.PaymentService;
import com.society.management.util.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepo;
    private final FileStorageService fileStorage;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Payment submit(@RequestPart("data") @Valid PaymentSubmitRequest data,
                          @RequestPart(value = "proof", required = false) MultipartFile proof) {
        return paymentService.submitPayment(data, proof);
    }

    @GetMapping
    public Page<Payment> list(@RequestParam(required = false) PaymentStatus status, Pageable p) {
        return status != null ? paymentRepo.findByStatus(status, p) : paymentRepo.findAll(p);
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','SOCIETY_ADMIN','ACCOUNTANT')")
    public Payment review(@PathVariable Long id, @Valid @RequestBody PaymentReviewRequest req) {
        return paymentService.reviewPayment(id, req);
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<ByteArrayResource> downloadReceipt(@PathVariable Long id) {
        Payment p = paymentRepo.findById(id).orElseThrow(() -> ApiException.notFound("Payment not found"));
        if (p.getReceiptFilePath() == null) throw ApiException.badRequest("Receipt not generated yet");
        byte[] bytes = fileStorage.read(p.getReceiptFilePath());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"receipt-" + p.getReceiptNumber() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new ByteArrayResource(bytes));
    }

    @GetMapping("/{id}/proof")
    public ResponseEntity<ByteArrayResource> downloadProof(@PathVariable Long id) {
        Payment p = paymentRepo.findById(id).orElseThrow(() -> ApiException.notFound("Payment not found"));
        if (p.getProofFilePath() == null) throw ApiException.notFound("No proof uploaded");
        byte[] bytes = fileStorage.read(p.getProofFilePath());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(p.getProofContentType() != null ? p.getProofContentType() : "application/octet-stream"))
                .body(new ByteArrayResource(bytes));
    }
}
