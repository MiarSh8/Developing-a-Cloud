import java.util.*;
import weka.classifiers.trees.J48;
import weka.core.*;

/**
 * Implements document classification using WEKA's J48 decision tree algorithm
 */
public class DocumentClassifier {
    private J48 classifier;
    private boolean isTrained;
    
    public DocumentClassifier() {
        this.classifier = new J48();
        this.isTrained = false;
    }
    
    /**
     * Trains the classifier with sample documents
     * @param trainingSet List of pre-classified documents
     */
    public void train(List<Document> trainingSet) throws Exception {
        // Convert documents to WEKA instances
        ArrayList<Attribute> attributes = new ArrayList<>();
        
        // Define attributes (simplified - would use more features in real system)
        attributes.add(new Attribute("title", (ArrayList<String>) null)); // Text attribute
        attributes.add(new Attribute("size", true));                     // Numeric attribute
        
        // Define class values (categories)
        ArrayList<String> classValues = new ArrayList<>();
        classValues.add("Academic");
        classValues.add("Business");
        classValues.add("Technical");
        classValues.add("Other");
        attributes.add(new Attribute("category", classValues));
        
        // Create empty training set
        Instances trainingInstances = new Instances("DocumentClassification", attributes, trainingSet.size());
        trainingInstances.setClassIndex(attributes.size() - 1); // Last attribute is class
        
        // Add training data
        for (Document doc : trainingSet) {
            Instance instance = new DenseInstance(attributes.size());
            instance.setValue(attributes.get(0), doc.getTitle()); // Title text
            instance.setValue(attributes.get(1), doc.getSize());  // Document size
            instance.setValue(attributes.get(2), doc.getCategory()); // Known category
            trainingInstances.add(instance);
        }
        
        // Build classifier
        classifier.buildClassifier(trainingInstances);
        isTrained = true;
    }
    
    /**
     * Classifies a document using the trained model
     * @param doc Document to classify
     * @return Predicted category
     */
    public String classify(Document doc) throws Exception {
        if (!isTrained) {
            throw new IllegalStateException("Classifier has not been trained");
        }
        
        // Create attributes same as training
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("title", (ArrayList<String>) null));
        attributes.add(new Attribute("size", true));
        
        ArrayList<String> classValues = new ArrayList<>();
        classValues.add("Academic");
        classValues.add("Business");
        classValues.add("Technical");
        classValues.add("Other");
        attributes.add(new Attribute("category", classValues));
        
        // Create single instance for classification
        Instances data = new Instances("ClassificationInstance", attributes, 1);
        data.setClassIndex(attributes.size() - 1);
        
        Instance instance = new DenseInstance(attributes.size());
        instance.setValue(attributes.get(0), doc.getTitle());
        instance.setValue(attributes.get(1), doc.getSize());
        data.add(instance);
        
        // Classify
        double prediction = classifier.classifyInstance(data.firstInstance());
        return data.classAttribute().value((int) prediction);
    }
}