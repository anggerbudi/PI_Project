import information.retrieval.WordList;
import information.retrieval.object.ObjectSearchResult;
import information.retrieval.utility.ResourceManager;
import information.retrieval.utility.Searching;
import information.retrieval.utility.WordProcessor;
import jsastrawi.morphology.Lemmatizer;
import opennlp.tools.tokenize.Tokenizer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GuiApp extends JFrame {

    private static final Logger logger = Logger.getLogger(GuiApp.class.getName());
    private static Searching searching;

    private JTextField keywordField;
    private JComboBox<String> searchOptionComboBox;

    public GuiApp() {
        setTitle("Inverted Index Search");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Container container = getContentPane();
        container.setLayout(new BorderLayout());

        JPanel keywordPanel = createKeywordPanel();
        JScrollPane resultScrollPane = createResultScrollPane();

        container.add(keywordPanel, BorderLayout.NORTH);
        container.add(resultScrollPane, BorderLayout.CENTER);
    }

    private JPanel createKeywordPanel() {
        JPanel keywordPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        keywordPanel.add(new JLabel("Enter Keyword:"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        keywordField = new JTextField();
        keywordField.setPreferredSize(new Dimension(80, 25));
        keywordPanel.add(keywordField, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        searchOptionComboBox = new JComboBox<>(new String[]{"Advanced", "AND", "OR", "Single"});
        keywordPanel.add(searchOptionComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        JButton searchButton = new JButton("Search");
        searchButton.setPreferredSize(new Dimension(80, 25));
        keywordPanel.add(searchButton, gbc);

        searchButton.addActionListener(e -> searchDocuments(keywordField.getText(),
                (String) searchOptionComboBox.getSelectedItem()));

        return keywordPanel;
    }

    private JScrollPane createResultScrollPane() {
        String[] columnNames = {"Document", "TF-IDF", "Terms"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable resultTable = new JTable(tableModel);
        return new JScrollPane(resultTable);
    }

    public static void main(String[] args) {
        ResourceManager resourceManager = new ResourceManager();

        Properties config = resourceManager.loadConfiguration("src/main/resources/config.properties");
        if (config == null) {
            logger.log(Level.SEVERE, "Configuration file not found or invalid.");
            return;
        }

        String tokenizerPath = config.getProperty("tokenizer.path");
        String stopwordsPath = config.getProperty("stopwords.path");
        String documentsPath = config.getProperty("documents.path");

        try {
            Tokenizer tokenizer = resourceManager.initializeTokenizer(tokenizerPath);
            Lemmatizer lemmatizer = resourceManager.initializeLemmatizer();
            Set<String> stopwords = resourceManager.loadStopWords(stopwordsPath);

            if (tokenizer == null || lemmatizer == null || stopwords == null) {
                throw new Exception("Failed to initialize required resources.");
            }

            WordProcessor wordProcessor = new WordProcessor(tokenizer, lemmatizer, stopwords);
            WordList wordList = new WordList();
            wordProcessor.processDocuments(documentsPath, wordList);
            wordList.calculateTfidf();

            searching = new Searching(wordList, lemmatizer);
            logger.log(Level.INFO, "Application initialized successfully.");

            // Start GUI
            SwingUtilities.invokeLater(() -> new GuiApp().setVisible(true));

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error initializing application.", e);
        }
    }

    private void searchDocuments(String keywords, String searchOption) {
        if (keywords.isEmpty() || searching == null) {
            showError("Please process documents first or enter a valid keyword.");
            return;
        }

        Map<String, ObjectSearchResult> results;
        switch (searchOption) {
            case "Advanced":
                results = searching.searchAdvanced(keywords.split("[,\\s]+"));
                break;
            case "AND":
                results = searching.searchAND(keywords.split("[,\\s]+"));
                break;
            case "OR":
                results = searching.searchOR(keywords.split("[,\\s]+"));
                break;
            case "Single":
                results = searching.searchSingleTerm(keywords);
                break;
            default:
                showError("Invalid search option selected.");
                return;
        }

        displaySearchResults(results);
    }

    private void displaySearchResults(Map<String, ObjectSearchResult> results) {
        DefaultTableModel tableModel = (DefaultTableModel) ((JTable) ((JScrollPane) getContentPane()
                .getComponent(1)).getViewport().getView()).getModel();
        tableModel.setRowCount(0);

        if (results == null || results.isEmpty()) {
            tableModel.addRow(new Object[]{"No results", "No snippets available", ""});
        } else {
            results.forEach((docId, result) -> tableModel.addRow(new Object[]{
                    docId, result.getCumulativeTfIdf(), result.getMatchedTermsCount()
            }));
        }
    }

    private void showError(String message) {
        DefaultTableModel tableModel = (DefaultTableModel) ((JTable) ((JScrollPane) getContentPane()
                .getComponent(1)).getViewport().getView()).getModel();
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[]{"Error", message, ""});
    }
}
