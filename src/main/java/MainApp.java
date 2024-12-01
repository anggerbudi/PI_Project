import jsastrawi.morphology.DefaultLemmatizer;
import jsastrawi.morphology.Lemmatizer;
import opennlp.tools.tokenize.Tokenizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class MainApp {
    
    private static Lemmatizer lemmatizer;
    private static Tokenizer tokenizer;

    public static void main(String[] args) {
        initializeLemmatizer();
        
        String dataPath = "src/data-koleksi";
        
        
        
    }
    
    private static void initializeLemmatizer() {
        Set<String> dictionary = new HashSet<String>();
        try (InputStream in = Lemmatizer.class.getResourceAsStream("/root-words.txt")) {
            assert in != null;
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            
            String line;
            while ((line = br.readLine()) != null) {
                dictionary.add(line);
            }
            
            lemmatizer = new DefaultLemmatizer(dictionary);
            System.out.println("Lemmatizer initialized successfully.");
        } catch (IOException e) {
            throw new RuntimeException("Error initializing lemmatizer.", e);
        }
    }
    
    private static void initializeTokenizer() {
        String modelPath = "src/data-model/langdetect-183.bin";
        
    }
}
