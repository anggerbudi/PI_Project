import information.retrieval.*;
import jsastrawi.morphology.*;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.*;
import java.util.HashSet;
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
        String modelPath = "src/data-model/langdetect-183.bin";
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
