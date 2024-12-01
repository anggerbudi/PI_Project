package information.retrieval;

import information.retrieval.object.ObjectDocument;
import information.retrieval.object.ObjectTerm;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WordList {

    private static final Logger logger = Logger.getLogger(WordList.class.getName());

    private final Map<ObjectTerm, Map<String, ObjectDocument>> invertedIndex;
    private int totalDocuments;

    public WordList() {
        this.invertedIndex = new TreeMap<>();
        this.totalDocuments = 0;
    }


    /**
     * Add terms to the word list
     *
     * @param documentID document ID
     * @param tokens     array of tokens
     */
    public void addTerm(String documentID, String[] tokens) {
        try {
            totalDocuments++;
            for (String token : tokens) {
                ObjectTerm term = new ObjectTerm(token);
                Map<String, ObjectDocument> postingList = invertedIndex.computeIfAbsent(term, k -> new TreeMap<>());

                ObjectDocument document = postingList.computeIfAbsent(documentID, k -> {
                    term.incrementDocumentFrequency();
                    return new ObjectDocument(documentID);
                });

                document.incrementTermFrequency();
            }
        } catch (Exception e) {
            logger.severe("Error adding term to word list for document: " + documentID);
        }
    }


    /**
     * Calculate TF-IDF for each term in the word list
     */
    public void calculateTfidf() {
        try {
            for (Map.Entry<ObjectTerm, Map<String, ObjectDocument>> entry : invertedIndex.entrySet()) {
                ObjectTerm term = entry.getKey();
                Map<String, ObjectDocument> postingList = entry.getValue();

                double idf = Math.log10((double) totalDocuments / term.getDocumentFrequency());

                for (ObjectDocument document : postingList.values()) {
                    double tf = document.getTermFrequency();
                    document.setTfidf(tf * idf);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error calculating TF-IDF", e);
        }
    }


    /**
     * Get a copy of the inverted index to ensure encapsulation
     *
     * @return inverted index
     */
    public Map<ObjectTerm, Map<String, ObjectDocument>> getInvertedIndex() {
        Map<ObjectTerm, Map<String, ObjectDocument>> copy = new TreeMap<>();
        for (Map.Entry<ObjectTerm, Map<String, ObjectDocument>> entry : invertedIndex.entrySet()) {
            Map<String, ObjectDocument> postingListCopy = new TreeMap<>(entry.getValue());
            copy.put(entry.getKey(), postingListCopy);
        }
        return Collections.unmodifiableMap(copy);
    }


}
