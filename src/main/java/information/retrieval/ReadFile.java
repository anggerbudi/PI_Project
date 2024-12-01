package information.retrieval;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReadFile {

    private static final Logger logger = Logger.getLogger(ReadFile.class.getName());
    

    /**
     * Read documents in a folder and return the contents
     *
     * @param directoryPath path to the folder containing the documents
     * @return map of file names to file contents
     */
    public Map<String, String> ReadDocuments(String directoryPath) {
        Map<String, String> contents = new HashMap<>();
        File directory = new File(directoryPath);

        if (!directory.exists()) {
            logger.severe("Folder does not exist: " + directoryPath);
            return contents;
        }

        if (!directory.isDirectory()) {
            logger.severe("Path is not a directory: " + directoryPath);
            return contents;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            logger.severe("Failed to list files in the folder: " + directoryPath);
            return contents;
        }

        try {
            String canonicalFolderPath = directory.getCanonicalPath();

            for (File file : files) {
                if (file.isFile() && file.canRead()) {
                    String canonicalFilePath = file.getCanonicalPath();
                    if (!canonicalFilePath.startsWith(canonicalFolderPath)) {
                        logger.severe("File path is outside the folder: " + file.getName());
                        continue;
                    }

                    StringBuilder content = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new java.io.FileReader(file))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            content.append(line).append("\n");
                        }
                    } catch (IOException e) {
                        logger.severe("IO Exception while reading file: " + file.getName());
                    }
                    contents.put(file.getName(), content.toString());
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "IO Exception while processing folder: " + directoryPath, e);
        }

        return contents;
    }
}
