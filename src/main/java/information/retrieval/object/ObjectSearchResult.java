package information.retrieval.object;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ObjectSearchResult {

    private final String documentId;
    private double cumulativeTfIdf;
    private final Set<String> matchedTerms;

    public ObjectSearchResult(String documentId) {
        this.documentId = documentId;
        this.cumulativeTfIdf = 0.0;
        this.matchedTerms = new HashSet<>();
    }

    public void update(double tfIdf, String term) {
        this.cumulativeTfIdf += tfIdf;
        this.matchedTerms.add(term);
    }

    public String getDocumentId() {
        return documentId;
    }

    public double getCumulativeTfIdf() {
        return cumulativeTfIdf;
    }

    public int getMatchedTermsCount() {
        return matchedTerms.size();
    }

    @Override
    public String toString() {
        return String.format("Document: %s, Cumulative TF-IDF: %.4f, Matched terms: %d", documentId, cumulativeTfIdf, matchedTerms.size());
    }

    public static void printResults(Map<String, ? extends ObjectSearchResult> results) {
        if (results.isEmpty()) {
            System.out.println("No results found.");
            return;
        }

        System.out.printf("%-15s %-20s %-10s%n", "Document ID", "Cumulative TF-IDF", "Matched Terms");
        System.out.println("-------------------------------------------------------");

        for (ObjectSearchResult result : results.values()) {
            System.out.printf("%-15s %-20.4f %-10d%n", result.getDocumentId(), result.getCumulativeTfIdf(), result.getMatchedTermsCount());
        }
    }
}
