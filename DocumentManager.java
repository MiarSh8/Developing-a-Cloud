import com.google.cloud.storage.*;
import com.google.cloud.datastore.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all document storage and retrieval operations with Google Cloud services
 */
public class DocumentManager {
    private Storage storage;        // Google Cloud Storage client
    private Datastore datastore;    // Google Cloud Datastore client
    private String bucketName;      // Cloud Storage bucket name

    public DocumentManager(Storage storage, Datastore datastore, String bucketName) {
        this.storage = storage;
        this.datastore = datastore;
        this.bucketName = bucketName;
    }

    /**
     * Uploads a document to Cloud Storage and stores metadata in Datastore
     * @param fileStream Input stream of the document file
     * @param fileName Original file name
     * @return Document object with metadata
     */
    public Document uploadDocument(InputStream fileStream, String fileName) {
        try {
            // Generate unique ID for the document
            String docId = generateDocumentId();
            
            // Upload to Cloud Storage
            BlobId blobId = BlobId.of(bucketName, docId);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
            Blob blob = storage.create(blobInfo, fileStream);
            
            // Extract basic metadata
            String fileType = getFileType(fileName);
            String title = extractTitleFromFileName(fileName); // Simplified - would parse content in real impl
            
            // Store metadata in Datastore
            Key docKey = datastore.newKeyFactory().setKind("Document").newKey(docId);
            Entity docEntity = Entity.newBuilder(docKey)
                .set("title", title)
                .set("filePath", blob.getBlobId().getName())
                .set("size", blob.getSize())
                .set("fileType", fileType)
                .set("category", "Unclassified") // Default before classification
                .set("uploadDate", DateTime.now())
                .build();
            datastore.put(docEntity);
            
            return new Document(docId, title, blob.getBlobId().getName(), 
                              blob.getSize(), fileType, "Unclassified", new Date());
        } catch (Exception e) {
            throw new RuntimeException("Document upload failed", e);
        }
    }

    /**
     * Retrieves all documents from Datastore
     * @return List of Document objects
     */
    public List<Document> getAllDocuments() {
        List<Document> documents = new ArrayList<>();
        
        Query<Entity> query = Query.newEntityQueryBuilder()
            .setKind("Document")
            .build();
        
        QueryResults<Entity> results = datastore.run(query);
        
        while (results.hasNext()) {
            Entity entity = results.next();
            documents.add(entityToDocument(entity));
        }
        
        return documents;
    }
    
    // Helper methods
    private String generateDocumentId() {
        return "doc-" + UUID.randomUUID().toString();
    }
    
    private String getFileType(String fileName) {
        if (fileName.toLowerCase().endsWith(".pdf")) return "PDF";
        if (fileName.toLowerCase().endsWith(".docx")) return "DOCX";
        return "UNKNOWN";
    }
    
    private String extractTitleFromFileName(String fileName) {
        // Simple implementation - would parse document content in real system
        return fileName.replaceFirst("[.][^.]+$", ""); // Remove extension
    }
    
    private Document entityToDocument(Entity entity) {
        return new Document(
            entity.getKey().getName(),
            entity.getString("title"),
            entity.getString("filePath"),
            entity.getLong("size"),
            entity.getString("fileType"),
            entity.getString("category"),
            new Date(entity.getTimestamp("uploadDate").getSeconds() * 1000)
        );
    }
}