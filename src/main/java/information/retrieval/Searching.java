package information.retrieval;

import jsastrawi.morphology.Lemmatizer;

import java.util.*;
import java.util.logging.Logger;

public class Searching {
    
    private final WordList wordList;
    private final Lemmatizer lemmatizer;
    
    private static final Logger logger = Logger.getLogger(Searching.class.getName());
    
    public Searching(WordList wordList, Lemmatizer lemmatizer) {
        this.wordList = wordList;
        this.lemmatizer = lemmatizer;
    }
    
    
    /**
     * Search for a single term in the word list.
     * @param term The term to search for.
     * @return A map of document IDs and their corresponding TF-IDF values.
     */
    public Map<String, Double> searchSingleTerm(String term) {
        ObjectTerm objectTerm = new ObjectTerm(term);
        Map<String, ObjectDocument> result = wordList.getInvertedIndex().get(objectTerm);
        
        if (result == null) {
            logger.log(java.util.logging.Level.INFO, "Term '" + term + "' not found in the word list.");
            return Collections.emptyMap();
        }
        
        Map<String, Double> rankedResult = new HashMap<>();
        for (Map.Entry<String, ObjectDocument> entry : result.entrySet()) {
            rankedResult.put(entry.getKey(), entry.getValue().getTfidf());
        }
        
        return sortByRankDescending(rankedResult);
    }
    
    
    /**
     * Search for multiple terms in the word list using "AND" and rank the results.
     * @param terms The terms to search for.
     * @return A map of document IDs and their corresponding cumulative TF-IDF values.
     */
    public Map<String, Double> searchAndRanked(String[] terms) {
        Map<ObjectTerm, Map<String, ObjectDocument>> invertedIndex = wordList.getInvertedIndex();
        
        Map<String, ObjectDocument> result = null;
        
        for (String term : terms) {
            String lemmatizedterm = lemmatizeTerm(term);
            ObjectTerm objectTerm = new ObjectTerm(lemmatizedterm);
            Map<String, ObjectDocument> currentPostingList = invertedIndex.get(objectTerm);
            
            if (currentPostingList == null) {
                logger.log(java.util.logging.Level.INFO, "Term '" + term + "' not found in the word list.");
                return Collections.emptyMap();
            }
            
            if (result == null) {
                result = new HashMap<>(currentPostingList);
            } else {
                result.keySet().removeIf(docID -> !currentPostingList.containsKey(docID));
            }
            
            if (result.isEmpty()) {
                logger.log(java.util.logging.Level.INFO, "No documents found containing all terms.");
                return Collections.emptyMap();
            }
        }
        
        Map<String, Double> rankedResult = new HashMap<>();
        assert result != null;
        for (String docId : result.keySet()) {
            double cumulativeScore = 0.0;
            
            for (String term : terms) {
                String lemmatizedTerm = lemmatizeTerm(term);
                ObjectTerm objectTerm = new ObjectTerm(lemmatizedTerm);
                Map<String, ObjectDocument> postingList = invertedIndex.get(objectTerm);
                if (postingList != null && postingList.containsKey(docId)) {
                    cumulativeScore += postingList.get(docId).getTfidf();
                }
            }
            rankedResult.put(docId, cumulativeScore);
        }
        return sortByRankDescending(rankedResult);
    }
    
    
    /**
     * Search for multiple terms in the word list using "OR" and return the results.
     * @param terms The terms to search for.
     * @return A map of document IDs and their corresponding TF-IDF values.
     */
    public Map<String, Double> searchOrRanked(String[] terms) {
        Map<ObjectTerm, Map<String, ObjectDocument>> invertedIndex = wordList.getInvertedIndex();
        
        Map<String, Double> rankedResult = new HashMap<>();
        
        for (String term : terms) {
            String lemmatizedTerm = lemmatizeTerm(term);
            ObjectTerm objectTerm = new ObjectTerm(lemmatizedTerm);
            Map<String, ObjectDocument> currentPostingList = invertedIndex.get(objectTerm);
            
            if (currentPostingList != null) {
                for (Map.Entry<String, ObjectDocument> entry : currentPostingList.entrySet()) {
                    String docId = entry.getKey();
                    double tfidf = entry.getValue().getTfidf();
                    
                    rankedResult.merge(docId, tfidf, Double::sum);
                }
            }
        }
        return sortByRankDescending(rankedResult);
    }
    
    
    /**
     * Sort a map by value in descending order.
     * @param unsortedMap The unsorted map.
     * @return The sorted map.
     */
    private Map<String, Double> sortByRankDescending(Map<String, Double> unsortedMap) {
        List<Map.Entry<String, Double>> list = new ArrayList<>(unsortedMap.entrySet());
        list.sort((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()));
        
        Map<String, Double> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
    
    private String lemmatizeTerm(String term) {
        return lemmatizer.lemmatize(term.toLowerCase());
    }
}
