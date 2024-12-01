package information.retrieval;

public class ObjectTerm implements Comparable<ObjectTerm> {
    
    private final String term;
    private int documentFrequency;
    
    public ObjectTerm(String term) {
        this.term = term;
        this.documentFrequency = 0;
    }

    public String getTerm() {
        return term;
    }

    public int getDocumentFrequency() {
        return documentFrequency;
    }
    
    public void incrementDocumentFrequency() {
        documentFrequency++;
    }

    @Override
    public int compareTo(ObjectTerm otherTerm) {
        return term.compareTo(otherTerm.term);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ObjectTerm that = (ObjectTerm) obj;
        return term.equals(that.term);
    }
    
    @Override
    public int hashCode() {
        return term.hashCode();
    }
}
