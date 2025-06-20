import java.util.*;
import java.util.stream.Collectors;

/**
 * Collects and reports statistics about the document collection
 */
public class StatisticsCollector {
    private DocumentManager docManager;
    
    public StatisticsCollector(DocumentManager docManager) {
        this.docManager = docManager;
    }
    
    /**
     * Generates statistics about the document collection
     * @return Map of statistics
     */
    public Map<String, Object> generateStatistics() {
        List<Document> allDocs = docManager.getAllDocuments();
        Map<String, Object> stats = new HashMap<>();
        
        // Basic counts
        stats.put("totalDocuments", allDocs.size());
        stats.put("pdfCount", countByType(allDocs, "PDF"));
        stats.put("docxCount", countByType(allDocs, "DOCX"));
        
        // Size statistics
        stats.put("totalSizeKB", allDocs.stream().mapToLong(Document::getSize).sum() / 1024);
        stats.put("averageSizeKB", allDocs.stream().mapToLong(Document::getSize).average().orElse(0) / 1024);
        
        // Category distribution
        stats.put("categoryDistribution", getCategoryDistribution(allDocs));
        
        return stats;
    }
    
    // Helper methods
    private long countByType(List<Document> docs, String type) {
        return docs.stream().filter(d -> d.getFileType().equals(type)).count();
    }
    
    private Map<String, Long> getCategoryDistribution(List<Document> docs) {
        return docs.stream()
            .collect(Collectors.groupingBy(Document::getCategory, Collectors.counting()));
    }
}b 