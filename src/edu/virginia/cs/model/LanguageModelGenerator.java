/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.virginia.cs.model;

import edu.virginia.cs.utility.DMOZDataReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import edu.virginia.cs.utility.StringTokenizer;
import org.xml.sax.SAXException;

/**
 *
 * @author Wasi
 */
public class LanguageModelGenerator {

    private HashMap<String, ArrayList<String>> DMOZdata;
    private HashMap<String, Integer> DictionaryOfUnigrams;
    private HashMap<String, Integer> DictionaryOfBigrams;
    private HashMap<String, Integer> DictionaryOfTrigrams;
    private HashMap<String, Integer> DictionaryOfFourGrams;
    private int NumberOfUnigrams;
    private int NumberOfBigrams;
    private int NumberOfTrigrams;
    private int NumberOfFourGrams;
    private final StringTokenizer tokenizer;

    public LanguageModelGenerator() {
        /* No stopword removal and no stemming during language model generation */
        tokenizer = new StringTokenizer(false, false);
    }

    /**
     * Initialize every variable.
     */
    private void reset() {
        DictionaryOfUnigrams = new HashMap<>();
        DictionaryOfBigrams = new HashMap<>();
        DictionaryOfTrigrams = new HashMap<>();
        DictionaryOfFourGrams = new HashMap<>();
        NumberOfUnigrams = 0;
        NumberOfBigrams = 0;
        NumberOfTrigrams = 0;
        NumberOfFourGrams = 0;
    }

    /**
     * Initialize every variable.
     *
     * @param document content of a document
     * @throws java.IOException
     */
    private void analyzeDocument(String document) throws IOException {
        String previousTrigram = ""; // for four-grams
        String previousBigram = ""; // for trigrams
        String previousUnigram = ""; // for bigrams
        List<String> tokens = tokenizer.TokenizeString(document);
        for (String token : tokens) {
            if (!token.isEmpty()) {
                if (DictionaryOfUnigrams.containsKey(token)) {
                    DictionaryOfUnigrams.put(token, DictionaryOfUnigrams.get(token) + 1);
                } else {
                    DictionaryOfUnigrams.put(token, 1);
                }
                NumberOfUnigrams++;
                // generating bigrams
                if (!previousUnigram.isEmpty()) {
                    String bigram = previousUnigram + " " + token;
                    if (DictionaryOfBigrams.containsKey(bigram)) {
                        DictionaryOfBigrams.put(bigram, DictionaryOfBigrams.get(bigram) + 1);
                    } else {
                        DictionaryOfBigrams.put(bigram, 1);
                    }
                    NumberOfBigrams++;
                }
                // generating trigrams
                if (!previousBigram.isEmpty()) {
                    String trigram = previousBigram + " " + token;
                    if (DictionaryOfTrigrams.containsKey(trigram)) {
                        DictionaryOfTrigrams.put(trigram, DictionaryOfTrigrams.get(trigram) + 1);
                    } else {
                        DictionaryOfTrigrams.put(trigram, 1);
                    }
                    NumberOfTrigrams++;
                }
                // generating four-grams
                if (!previousTrigram.isEmpty()) {
                    String fourgram = previousTrigram + " " + token;
                    if (DictionaryOfFourGrams.containsKey(fourgram)) {
                        DictionaryOfFourGrams.put(fourgram, DictionaryOfFourGrams.get(fourgram) + 1);
                    } else {
                        DictionaryOfFourGrams.put(fourgram, 1);
                    }
                    NumberOfFourGrams++;
                }
                if (!previousBigram.isEmpty()) {
                    previousTrigram = previousBigram + " " + token;
                }
                if (!previousUnigram.isEmpty()) {
                    previousBigram = previousUnigram + " " + token;
                }
                previousUnigram = token;
            }
        }
    }

    /**
     * Creates a directory at a specified path.
     *
     * @param path
     */
    private void createDirectory(String path) {
        File f = new File(path);
        if (!f.exists()) {
            if (f.mkdirs()) {
            } else {
                System.out.println(f.getName() + " - Failed to create directory!");
            }
        }
    }

