package com.ewallet.core.utils;

import com.ewallet.core.models.JournalAudit;
import com.ewallet.core.models.Transaction;
import com.ewallet.core.models.Utilisateur;
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.Chunk;



import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * Utilitaire pour l'exportation de données en PDF
 */
public class PDFExporter {
    
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL);
    
    /**
     * Exporter les transactions en PDF
     */
    public static boolean exportTransactionsToPDF(List<Transaction> transactions, String filePath, String title) {
        try {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            
            document.open();
            
            // Titre
            Paragraph titlePara = new Paragraph(title, TITLE_FONT);
            titlePara.setAlignment(Element.ALIGN_CENTER);
            titlePara.setSpacingAfter(15);
            document.add(titlePara);
            
            // Date de génération
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Paragraph datePara = new Paragraph("Généré le : " + sdf.format(new Date()), NORMAL_FONT);
            datePara.setAlignment(Element.ALIGN_RIGHT);
            datePara.setSpacingAfter(20);
            document.add(datePara);
            
            // Statistiques
            if (!transactions.isEmpty()) {
                long totalTransactions = transactions.size();
                double totalDepot = transactions.stream()
                    .filter(t -> "DEPOT".equalsIgnoreCase(t.getType()))
                    .mapToDouble(Transaction::getMontant)
                    .sum();
                double totalRetrait = transactions.stream()
                    .filter(t -> "RETRAIT".equalsIgnoreCase(t.getType()))
                    .mapToDouble(Transaction::getMontant)
                    .sum();
                double totalTransfert = transactions.stream()
                    .filter(t -> "TRANSFERT".equalsIgnoreCase(t.getType()))
                    .mapToDouble(Transaction::getMontant)
                    .sum();
                
                Paragraph stats = new Paragraph();
                stats.add(new Chunk("Résumé : ", HEADER_FONT));
                stats.add(new Chunk(totalTransactions + " transactions | ", NORMAL_FONT));
                stats.add(new Chunk("Dépôts : " + String.format("%,.0f", totalDepot) + " GNF | ", NORMAL_FONT));
                stats.add(new Chunk("Retraits : " + String.format("%,.0f", totalRetrait) + " GNF | ", NORMAL_FONT));
                stats.add(new Chunk("Transferts : " + String.format("%,.0f", totalTransfert) + " GNF", NORMAL_FONT));
                stats.setSpacingAfter(15);
                document.add(stats);
            }
            
            // Table des transactions
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);
            
            float[] columnWidths = {5f, 12f, 10f, 12f, 15f, 10f, 26f};
            table.setWidths(columnWidths);
            
            // Headers
            addTableHeader(table, "ID");
            addTableHeader(table, "N° Transaction");
            addTableHeader(table, "Type");
            addTableHeader(table, "Montant (GNF)");
            addTableHeader(table, "Date");
            addTableHeader(table, "Statut");
            addTableHeader(table, "Description");
            
            // Data
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            int rowNum = 0;
            
            for (Transaction transaction : transactions) {
                // Alternance de couleurs
                if (rowNum % 2 == 0) {
                    table.getDefaultCell().setBackgroundColor(BaseColor.WHITE);
                } else {
                    table.getDefaultCell().setBackgroundColor(new BaseColor(248, 248, 248));
                }
                
                addTableCell(table, String.valueOf(transaction.getId()));
                addTableCell(table, transaction.getNumeroTransaction() != null ? transaction.getNumeroTransaction() : "N/A");
                addTableCell(table, getTypeLabel(transaction.getType()));
                addTableCell(table, String.format("%,.0f", transaction.getMontant()));
                
                String dateStr = "N/A";
                if (transaction.getDateTransaction() != null) {
                    dateStr = transaction.getDateTransaction().format(dateFormatter);
                }
                addTableCell(table, dateStr);
                
                addTableCell(table, getStatusLabel(transaction.getStatut()));
                addTableCell(table, transaction.getDescription() != null ? transaction.getDescription() : "-");
                
                rowNum++;
            }
            
            document.add(table);
            
            // Totaux
            if (!transactions.isEmpty()) {
                double totalDepot = transactions.stream()
                    .filter(t -> "DEPOT".equalsIgnoreCase(t.getType()))
                    .mapToDouble(Transaction::getMontant)
                    .sum();
                double totalRetrait = transactions.stream()
                    .filter(t -> "RETRAIT".equalsIgnoreCase(t.getType()))
                    .mapToDouble(Transaction::getMontant)
                    .sum();
                double totalTransfert = transactions.stream()
                    .filter(t -> "TRANSFERT".equalsIgnoreCase(t.getType()))
                    .mapToDouble(Transaction::getMontant)
                    .sum();
                
                Paragraph totals = new Paragraph();
                totals.setSpacingBefore(20);
                
                if (totalDepot > 0) {
                    totals.add(new Paragraph("Total des dépôts : " + String.format("%,.0f", totalDepot) + " GNF", HEADER_FONT));
                }
                if (totalRetrait > 0) {
                    totals.add(new Paragraph("Total des retraits : " + String.format("%,.0f", totalRetrait) + " GNF", HEADER_FONT));
                }
                if (totalTransfert > 0) {
                    totals.add(new Paragraph("Total des transferts : " + String.format("%,.0f", totalTransfert) + " GNF", HEADER_FONT));
                }
                
                document.add(totals);
            }
            
            // Pied de page
            Paragraph footer = new Paragraph("\n\nDocument généré électroniquement - E-Wallet System v1.0", SMALL_FONT);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(30);
            document.add(footer);
            
            document.close();
            return true;
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'export PDF : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Exporter un reçu de transaction en PDF
     */
    public static boolean exportTransactionReceipt(Transaction transaction, Utilisateur utilisateur, String filePath) {
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            
            document.open();
            
            // En-tête
            Paragraph header = new Paragraph("E-WALLET SYSTEM", 
                new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.DARK_GRAY));
            header.setAlignment(Element.ALIGN_CENTER);
            header.setSpacingAfter(5);
            document.add(header);
            
            Paragraph subHeader = new Paragraph("Reçu de Transaction", 
                new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD));
            subHeader.setAlignment(Element.ALIGN_CENTER);
            subHeader.setSpacingAfter(30);
            document.add(subHeader);
            
            // Ligne séparatrice
            Paragraph separator = new Paragraph("_____________________________________________________");
            separator.setAlignment(Element.ALIGN_CENTER);
            separator.setSpacingAfter(20);
            document.add(separator);
            
            // Informations de la transaction
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm:ss");
            
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(80);
            infoTable.setHorizontalAlignment(Element.ALIGN_CENTER);
            infoTable.setSpacingBefore(10);
            infoTable.setSpacingAfter(20);
            
            addInfoRow(infoTable, "Numéro de transaction :", 
                transaction.getNumeroTransaction() != null ? transaction.getNumeroTransaction() : "N/A");
            
            String dateStr = "N/A";
            if (transaction.getDateTransaction() != null) {
                dateStr = transaction.getDateTransaction().format(formatter);
            }
            addInfoRow(infoTable, "Date et heure :", dateStr);
            
            addInfoRow(infoTable, "Type de transaction :", getTypeLabel(transaction.getType()));
            addInfoRow(infoTable, "Montant :", String.format("%,.0f GNF", transaction.getMontant()));
            addInfoRow(infoTable, "Statut :", getStatusLabel(transaction.getStatut()));
            
            if (utilisateur != null) {
                addInfoRow(infoTable, "Client :", utilisateur.getPrenom() + " " + utilisateur.getNom());
                addInfoRow(infoTable, "Email :", utilisateur.getEmail());
                if (utilisateur.getTelephone() != null) {
                    addInfoRow(infoTable, "Téléphone :", utilisateur.getTelephone());
                }
            }
            
            if (transaction.getDescription() != null && !transaction.getDescription().isEmpty()) {
                addInfoRow(infoTable, "Description :", transaction.getDescription());
            }
            
            document.add(infoTable);
            
            // Message de confirmation
            if ("CONFIRME".equalsIgnoreCase(transaction.getStatut())) {
                Paragraph confirmMsg = new Paragraph("✓ TRANSACTION CONFIRMÉE", 
                    new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.GREEN));
                confirmMsg.setAlignment(Element.ALIGN_CENTER);
                confirmMsg.setSpacingBefore(20);
                document.add(confirmMsg);
            }
            
            // Notes importantes
            Paragraph notes = new Paragraph();
            notes.setSpacingBefore(30);
            notes.add(new Chunk("Notes importantes :\n", HEADER_FONT));
            notes.add(new Chunk("• Ce reçu est une preuve électronique de transaction\n", SMALL_FONT));
            notes.add(new Chunk("• Conservez ce document pour vos archives\n", SMALL_FONT));
            notes.add(new Chunk("• En cas de problème, contactez le support avec le numéro de transaction\n", SMALL_FONT));
            notes.add(new Chunk("• Numéro de support : +224 000 000 000\n", SMALL_FONT));
            document.add(notes);
            
            // Pied de page
            Paragraph footer = new Paragraph();
            footer.setSpacingBefore(40);
            footer.add(new Chunk("_____________________________________________________\n", NORMAL_FONT));
            footer.add(new Chunk("Signature électronique\n\n", SMALL_FONT));
            footer.add(new Chunk("Document généré le : " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()), SMALL_FONT));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);
            
            document.close();
            return true;
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération du reçu : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Méthodes auxiliaires privées
     */
    private static void addTableHeader(PdfPTable table, String header) {
        PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
        cell.setBackgroundColor(new BaseColor(70, 130, 180));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8);
        cell.setBorderWidth(1);
        cell.setBorderColor(BaseColor.WHITE);
        table.addCell(cell);
    }
    
    private static void addTableCell(PdfPTable table, String content) {
        PdfPCell cell = new PdfPCell(new Phrase(content, NORMAL_FONT));
        cell.setPadding(6);
        cell.setBorderWidth(0.5f);
        cell.setBorderColor(BaseColor.LIGHT_GRAY);
        table.addCell(cell);
    }
    
    private static void addInfoRow(PdfPTable table, String label, String value) {
        // Label
        PdfPCell labelCell = new PdfPCell(new Phrase(label, HEADER_FONT));
        labelCell.setBorder(PdfPCell.NO_BORDER);
        labelCell.setPadding(5);
        table.addCell(labelCell);
        
        // Value
        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setBorder(PdfPCell.NO_BORDER);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }
    
    private static String getTypeLabel(String type) {
        if (type == null) return "N/A";
        switch (type.toUpperCase()) {
            case "DEPOT": return "DÉPÔT";
            case "RETRAIT": return "RETRAIT";
            case "TRANSFERT": return "TRANSFERT";
            case "PAIEMENT_SERVICE": return "PAIEMENT SERVICE";
            default: return type;
        }
    }
    
    private static String getStatusLabel(String status) {
        if (status == null) return "N/A";
        switch (status.toUpperCase()) {
            case "CONFIRME": return "✓ CONFIRMÉ";
            case "EN_ATTENTE": return "⏳ EN ATTENTE";
            case "REFUSE": return "✗ REFUSÉ";
            case "ANNULE": return "⚠ ANNULÉ";
            default: return status;
        }
    }


    public static boolean exportAuditToPDF(List<JournalAudit> logs, String filePath, String title) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            
            // Ajouter le titre
            Paragraph header = new Paragraph(title, 
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);
            
            document.add(new Paragraph(" "));
            
            // Créer le tableau
            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            
            // En-têtes
            String[] headers = {"ID", "Utilisateur", "Action", "Entité", "ID Entité", "Ancienne", "Nouvelle", "Date"};
            for (String headerText : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(headerText, 
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
                cell.setBackgroundColor(new BaseColor(220, 220, 220));
                table.addCell(cell);
            }
            
            // Données
            for (JournalAudit log : logs) {
                table.addCell(String.valueOf(log.getJournalId()));
                table.addCell(String.valueOf(log.getUtilisateurId()));
                table.addCell(log.getAction());
                table.addCell(log.getEntite() != null ? log.getEntite() : "");
                table.addCell(log.getEntiteId() != null ? String.valueOf(log.getEntiteId()) : "");
                table.addCell(truncate(log.getAncienneValeur(), 20));
                table.addCell(truncate(log.getNouvelleValeur(), 20));
                table.addCell(log.getDateAction() != null ? 
                    log.getDateAction().format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm")) : "");
            }
            
            document.add(table);
            document.close();
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text != null ? text : "";
        }
        return text.substring(0, maxLength - 3) + "...";
    }
}