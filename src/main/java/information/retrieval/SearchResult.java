package information.retrieval;

import java.util.HashSet;
import java.util.Set;

public class SearchResult {
    
    private final String documentId;
    private double cumulativeTfIdf;
    private final Set<String> matchedTerms;
    
    public SearchResult(String documentId) {
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
}