    /**
     * Reads all files and folders from a zip or rar file.
     *
     * @param filepath
     * @param filename
     * @throws Throwable
     */
    private void LoadZipFile(String filepath, String filename) throws Throwable {
        String currentFolder = filename.substring(0, filename.lastIndexOf("."));
        String currentFolderPath = "data/language_models/" + currentFolder;
        createDirectory(currentFolderPath);
        Charset CP437 = Charset.forName("CP437");
        ZipFile zipFile = new ZipFile(filepath, CP437);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        int numberOfFiles = 0;
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            int count = entry.getName().length() - entry.getName().replace("/", "").length();
            if (count > 3) {
                continue;
            }
            if (entry.isDirectory()) {
                File folder = new File(entry.getName());
                String path = folder.getPath();
                createDirectory("data/language_models/" + path);
                if (!currentFolder.equals(folder.getName())) {
//                    System.out.println(numberOfFiles + " files are inside " + currentFolderPath);
                    if (numberOfFiles > 0) {
                        storeLanguageModel(currentFolderPath);
                    }
                    numberOfFiles = 0;
                }
                currentFolder = folder.getName();
                currentFolderPath = "data/language_models/" + path;
                String categoryName = "Top/" + path.replace("\\", "/");
                System.out.println(categoryName);
                if (DMOZdata.get(categoryName) != null) {
                    for (String str : DMOZdata.get(categoryName)) {
                        analyzeDocument(str);
                    }
                }
            } else {
                InputStream stream = zipFile.getInputStream(entry);
                analyzeDocument(LoadFile(stream));
                numberOfFiles++;
            }
        }
        if (numberOfFiles > 0) {
            storeLanguageModel(currentFolderPath);
        }
        System.out.println(filename + " completed...");
    }

    /**
     * Read file content from input stream.
     *
     * @param is
     * @throws Throwable
     */
    private String LoadFile(InputStream is) throws Throwable {
        String str;
        StringBuilder buf = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        while ((str = reader.readLine()) != null) {
            buf.append(str + "\n");
        }
        return buf.toString();
    }

    /**
     * Reads data from a folder, folder may contain zip files.
     *
     * @param folder path of a directory
     * @throws Throwable
     */
    private void LoadDirectory(String folder) throws Throwable {
        int numberOfDocumentsLoaded = 0;
        File dir = new File(folder);
        for (File f : dir.listFiles()) {
            if (f.isFile()) {
                if (f.getName().endsWith(".rar") || f.getName().endsWith(".zip")) {
                    reset();
                    LoadZipFile(f.getAbsolutePath(), f.getName());
                } else {
                    analyzeDocument(LoadFile(new FileInputStream(f)));
                }
                numberOfDocumentsLoaded++;
            } else if (f.isDirectory()) {
                LoadDirectory(f.getAbsolutePath());
            }
        }
        System.out.println("Loaded " + numberOfDocumentsLoaded + " documents from " + folder);
    }

    /**
     * Store the language models in file.
     *
     * @param filename
     * @throws Throwable
     */
    private void storeLanguageModel(String filename) throws Throwable {
        FileWriter fw = null;
        if (NumberOfUnigrams > 0) {
            fw = new FileWriter(filename + "/unigram_LM.txt");
            fw.write(NumberOfUnigrams + "\n");
            for (Map.Entry<String, Integer> entry : DictionaryOfUnigrams.entrySet()) {
                fw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
            }
            fw.close();
        }
        if (NumberOfBigrams > 0) {
            fw = new FileWriter(filename + "/bigram_LM.txt");
            fw.write(NumberOfBigrams + "\n");
            for (Map.Entry<String, Integer> entry : DictionaryOfBigrams.entrySet()) {
                fw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
            }
            fw.close();
        }
        if (NumberOfTrigrams > 0) {
            fw = new FileWriter(filename + "/trigram_LM.txt");
            fw.write(NumberOfTrigrams + "\n");
            for (Map.Entry<String, Integer> entry : DictionaryOfTrigrams.entrySet()) {
                fw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
            }
            fw.close();
        }
        if (NumberOfFourGrams > 0) {
            fw = new FileWriter(filename + "/fourgram_LM.txt");
            fw.write(NumberOfFourGrams + "\n");
            for (Map.Entry<String, Integer> entry : DictionaryOfFourGrams.entrySet()) {
                fw.write(entry.getKey() + "\t" + entry.getValue() + "\n");
            }
            fw.close();
        }
        reset();
    }

    /**
     * Read DMOZ data from xml file to build the language models.
     *
     * @param filename
     */
    private void ReadDMOZData(String filename) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            DMOZDataReader handler = new DMOZDataReader();
            File inputFile = new File(filename);
            saxParser.parse(inputFile, handler);
            DMOZdata = handler.getCategoryToContent();
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(LanguageModelGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Main method that begins the execution of language model generation.
     *
     * @param args command line arguments
     * @throws Throwable
     */
    public static void main(String[] args) throws Throwable {
        // TODO code application logic here
        LanguageModelGenerator tmodel = new LanguageModelGenerator();
        tmodel.ReadDMOZData("data/DMOZ-Crawled-Data-Level-4.xml");
        tmodel.LoadDirectory("data/dmoz_data/Top/");
    }
}
