package information.retrieval;

import jsastrawi.morphology.DefaultLemmatizer;
import jsastrawi.morphology.Lemmatizer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import java.io.*;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResourceManager {
    
    private static final Logger logger = Logger.getLogger(ResourceManager.class.getName());

    
    /**
     * Initialize tokenizer.
     * 
     * @param modelPath Path to tokenizer model.
     * @return Tokenizer object.
     */
    public Tokenizer initializeTokenizer(String modelPath) {
        try (InputStream modelIn = new FileInputStream(modelPath)) {
            TokenizerModel model = new TokenizerModel(modelIn);
            logger.log(Level.INFO, "Tokenizer initialized successfully.");
            return new TokenizerME(model);
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "Tokenizer model not found: " + modelPath, e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading tokenizer model: " + modelPath, e);
        }
        return null;
    }

    
    /**
     * Initialize lemmatizer.
     * 
     * @return Lemmatizer object.
     */
    public Lemmatizer initializeLemmatizer() {
        Set<String> dictionary = new HashSet<>();
        try (InputStream in = Lemmatizer.class.getResourceAsStream("/root-words.txt")) {
            assert in != null;
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                dictionary.add(line);
            }
            logger.log(Level.INFO, "Lemmatizer initialized successfully.");
            return new DefaultLemmatizer(dictionary);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error initializing lemmatizer.", e);
        }
        return null;
    }

    
    /**
     * Load stopwords from file.
     * 
     * @param stopWordsPath Path to stopwords file.
     * @return Set of stopwords.
     */
    public Set<String> loadStopWords(String stopWordsPath) {
        Set<String> stopwords = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(stopWordsPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stopwords.add(line.trim());
            }
            logger.log(Level.INFO, "Stopwords loaded successfully.");
            return stopwords;
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "Stopwords file not found: " + stopWordsPath, e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error reading stopwords file: " + stopWordsPath, e);
        }
        return null;
    }

    
    /**
     * Load configuration from file.
     * 
     * @param configFilePath Path to configuration file.
     * @return Properties object containing configuration.
     */
    public Properties loadConfiguration(String configFilePath) {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(configFilePath)) {
            properties.load(input);
            logger.log(Level.INFO, "Configuration file loaded successfully.");
            return properties;
        } catch (FileNotFoundException e) {
            logger.log(Level.SEVERE, "Configuration file not found: " + configFilePath, e);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading configuration file: " + configFilePath, e);
        }
        return null;
    }
}
