package com.ewallet.core.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ExportUtil {
    
    /**
     * Export simple vers CSV
     */
    public static boolean exportToCsv(List<Map<String, Object>> data, String filePath, String title) {
        if (data == null || data.isEmpty()) {
            System.err.println("[EXPORT] Aucune donnée à exporter");
            return false;
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // En-tête
            writer.println("========================================================");
            writer.println(title.toUpperCase());
            writer.println("========================================================");
            writer.println("Généré le: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            writer.println();
            
            if (!data.isEmpty()) {
                // En-tête des colonnes
                Map<String, Object> firstRow = data.get(0);
                StringBuilder header = new StringBuilder();
                boolean first = true;
                
                for (String key : firstRow.keySet()) {
                    if (!first) header.append(";");
                    header.append(formatCsvCell(key));
                    first = false;
                }
                writer.println(header.toString());
                
                // Données
                for (Map<String, Object> rowData : data) {
                    StringBuilder line = new StringBuilder();
                    first = true;
                    
                    for (Object value : rowData.values()) {
                        if (!first) line.append(";");
                        line.append(formatCsvCell(value != null ? value.toString() : ""));
                        first = false;
                    }
                    writer.println(line.toString());
                }
            }
            
            System.out.println("[EXPORT] CSV créé: " + filePath);
            return true;
            
        } catch (Exception e) {
            System.err.println("[EXPORT] Erreur CSV: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Export vers TXT formaté
     */
    public static boolean exportToTxt(List<Map<String, Object>> data, String filePath, String title) {
        if (data == null || data.isEmpty()) {
            System.err.println("[EXPORT] Aucune donnée à exporter");
            return false;
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // En-tête
            writer.println("=".repeat(60));
            writer.println("          " + title.toUpperCase());
            writer.println("=".repeat(60));
            writer.println();
            writer.println("Généré le: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            writer.println();
            
            // Données
            for (int i = 0; i < data.size(); i++) {
                Map<String, Object> row = data.get(i);
                writer.println("─".repeat(60));
                writer.println("Enregistrement #" + (i + 1));
                writer.println("─".repeat(30));
                
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    String key = formatKey(entry.getKey());
                    String value = entry.getValue() != null ? entry.getValue().toString() : "";
                    writer.printf("%-25s: %s%n", key, value);
                }
                writer.println();
            }
            
            // Résumé
            writer.println("=".repeat(60));
            writer.println("RÉSUMÉ");
            writer.println("=".repeat(60));
            writer.printf("Total enregistrements: %d%n", data.size());
            writer.println();
            writer.println("Fin du rapport");
            
            System.out.println("[EXPORT] TXT créé: " + filePath);
            return true;
            
        } catch (Exception e) {
            System.err.println("[EXPORT] Erreur TXT: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Export statistiques
     */
    public static boolean exportStatistics(Map<String, Object> stats, String filePath) {
        if (stats == null || stats.isEmpty()) {
            System.err.println("[EXPORT] Aucune statistique");
            return false;
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("=".repeat(70));
            writer.println("                STATISTIQUES DU SYSTÈME");
            writer.println("=".repeat(70));
            writer.println();
            writer.println("Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            writer.println();
            
            for (Map.Entry<String, Object> entry : stats.entrySet()) {
                String key = formatKey(entry.getKey());
                Object value = entry.getValue();
                
                if (value instanceof Number) {
                    double num = ((Number) value).doubleValue();
                    if (key.toLowerCase().contains("montant") || key.toLowerCase().contains("solde") || 
                        key.toLowerCase().contains("commission") || key.toLowerCase().contains("volume")) {
                        writer.printf("%-30s: %,.0f GNF%n", key, num);
                    } else {
                        writer.printf("%-30s: %,.0f%n", key, num);
                    }
                } else {
                    writer.printf("%-30s: %s%n", key, value.toString());
                }
            }
            
            writer.println();
            writer.println("=".repeat(70));
            writer.println("Fin du rapport");
            
            System.out.println("[EXPORT] Statistiques créées: " + filePath);
            return true;
            
        } catch (Exception e) {
            System.err.println("[EXPORT] Erreur statistiques: " + e.getMessage());
            return false;
        }
    }
    
    // Méthodes utilitaires
    private static String formatCsvCell(String value) {
        if (value == null) return "";
        if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    private static String formatKey(String key) {
        key = key.replace("_", " ");
        key = key.substring(0, 1).toUpperCase() + key.substring(1);
        return key;
    }


    /**
 * Exporte des données vers un fichier texte formaté (alternative au PDF)
 */
    public static boolean exportToPdf(List<Map<String, Object>> data, 
                                    String filePath, 
                                    String title,
                                    String subtitle,
                                    Map<String, Object> extraInfo) {
        try {
            StringBuilder content = new StringBuilder();
            
            // En-tête
            content.append("=".repeat(80)).append("\n");
            content.append(title).append("\n");
            content.append(subtitle).append("\n");
            content.append("=".repeat(80)).append("\n\n");
            
            // Informations supplémentaires
            if (extraInfo != null && !extraInfo.isEmpty()) {
                content.append("INFORMATIONS:\n");
                content.append("-".repeat(40)).append("\n");
                for (Map.Entry<String, Object> entry : extraInfo.entrySet()) {
                    content.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }
                content.append("\n");
            }
            
            // Données
            if (!data.isEmpty()) {
                content.append("DONNÉES:\n");
                content.append("-".repeat(40)).append("\n");
                
                // En-têtes du tableau
                for (String key : data.get(0).keySet()) {
                    content.append(String.format("%-20s", key)).append(" | ");
                }
                content.append("\n");
                content.append("-".repeat(data.get(0).size() * 23)).append("\n");
                
                // Lignes de données
                for (Map<String, Object> row : data) {
                    for (Object value : row.values()) {
                        String cellValue = (value != null) ? value.toString() : "";
                        content.append(String.format("%-20s", cellValue.length() > 20 ? cellValue.substring(0, 17) + "..." : cellValue))
                            .append(" | ");
                    }
                    content.append("\n");
                }
            } else {
                content.append("AUCUNE DONNÉE À EXPORTER\n");
            }
            
            // Pied de page
            content.append("\n").append("=".repeat(80)).append("\n");
            content.append("Généré le ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
            content.append("=".repeat(80)).append("\n");
            
            // Écrire dans le fichier
            Files.write(Paths.get(filePath), content.toString().getBytes());
            
            System.out.println("[EXPORT] Fichier texte créé: " + filePath);
            return true;
            
        } catch (IOException e) {
            System.err.println("[ERROR] Erreur export fichier: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}