package information.retrieval.utility;

import information.retrieval.WordList;
import jsastrawi.morphology.Lemmatizer;
import opennlp.tools.tokenize.Tokenizer;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WordProcessor {

    private static final Logger logger = Logger.getLogger(WordProcessor.class.getName());
    private final Tokenizer tokenizer;
    private final Lemmatizer lemmatizer;
    private final Set<String> stopwords;

    public WordProcessor(Tokenizer tokenizer, Lemmatizer lemmatizer, Set<String> stopwords) {
        this.tokenizer = tokenizer;
        this.lemmatizer = lemmatizer;
        this.stopwords = stopwords;
    }


    /**
     * Process text by tokenizing, lowercasing, removing stopwords, and lemmatizing
     *
     * @param text input text that want to be processed
     * @return array of processed tokens
     */
    public String[] processText(String text) {
        String[] tokens = tokenizer.tokenize(text);

        return Arrays.stream(tokens)
                .map(String::toLowerCase)
                .filter(token -> !stopwords.contains(token))
                .map(lemmatizer::lemmatize)
                .toArray(String[]::new);
    }


    /**
     * Process documents in a folder by reading the files and adding the terms to the word list
     *
     * @param folderPath path to the folder containing the documents
     * @param wordList   word list to add the terms to
     */
    public void processDocuments(String folderPath, WordList wordList) {
        ReadFile readFile = new ReadFile();
        Map<String, String> fileContents = readFile.ReadDocuments(folderPath);

        if (fileContents.isEmpty()) {
            logger.log(Level.INFO, "No files found in the folder.");
            return;
        }

        for (Map.Entry<String, String> entry : fileContents.entrySet()) {
            String fileName = entry.getKey();
            if (fileName.endsWith(".txt")) {
                fileName = fileName.substring(0, fileName.length() - 4);
            }
            String content = entry.getValue();
            String[] tokens = processText(content);
            wordList.addTerm(fileName, tokens);
        }
    }
}
