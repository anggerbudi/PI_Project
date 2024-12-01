package information.retrieval.object;

public class ObjectDocument {

    private final String documentID;
    private int termFrequency;
    private double tfidf;

    public ObjectDocument(String documentID) {
        this.documentID = documentID;
        this.termFrequency = 1;
        this.tfidf = 0.0;
    }

    public int getTermFrequency() {
        return termFrequency;
    }

    public double getTfidf() {
        return tfidf;
    }

    public void setTfidf(double tfidf) {
        this.tfidf = tfidf;
    }

    public void incrementTermFrequency() {
        termFrequency++;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ObjectDocument that = (ObjectDocument) obj;
        return documentID.equals(that.documentID);
    }

    @Override
    public int hashCode() {
        return documentID.hashCode();
    }
}
