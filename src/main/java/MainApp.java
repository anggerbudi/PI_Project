import information.retrieval.WordList;
import information.retrieval.object.ObjectSearchResult;
import information.retrieval.utility.ResourceManager;
import information.retrieval.utility.Searching;
import information.retrieval.utility.WordProcessor;
import jsastrawi.morphology.Lemmatizer;
import opennlp.tools.tokenize.Tokenizer;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainApp {

    private static final Logger logger = Logger.getLogger(MainApp.class.getName());

    public static void main(String[] args) {

        ResourceManager resourceManager = new ResourceManager();

        Properties config = resourceManager.loadConfiguration("src/main/resources/config.properties");

        String tokenizerPath = config.getProperty("tokenizer.path");
        String stopwordsPath = config.getProperty("stopwords.path");
        String documentsPath = config.getProperty("documents.path");

        Tokenizer tokenizer = resourceManager.initializeTokenizer(tokenizerPath);
        Lemmatizer lemmatizer = resourceManager.initializeLemmatizer();
        Set<String> stopwords = resourceManager.loadStopWords(stopwordsPath);

        if (tokenizer == null || lemmatizer == null || stopwords == null) {
            logger.log(Level.SEVERE, "Error initializing resources.");
            return;
        }

        WordProcessor wordProcessor = new WordProcessor(tokenizer, lemmatizer, stopwords);
        WordList wordList = new WordList();

        try {
            wordProcessor.processDocuments(documentsPath, wordList);
            wordList.calculateTfidf();
            logger.log(Level.INFO, "Word list processed successfully.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing word list.", e);
        }

        Searching searching = new Searching(wordList, lemmatizer);
        
        long startTime, endTime;

        startTime = System.currentTimeMillis();
        Map<String, ObjectSearchResult> singleTermResult = searching.searchSingleTerm("cuaca");
        endTime = System.currentTimeMillis();

        System.out.println("Time taken to search for term 'cuaca': " + (endTime - startTime) + " ms");
        System.out.println("Search result for term 'cuaca':");
        ObjectSearchResult.printResults(singleTermResult);

        startTime = System.currentTimeMillis();
        Map<String, ObjectSearchResult> andResult = searching.searchAND(new String[]{"cuaca", "hujan"});
        endTime = System.currentTimeMillis();

        System.out.println("Time taken to search for terms 'cuaca' and 'hujan': " + (endTime - startTime) + " ms");
        System.out.println("\nSearch result for terms 'cuaca' and 'hujan':");
        ObjectSearchResult.printResults(andResult);
        
        startTime = System.currentTimeMillis();
        Map<String, ObjectSearchResult> orResult = searching.searchOR(new String[]{"cuaca", "hujan"});
        endTime = System.currentTimeMillis();

        System.out.println("Time taken to search for terms 'cuaca' or 'hujan': " + (endTime - startTime) + " ms");
        System.out.println("\nSearch result for terms 'cuaca' or 'hujan':");
        ObjectSearchResult.printResults(orResult);

        String[] searchTerms = new String[]{"cuaca", "hujan", "pesta", "acara"};
        startTime = System.currentTimeMillis();
        Map<String, ObjectSearchResult> advancedResult = searching.searchAdvanced(searchTerms);
        endTime = System.currentTimeMillis();
        
        System.out.println("Time taken to search for terms '" + String.join("', '", searchTerms) + "': " + (endTime - startTime) + " ms");
        System.out.println("\nAdvanced search result for terms '" + String.join("', '", searchTerms) + "':");
        ObjectSearchResult.printResults(advancedResult);
    }
}
