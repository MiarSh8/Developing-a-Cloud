import java.util.Date;

/**
 * Represents a document stored in the system with all relevant metadata
 */
public class Document {
    private String id;          // Unique document identifier
    private String title;      // Document title extracted from content/metadata
    private String filePath;   // Cloud Storage path to the document
    private long size;         // File size in bytes
    private String fileType;   // PDF or DOCX
    private String category;   // Classification category
    private Date uploadDate;   // When document was uploaded
    
    // Constructor
    public Document(String id, String title, String filePath, long size, 
                   String fileType, String category, Date uploadDate) {
        this.id = id;
        this.title = title;
        this.filePath = filePath;
        this.size = size;
        this.fileType = fileType;
        this.category = category;
        this.uploadDate = uploadDate;
    }

    // Getters and setters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getFilePath() { return filePath; }
    public long getSize() { return size; }
    public String getFileType() { return fileType; }
    public String getCategory() { return category; }
    public Date getUploadDate() { return uploadDate; }
    
    public void setCategory(String category) { this.category = category; }
    // Other setters as needed...
}