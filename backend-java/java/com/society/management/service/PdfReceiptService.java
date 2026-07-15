package com.society.management.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.society.management.config.AppProperties;
import com.society.management.entity.MaintenanceBill;
import com.society.management.entity.Payment;
import com.society.management.repository.AppSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfReceiptService {

    private final AppProperties props;
    private final AppSettingRepository settingsRepo;

    public String generateReceipt(Payment payment) {
        try {
            MaintenanceBill bill = payment.getMaintenance();
            String societyName = settingValue("society.name", "Society Management");
            String societyAddr = settingValue("society.address", "");

            Path baseDir = Paths.get(props.getUpload().getBaseDir(), props.getUpload().getReceipts())
                                .toAbsolutePath();
            Files.createDirectories(baseDir);
            String fileName = "receipt-" + payment.getReceiptNumber() + ".pdf";
            Path filePath = baseDir.resolve(fileName);

            Document doc = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(doc, new FileOutputStream(filePath.toFile()));
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(30, 60, 120));
            Font h2 = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font body = FontFactory.getFont(FontFactory.HELVETICA, 11);

            Paragraph title = new Paragraph(societyName, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);
            if (!societyAddr.isEmpty()) {
                Paragraph addr = new Paragraph(societyAddr, body);
                addr.setAlignment(Element.ALIGN_CENTER);
                doc.add(addr);
            }
            doc.add(new Paragraph(" "));

            Paragraph rec = new Paragraph("PAYMENT RECEIPT", h2);
            rec.setAlignment(Element.ALIGN_CENTER);
            doc.add(rec);
            doc.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1f, 2f});
            row(table, "Receipt No", payment.getReceiptNumber(), body);
            row(table, "Payment Date",
                    payment.getPaymentDate().format(DateTimeFormatter.ISO_LOCAL_DATE), body);
            row(table, "Flat", bill.getFlat().getFlatNumber() + " ("
                    + bill.getFlat().getBuilding().getName() + ")", body);
            row(table, "Owner", bill.getOwner() != null ? bill.getOwner().getFullName() : "-", body);
            row(table, "Period", monthName(bill.getBillMonth()) + " " + bill.getBillYear(), body);
            row(table, "Amount", "Rs. " + payment.getAmount(), body);
            row(table, "Payment Mode", payment.getPaymentMode().name(), body);
            if (payment.getTransactionRef() != null)
                row(table, "Transaction Ref", payment.getTransactionRef(), body);
            row(table, "Status", "APPROVED", body);
            doc.add(table);

            doc.add(new Paragraph(" "));
            Paragraph thanks = new Paragraph("Thank you for your payment.", body);
            thanks.setAlignment(Element.ALIGN_CENTER);
            doc.add(thanks);

            doc.add(new Paragraph(" "));
            Paragraph footer = new Paragraph(
                    "This is a system-generated receipt and does not require a signature.",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();
            log.info("Receipt generated: {}", filePath);
            return filePath.toString();
        } catch (Exception e) {
            log.error("Failed to generate PDF receipt", e);
            return null;
        }
    }

    private void row(PdfPTable t, String k, String v, Font f) {
        PdfPCell kc = new PdfPCell(new Phrase(k, f));
        kc.setBackgroundColor(new Color(240, 240, 250));
        kc.setPadding(6f);
        PdfPCell vc = new PdfPCell(new Phrase(v == null ? "-" : v, f));
        vc.setPadding(6f);
        t.addCell(kc); t.addCell(vc);
    }

    private String monthName(int m) {
        return java.time.Month.of(m).getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH);
    }

    private String settingValue(String key, String def) {
        return settingsRepo.findBySettingKey(key).map(s -> s.getSettingValue()).orElse(def);
    }
}
