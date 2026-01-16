package com.ritesh.crypto_portfolio_tracker.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.ritesh.crypto_portfolio_tracker.dto.pnl.RealizedPnlSummary;
import com.ritesh.crypto_portfolio_tracker.dto.pnl.UnrealizedPnlEntry;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PortfolioPdfService {

    private final RealizedPnlService realizedService;
    private final UnrealizedPnlService unrealizedService;

    public PortfolioPdfService(RealizedPnlService realizedService,
                               UnrealizedPnlService unrealizedService) {
        this.realizedService = realizedService;
        this.unrealizedService = unrealizedService;
    }

    public byte[] generatePdf(Long userId) throws DocumentException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);

        document.open();

        // Fonts
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

        // Title
        document.add(new Paragraph("Portfolio Summary Report", titleFont));
        document.add(new Paragraph(" "));

        // Data
        List<UnrealizedPnlEntry> unrealized = unrealizedService.computeUnrealizedPnl(userId);
        RealizedPnlSummary realized = realizedService.computeRealizedPnl(userId);

        double totalValue = unrealized.stream().mapToDouble(UnrealizedPnlEntry::getValue).sum();
        double unrealizedTotal = unrealized.stream().mapToDouble(UnrealizedPnlEntry::getUnrealizedPnl).sum();
        double realizedTotal = realized.getTotalRealizedGain();

        // Summary
        document.add(new Paragraph("Total Portfolio Value: " + String.format("%.2f USD", totalValue), bodyFont));
        document.add(new Paragraph("Unrealized P&L: " + String.format("%.2f USD", unrealizedTotal), bodyFont));
        document.add(new Paragraph("Realized P&L: " + String.format("%.2f USD", realizedTotal), bodyFont));
        document.add(new Paragraph(" "));

        // Table
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);

        table.addCell(new Paragraph("Symbol", headerFont));
        table.addCell(new Paragraph("Quantity", headerFont));
        table.addCell(new Paragraph("Avg Cost", headerFont));
        table.addCell(new Paragraph("Current Price", headerFont));
        table.addCell(new Paragraph("Unrealized P&L", headerFont));

        for (UnrealizedPnlEntry u : unrealized) {
            table.addCell(new Paragraph(u.getSymbol(), bodyFont));
            table.addCell(new Paragraph(String.valueOf(u.getQuantity()), bodyFont));
            table.addCell(new Paragraph(String.valueOf(u.getAvgCost()), bodyFont));
            table.addCell(new Paragraph(String.valueOf(u.getCurrentPrice()), bodyFont));
            table.addCell(new Paragraph(String.valueOf(u.getUnrealizedPnl()), bodyFont));
        }

        document.add(table);
        document.close();

        return out.toByteArray();
    }
}
