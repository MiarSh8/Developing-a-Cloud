import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import java.io.*;
import java.util.*;

/**
 * Implements full-text search with highlighting using Apache Lucene
 */
public class DocumentSearchEngine {
    private Directory indexDirectory;
    private StandardAnalyzer analyzer;
    private DocumentManager docManager;
    
    public DocumentSearchEngine(DocumentManager docManager) throws IOException {
        this.docManager = docManager;
        this.indexDirectory = new RAMDirectory(); // In-memory index (would use persistent storage in production)
        this.analyzer = new StandardAnalyzer();
        buildIndex();
    }
    
    /**
     * Builds search index from all documents
     */
    private void buildIndex() throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(indexDirectory, config);
        
        // Add all documents to index
        for (Document doc : docManager.getAllDocuments()) {
            org.apache.lucene.document.Document luceneDoc = new org.apache.lucene.document.Document();
            luceneDoc.add(new StringField("id", doc.getId(), Field.Store.YES));
            luceneDoc.add(new TextField("title", doc.getTitle(), Field.Store.YES));
            
            // In real system would index document content too
            // For now just index title
            writer.addDocument(luceneDoc);
        }
        
        writer.close();
    }
    
    /**
     * Searches documents for the given query
     * @param queryText Search query
     * @return Search results with highlighted matches
     */
    public SearchResults search(String queryText) throws Exception {
        QueryParser parser = new QueryParser("title", analyzer);
        Query query = parser.parse(queryText);
        
        IndexReader reader = DirectoryReader.open(indexDirectory);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(query, 10); // Limit to 10 results
        
        SearchResults results = new SearchResults();
        results.setTotalHits(docs.totalHits.value);
        
        // Process hits
        for (ScoreDoc scoreDoc : docs.scoreDocs) {
            org.apache.lucene.document.Document hitDoc = searcher.doc(scoreDoc.doc);
            SearchResult result = new SearchResult();
            result.setDocumentId(hitDoc.get("id"));
            result.setTitle(hitDoc.get("title"));
            
            // Highlight matches (simplified - would use proper highlighter in real system)
            result.setHighlightedTitle(highlightMatches(hitDoc.get("title"), queryText));
            
            results.addResult(result);
        }
        
        reader.close();
        return results;
    }
    
    // Simple highlighting (would use Lucene Highlighter in production)
    private String highlightMatches(String text, String query) {
        return text.replaceAll("(?i)(" + query + ")", "<b>$1</b>");
    }
    
    // Nested classes for search results
    public static class SearchResults {
        private long totalHits;
        private List<SearchResult> results = new ArrayList<>();
        
        // Getters and setters
        public void addResult(SearchResult result) { results.add(result); }
        public List<SearchResult> getResults() { return results; }
        public long getTotalHits() { return totalHits; }
        public void setTotalHits(long totalHits) { this.totalHits = totalHits; }
    }
    
    public static class SearchResult {
        private String documentId;
        private String title;
        private String highlightedTitle;
        
        // Getters and setters
        public String getDocumentId() { return documentId; }
        public void setDocumentId(String documentId) { this.documentId = documentId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getHighlightedTitle() { return highlightedTitle; }
        public void setHighlightedTitle(String highlightedTitle) { this.highlightedTitle = highlightedTitle; }
    }
}