package information.retrieval;

import java.util.*;
import java.util.logging.Logger;

public class Searching {
    
    private final WordList wordList;
    
    private static final Logger logger = Logger.getLogger(Searching.class.getName());
    
    public Searching(WordList wordList) {
        this.wordList = wordList;
    }
    
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
    
    private Map<String, Double> sortByRankDescending(Map<String, Double> unsortedMap) {
        List<Map.Entry<String, Double>> list = new ArrayList<>(unsortedMap.entrySet());
        list.sort((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()));
        
        Map<String, Double> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
}
