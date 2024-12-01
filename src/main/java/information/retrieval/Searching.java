package information.retrieval;

import jsastrawi.morphology.Lemmatizer;

import java.util.*;
import java.util.logging.Level;
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
     *
     * @param term The term to search for.
     * @return A map of document IDs and their corresponding TF-IDF values.
     */
    public Map<String, SearchResult> searchSingleTerm(String term) {
        String lemmatizedTerm = lemmatizeTerm(term);
        ObjectTerm objectTerm = new ObjectTerm(lemmatizedTerm);
        Map<String, ObjectDocument> postingList = wordList.getInvertedIndex().get(objectTerm);

        if (postingList == null) {
            logger.log(Level.INFO, "Term '" + term + "' not found in the word list.");
            return Collections.emptyMap();
        }

        Map<String, SearchResult> results = new HashMap<>();
        for (Map.Entry<String, ObjectDocument> entry : postingList.entrySet()) {
            String docId = entry.getKey();
            double tfidf = entry.getValue().getTfidf();
            results.put(docId, new SearchResult(docId));
            results.get(docId).update(tfidf, lemmatizedTerm);
        }

        return sortByRankDescending(results);
    }


    /**
     * Search for multiple terms in the word list using "AND" and rank the results.
     *
     * @param terms The terms to search for.
     * @return A map of document IDs and their corresponding cumulative TF-IDF values.
     */
    public Map<String, SearchResult> searchAND(String[] terms) {
        Map<ObjectTerm, Map<String, ObjectDocument>> invertedIndex = wordList.getInvertedIndex();

        Set<String> commonDocs = new HashSet<>();
        Map<String, SearchResult> results = new HashMap<>();

        for (int i = 0; i < terms.length; i++) {
            String lemmatizedterm = lemmatizeTerm(terms[i]);
            ObjectTerm objectTerm = new ObjectTerm(lemmatizedterm);
            Map<String, ObjectDocument> postingList = invertedIndex.get(objectTerm);

            if (postingList == null) {
                logger.log(Level.INFO, "Term '" + terms[i] + "' not found in the word list.");
                return Collections.emptyMap();
            }

            if (i==0) {
                commonDocs.addAll(postingList.keySet());
            } else {
                commonDocs.retainAll(postingList.keySet());
            }
            
            if (commonDocs.isEmpty()) {
                logger.log(Level.INFO, "No documents found for terms containing all terms.");
                return Collections.emptyMap();
            }

            for (String docId : commonDocs) {
                double tfidf = postingList.get(docId).getTfidf();
                results.computeIfAbsent(docId, SearchResult::new).update(tfidf, lemmatizedterm);
            }
        }

        return sortByRankDescending(results);
    }


    /**
     * Search for multiple terms in the word list using "OR" and return the results.
     *
     * @param terms The terms to search for.
     * @return A map of document IDs and their corresponding TF-IDF values.
     */
    public Map<String, SearchResult> searchOR(String[] terms) {
        Map<ObjectTerm, Map<String, ObjectDocument>> invertedIndex = wordList.getInvertedIndex();
        Map<String, SearchResult> results = new HashMap<>();

        for (String term : terms) {
            String lemmatizedTerm = lemmatizeTerm(term);
            ObjectTerm objectTerm = new ObjectTerm(lemmatizedTerm);
            Map<String, ObjectDocument> postingList = invertedIndex.get(objectTerm);

            if (postingList == null) continue;

            for (Map.Entry<String, ObjectDocument> entry : postingList.entrySet()) {
                String docId = entry.getKey();
                double tfidf = entry.getValue().getTfidf();
                results.computeIfAbsent(docId, SearchResult::new).update(tfidf, lemmatizedTerm);
            }
        }
        return sortByRankDescending(results);
    }


    /**
     * Search for multiple terms in the word list using "AND" and "OR" and return the results.
     *
     * @param terms The terms to search for.
     * @return A map of document IDs and their corresponding TF-IDF values.
     */
    public Map<String, SearchResult> searchAdvanced(String[] terms) {
        Map<ObjectTerm, Map<String, ObjectDocument>> invertedIndex = wordList.getInvertedIndex();

        Map<String, SearchResult> results = new HashMap<>();
        Set<String> missingTerms = new HashSet<>();

        for (String term : terms) {
            String lemmatizedTerm = lemmatizeTerm(term);
            ObjectTerm objectTerm = new ObjectTerm(lemmatizedTerm);
            Map<String, ObjectDocument> postingList = invertedIndex.get(objectTerm);

            if (postingList == null) {
                missingTerms.add(term);
            } else {
                for (Map.Entry<String, ObjectDocument> entry : postingList.entrySet()) {
                    String docId = entry.getKey();
                    double tfidf = entry.getValue().getTfidf();

                    results.computeIfAbsent(docId, SearchResult::new).update(tfidf, lemmatizedTerm);
                }
            }
        }

        List<SearchResult> sortedResults = new ArrayList<>(results.values());
        sortedResults.sort((r1, r2) -> {
            int termComparison = Integer.compare(r2.getMatchedTermsCount(), r1.getMatchedTermsCount());
            return termComparison != 0 ? termComparison : Double.compare(r2.getCumulativeTfIdf(), r1.getCumulativeTfIdf());
        });

        Map<String, SearchResult> sortedResultsMap = new LinkedHashMap<>();
        for (SearchResult result : sortedResults) {
            sortedResultsMap.put(result.getDocumentId(), result);
        }

        if (!missingTerms.isEmpty()) {
            System.out.println("The following terms were not found in the search: " + missingTerms);
        }

        return sortedResultsMap;
    }


    /**
     * Sort a map by value in descending order.
     *
     * @param unsortedResults The unsorted map.
     * @return The sorted map.
     */
    private Map<String, SearchResult> sortByRankDescending(Map<String, SearchResult> unsortedResults) {
        List<Map.Entry<String, SearchResult>> sortedEntries = new ArrayList<>(unsortedResults.entrySet());
        sortedEntries.sort((e1, e2) -> Double.compare(e2.getValue().getCumulativeTfIdf(), e1.getValue().getCumulativeTfIdf()));

        Map<String, SearchResult> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, SearchResult> entry : sortedEntries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }


    /**
     * Lemmatize a term.
     *
     * @param term The term to lemmatize.
     * @return The lemmatized term.
     */
    private String lemmatizeTerm(String term) {
        return lemmatizer.lemmatize(term.toLowerCase());
    }
}
