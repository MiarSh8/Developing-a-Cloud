import java.util.List;
import java.util.ArrayList;

/**
 * Implements merge sort algorithm for sorting documents by title
 */
public class DocumentSorter {
    
    /**
     * Sorts documents by title using merge sort algorithm
     * @param documents List of documents to sort
     * @return New sorted list of documents
     */
    public List<Document> sortByTitle(List<Document> documents) {
        if (documents == null || documents.size() <= 1) {
            return new ArrayList<>(documents);
        }
        
        // Convert to array for merge sort
        Document[] docArray = documents.toArray(new Document[0]);
        mergeSort(docArray, 0, docArray.length - 1);
        
        // Convert back to list
        List<Document> sorted = new ArrayList<>();
        for (Document doc : docArray) {
            sorted.add(doc);
        }
        return sorted;
    }
    
    // Recursive merge sort implementation
    private void mergeSort(Document[] array, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;
            mergeSort(array, left, mid);
            mergeSort(array, mid + 1, right);
            merge(array, left, mid, right);
        }
    }
    
    // Merge two sorted subarrays
    private void merge(Document[] array, int left, int mid, int right) {
        // Create temp arrays
        Document[] leftArray = new Document[mid - left + 1];
        Document[] rightArray = new Document[right - mid];
        
        System.arraycopy(array, left, leftArray, 0, leftArray.length);
        System.arraycopy(array, mid + 1, rightArray, 0, rightArray.length);
        
        // Merge the temp arrays
        int i = 0, j = 0, k = left;
        while (i < leftArray.length && j < rightArray.length) {
            if (leftArray[i].getTitle().compareToIgnoreCase(rightArray[j].getTitle()) <= 0) {
                array[k++] = leftArray[i++];
            } else {
                array[k++] = rightArray[j++];
            }
        }
        
        // Copy remaining elements
        while (i < leftArray.length) {
            array[k++] = leftArray[i++];
        }
        while (j < rightArray.length) {
            array[k++] = rightArray[j++];
        }
    }
}