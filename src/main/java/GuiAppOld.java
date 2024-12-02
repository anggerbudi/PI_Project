import information.retrieval.*;
import information.retrieval.object.ObjectSearchResult;
import information.retrieval.utility.ResourceManager;
import information.retrieval.utility.Searching;
import information.retrieval.utility.WordProcessor;
import jsastrawi.morphology.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import opennlp.tools.tokenize.Tokenizer;

public class GuiAppOld extends JFrame {

    private static WordProcessor wordProcessor;
    private static WordList wordList;
    private static Searching searching;

    private static Lemmatizer lemmatizer;
    private static Tokenizer tokenizer;
    private static Set<String> stopwords;

    private static final Logger logger = Logger.getLogger(GuiAppOld.class.getName());

    private JTextArea resultArea;
    private JTextField keywordField;
    private JTextField directoryField;
    private JLabel directoryLabel;
    private JComboBox<String> searchOptionComboBox;

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

        wordProcessor = new WordProcessor(tokenizer, lemmatizer, stopwords);
        wordList = new WordList();

        try {
            wordProcessor.processDocuments(documentsPath, wordList);
            wordList.calculateTfidf();
            logger.log(Level.INFO, "Word list processed successfully.");

            // Initialize searching after processing the documents
            searching = new Searching(wordList, lemmatizer);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing word list.", e);
        }

        // Start the GUI application
        SwingUtilities.invokeLater(() -> new GuiAppOld().setVisible(true));
    }


    public GuiAppOld() {
        setTitle("Inverted Index Search");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Layout utama
        Container container = getContentPane();
        container.setLayout(new BorderLayout());

        JPanel keywordPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Label "Enter Keyword"
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        keywordPanel.add(new JLabel("Enter Keyword:"), gbc);

        // Field untuk memasukkan keyword
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        keywordField = new JTextField();
        keywordField.setPreferredSize(new Dimension(80, 25));
        keywordPanel.add(keywordField, gbc);

        // ComboBox untuk opsi pencarian
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        searchOptionComboBox = new JComboBox<>(new String[]{"Advanced", "AND", "OR", "Single"});
        keywordPanel.add(searchOptionComboBox, gbc);

        // Button Search
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        JButton searchButton = new JButton("Search");

        // Atur ukuran button dengan mengatur preferred size
        searchButton.setPreferredSize(new Dimension(80, 25));
        keywordPanel.add(searchButton, gbc);

        // Area hasil pencarian dalam bentuk tabel
        String[] columnNames = {"Document", "TF-IDF", "Terms"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable resultTable = new JTable(tableModel);
        JScrollPane resultScrollPane = new JScrollPane(resultTable);


        // Menambahkan panel ke container utama
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.add(keywordPanel);

        container.add(inputPanel, BorderLayout.NORTH);
        container.add(resultScrollPane, BorderLayout.CENTER);

        // Action Listeners
        searchButton.addActionListener(e -> searchDocuments(keywordField.getText(),
                (String) searchOptionComboBox.getSelectedItem(), tableModel));
    }


    /**
     * Handle the search action
     */
    private void searchDocuments(String keywords, String searchOption, DefaultTableModel tableModel) {
        if (!keywords.isEmpty() && searching != null) {
            Map<String, ObjectSearchResult> results = null;
            if ("Advanced".equals(searchOption)) {
                results = searching.searchAdvanced(keywords.split(","));
            } else if ("AND".equals(searchOption)) {
                results = searching.searchAND(keywords.split(","));
            } else if ("OR".equals(searchOption)) {
                results = searching.searchOR(keywords.split(","));
            } else if ("Single".equals(searchOption)) {
                results = searching.searchSingleTerm(keywords);
            }

            tableModel.setRowCount(0); // Clear previous results
            if (results != null && !results.isEmpty()) {
                results.forEach((docId, result) -> {
                    // Extract the cumulative TF-IDF and matched terms
                    double tfIdf = result.getCumulativeTfIdf(); // Assuming this method exists in ObjectSearchResult
                    int matchedTerms = result.getMatchedTermsCount(); // Assuming this method exists in ObjectSearchResult

                    // Add rows to the table
                    tableModel.addRow(new Object[]{docId, tfIdf, matchedTerms});
                });
            } else {
                tableModel.addRow(new Object[]{"No results", "No snippets available", ""});
            }
        } else {
            tableModel.addRow(new Object[]{"Error", "Please process documents first or enter a valid keyword.", ""});
        }
    }

}
