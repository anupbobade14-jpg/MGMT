package com.society.management.service;

import com.society.management.config.AppProperties;
import com.society.management.dto.request.PaymentReviewRequest;
import com.society.management.dto.request.PaymentSubmitRequest;
import com.society.management.entity.*;
import com.society.management.exception.ApiException;
import com.society.management.repository.*;
import com.society.management.util.FileStorageService;
import com.society.management.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepo;
    private final MaintenanceBillRepository billRepo;
    private final OwnerRepository ownerRepo;
    private final UserRepository userRepo;
    private final FileStorageService fileStorage;
    private final EmailService emailService;
    private final PdfReceiptService pdfService;
    private final AppProperties props;

    @Transactional
    public Payment submitPayment(PaymentSubmitRequest req, MultipartFile proofImage) {
        MaintenanceBill bill = billRepo.findById(req.maintenanceId())
                .orElseThrow(() -> ApiException.notFound("Maintenance bill not found"));

        Owner owner = ownerRepo.findByUserId(SecurityUtils.currentUserId()).orElse(bill.getOwner());

        String storedPath = null;
        String contentType = null;
        if (proofImage != null && !proofImage.isEmpty()) {
            var allowed = new HashSet<>(Arrays.asList(props.getUpload().getAllowedImageTypes().split(",")));
            storedPath = fileStorage.store(proofImage, props.getUpload().getPaymentProofs(), allowed);
            contentType = proofImage.getContentType();
        }

        Payment payment = Payment.builder()
                .maintenance(bill).owner(owner).amount(req.amount())
                .paymentMode(req.paymentMode()).transactionRef(req.transactionRef())
                .paymentDate(req.paymentDate() != null ? LocalDate.parse(req.paymentDate()) : LocalDate.now())
                .proofFilePath(storedPath).proofContentType(contentType)
                .status(PaymentStatus.UNDER_VERIFICATION).build();
        payment = paymentRepo.save(payment);

        bill.setStatus(MaintenanceStatus.UNDER_VERIFICATION);
        billRepo.save(bill);

        if (owner != null && owner.getEmail() != null) {
            Map<String, String> vars = varsFor(owner, bill);
            vars.put("amount", req.amount().toString());
            emailService.sendTemplate(owner.getEmail(), "PAYMENT_RECEIVED", vars);
        }
        return payment;
    }

    @Transactional
    public Payment reviewPayment(Long paymentId, PaymentReviewRequest req) {
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> ApiException.notFound("Payment not found"));
        if (payment.getStatus() != PaymentStatus.UNDER_VERIFICATION)
            throw ApiException.badRequest("Payment already reviewed");

        User reviewer = userRepo.findById(SecurityUtils.currentUserId()).orElse(null);
        payment.setReviewedBy(reviewer);
        payment.setReviewedAt(OffsetDateTime.now());
        payment.setReviewNotes(req.notes());

        MaintenanceBill bill = payment.getMaintenance();
        Owner owner = payment.getOwner();

        if (Boolean.TRUE.equals(req.approve())) {
            payment.setStatus(PaymentStatus.APPROVED);
            payment.setReceiptNumber(generateReceiptNo(payment));
            String pdfPath = pdfService.generateReceipt(payment);
            payment.setReceiptFilePath(pdfPath);

            bill.setStatus(MaintenanceStatus.APPROVED);
            billRepo.save(bill);

            if (owner != null && owner.getEmail() != null) {
                Map<String, String> vars = varsFor(owner, bill);
                vars.put("receiptNo", payment.getReceiptNumber());
                emailService.sendTemplate(owner.getEmail(), "PAYMENT_APPROVED", vars);
            }
        } else {
            payment.setStatus(PaymentStatus.REJECTED);
            bill.setStatus(MaintenanceStatus.PENDING);
            billRepo.save(bill);
            if (owner != null && owner.getEmail() != null) {
                Map<String, String> vars = varsFor(owner, bill);
                vars.put("reason", req.notes() == null ? "Invalid payment proof" : req.notes());
                emailService.sendTemplate(owner.getEmail(), "PAYMENT_REJECTED", vars);
            }
        }
        return paymentRepo.save(payment);
    }

    private String generateReceiptNo(Payment p) {
        return "RCPT-" + p.getMaintenance().getBillYear()
                + String.format("%02d", p.getMaintenance().getBillMonth())
                + "-" + String.format("%06d", p.getId());
    }

    private Map<String, String> varsFor(Owner o, MaintenanceBill b) {
        Map<String, String> v = new HashMap<>();
        v.put("ownerName", o.getFullName());
        v.put("month", java.time.Month.of(b.getBillMonth()).name());
        v.put("year", String.valueOf(b.getBillYear()));
        v.put("amount", b.getTotalAmount().toString());
        v.put("dueDate", b.getDueDate().toString());
        return v;
    }
}
