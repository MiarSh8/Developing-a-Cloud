import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import com.google.cloud.storage.*;
import com.google.cloud.datastore.*;

/**
 * Main servlet handling HTTP requests for the document analytics system
 */
@WebServlet("/api/*")
public class DocumentAnalyticsServlet extends HttpServlet {
    private DocumentManager docManager;
    private DocumentSorter sorter;
    private DocumentSearchEngine searchEngine;
    private DocumentClassifier classifier;
    
    @Override
    public void init() throws ServletException {
        try {
            // Initialize Google Cloud services
            Storage storage = StorageOptions.getDefaultInstance().getService();
            Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
            String bucketName = getServletContext().getInitParameter("bucketName");
            
            // Initialize components
            docManager = new DocumentManager(storage, datastore, bucketName);
            sorter = new DocumentSorter();
            searchEngine = new DocumentSearchEngine(docManager);
            classifier = new DocumentClassifier();
            
            // Load or train classifier (would load from file in production)
            List<Document> trainingSet = loadTrainingData();
            classifier.train(trainingSet);
        } catch (Exception e) {
            throw new ServletException("Failed to initialize application", e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        try {
            String path = req.getPathInfo();
            
            if (path == null || path.equals("/")) {
                // List all documents
                List<Document> docs = docManager.getAllDocuments();
                sendJsonResponse(resp, docs);
            } else if (path.equals("/sorted")) {
                // Get sorted documents
                List<Document> docs = sorter.sortByTitle(docManager.getAllDocuments());
                sendJsonResponse(resp, docs);
            } else if (path.startsWith("/search")) {
                // Handle search
                String query = req.getParameter("q");
                if (query == null || query.trim().isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing search query");
                    return;
                }
                DocumentSearchEngine.SearchResults results = searchEngine.search(query);
                sendJsonResponse(resp, results);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        try {
            String path = req.getPathInfo();
            
            if (path == null || path.equals("/upload")) {
                // Handle document upload
                Part filePart = req.getPart("file");
                if (filePart == null) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No file uploaded");
                    return;
                }
                
                Document doc = docManager.uploadDocument(
                    filePart.getInputStream(), 
                    filePart.getSubmittedFileName()
                );
                
                // Classify the new document
                String category = classifier.classify(doc);
                doc.setCategory(category);
                
                sendJsonResponse(resp, doc);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
    
    // Helper methods
    private void sendJsonResponse(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        new ObjectMapper().writeValue(resp.getWriter(), data);
    }
    
    private List<Document> loadTrainingData() {
        // In real system would load from persistent storage
        List<Document> trainingSet = new ArrayList<>();
        trainingSet.add(new Document("train1", "Research Paper on AI", "path1", 1024, 
                                   "PDF", "Academic", new Date()));
        trainingSet.add(new Document("train2", "Quarterly Financial Report", "path2", 2048, 
                                   "DOCX", "Business", new Date()));
        // Add more training samples...
        return trainingSet;
    }
}