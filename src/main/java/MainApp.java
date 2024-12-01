import information.retrieval.*;
import jsastrawi.morphology.*;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainApp {

    private static Lemmatizer lemmatizer;
    private static Tokenizer tokenizer;
    private static Set<String> stopwords;

    private static final Logger logger = Logger.getLogger(MainApp.class.getName());

    public static void main(String[] args) {
        initializeTokenizer();
        initializeLemmatizer();
        loadStopWords();

        WordProcessor wordProcessor = new WordProcessor(tokenizer, lemmatizer, stopwords);
        WordList wordList = new WordList();
        
        try {
            wordProcessor.processDocuments("src/data-koleksi", wordList);
            wordList.calculateTfidf();
            logger.log(Level.INFO, "Word list processed successfully.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing word list.", e);
        }
        
        Searching searching = new Searching(wordList, lemmatizer);
        
        Map<String, Double> result = searching.searchSingleTerm("cuaca");
        System.out.println("Search result for term 'cuaca':");
        for (Map.Entry<String, Double> entry : result.entrySet()) {
            System.out.println("Document ID: " + entry.getKey() + ", TF-IDF: " + entry.getValue());
        }
        
        Map<String, Double> result2 = searching.searchAndRanked(new String[]{"cuaca", "hujan"});
        System.out.println("\nSearch result for terms 'cuaca' and 'hujan':");
        for (Map.Entry<String, Double> entry : result2.entrySet()) {
            System.out.println("Document ID: " + entry.getKey() + ", Cumulative TF-IDF: " + entry.getValue());
        }
        
        Map<String, Double> result3 = searching.searchOrRanked(new String[]{"cuaca", "hujan"});
        System.out.println("\nSearch result for terms 'cuaca' or 'hujan':");
        for (Map.Entry<String, Double> entry : result3.entrySet()) {
            System.out.println("Document ID: " + entry.getKey() + ", Cumulative TF-IDF: " + entry.getValue());
        }
    }

    
    /**
     * Initialize the lemmatizer
     */
    private static void initializeLemmatizer() {
        Set<String> dictionary = new HashSet<>();
        try (InputStream in = Lemmatizer.class.getResourceAsStream("/root-words.txt")) {
            assert in != null;
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                dictionary.add(line);
            }
            lemmatizer = new DefaultLemmatizer(dictionary);
            logger.log(Level.INFO, "Lemmatizer initialized.");
        } catch (IOException e) {
            throw new RuntimeException("Error initializing lemmatizer.", e);
        }
    }

    
    /**
     * Initialize the tokenizer
     */
    private static void initializeTokenizer() {
        String modelPath = "src/data-model/id-token.bin";
        try (InputStream modelIn = new FileInputStream(modelPath)) {
            TokenizerModel model = new TokenizerModel(modelIn);
            tokenizer = new TokenizerME(model);
            logger.log(Level.INFO, "Tokenizer initialized.");
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "Tokenizer model not found: " + modelPath, e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading tokenizer model: " + modelPath, e);
        }
    }

    /**
     * Load stopwords from file
     */
    private static void loadStopWords() {
        stopwords = new HashSet<>();
        String stopwordsPath = "src/data-model/stopwordbahasa.csv";
        try (BufferedReader reader = new BufferedReader(new FileReader(stopwordsPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stopwords.add(line);
            }
            logger.log(Level.INFO, "Stopwords loaded successfully.");
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "Stopwords file not found: " + stopwordsPath, e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading stopwords: " + stopwordsPath, e);
        }
    }
}
