/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.virginia.cs.eval;

import edu.virginia.cs.model.LanguageModel;
import edu.virginia.cs.model.LoadLanguageModel;
import edu.virginia.cs.utility.FileOperations;
import edu.virginia.cs.utility.Settings;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Wasi
 */
public class MultiThread extends Thread {

    private Settings settings;
    private HashMap<String, Float> referenceModel;

    public static void main(String[] args) throws Exception {
        MultiThread ml = new MultiThread();
        ml.loadParameters();
        ml.doInitialization();
        ml.createThreads();
    }

    /**
     * Load all parameters.
     */
    private void loadParameters() {
        try {
            settings = new Settings();
            BufferedReader br = new BufferedReader(new FileReader(new File("settings.txt")));
            int numQ = Integer.parseInt(br.readLine().replace("number of cover queries =", "").trim());
            settings.setNumberOfCoverQuery(numQ);
            String cRanking = br.readLine().replace("client side re-ranking =", "").trim();
            settings.setClientSideRanking(cRanking.equals("on"));
            String indexPath = br.readLine().replace("lucene AOL index directory =", "").trim();
            settings.setLuceneIndexPath(indexPath);
            String searchLogPath = br.readLine().replace("users search log directory =", "").trim();
            settings.setUserSearchLogPath(searchLogPath);
            String languageModelPath = br.readLine().replace("language models directory =", "").trim();
            settings.setLanguageModelPath(languageModelPath);
            String referenceModelPath = br.readLine().replace("reference model file =", "").trim();
            settings.setReferenceModelPath(referenceModelPath);
            String AOLDictPath = br.readLine().replace("AOL dictionary file =", "").trim();
            settings.setAOLDictionaryPath(AOLDictPath);
            String termIndexPath = br.readLine().replace("term index file =", "").trim();
            settings.setTermIndexPath(termIndexPath);
            int numberOfThreads = Integer.parseInt(br.readLine().replace("number of threads =", "").trim());
            settings.setNumberOfThreads(numberOfThreads);
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(MultiThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Do initialization.
     */
    private void doInitialization() {
        File file = new File("model-output-files/");
        if (!file.exists()) {
            file.mkdir();
        }
    }

    /**
     * The main method that creates and starts threads.
     *
     * @param count number of threads need to be created and started.
     * @return
     */
    private void createThreads() throws InterruptedException {
        try {
            MyThread[] myT = new MyThread[settings.getNumberOfThreads()];
            SemanticEvaluation semEval = new SemanticEvaluation(settings.getTermIndexPath());
            ArrayList<String> allUserId = getAllUserId(settings.getUserSearchLogPath(), -1);
            loadRefModel(settings.getReferenceModelPath());
            LoadLanguageModel llm = new LoadLanguageModel();
            llm.loadModels(3);
            ArrayList<LanguageModel> langModels = llm.getLanguageModels();
            int limit = allUserId.size() / settings.getNumberOfThreads();
            for (int i = 0; i < settings.getNumberOfThreads(); i++) {
                int start = i * limit;
                ArrayList<String> list;
                if (i == settings.getNumberOfThreads() - 1) {
                    list = new ArrayList<>(allUserId.subList(start, allUserId.size()));
                } else {
                    list = new ArrayList<>(allUserId.subList(start, start + limit));
                }
                myT[i] = new MyThread(list, "thread_" + i, settings, referenceModel, langModels, semEval);
                myT[i].start();
            }
            for (int i = 0; i < settings.getNumberOfThreads(); i++) {
                myT[i].getThread().join();
            }
            /* When all threads finished its execution, generate final result */
            double totalKLDivergence = 0.0;
            double totalMI = 0.0;
            double totalMAP = 0.0;
            int totalUsers = 0;
            double totalQueries = 0;
            for (int i = 0; i < settings.getNumberOfThreads(); i++) {
                String[] result = myT[i].getResult().split("\t");
                totalUsers += Integer.parseInt(result[0]);
                totalQueries += Double.parseDouble(result[1]);
                totalMAP += Double.valueOf(result[2]);
                totalKLDivergence += Double.valueOf(result[3]);
                totalMI += Double.valueOf(result[4]);
            }
            double finalKL = totalKLDivergence / totalUsers;
            double finalMI = totalMI / totalUsers;
            double finalMAP = totalMAP / totalQueries;
            FileWriter fw = new FileWriter("model-output-files/final_output.txt");
            fw.write("**************Parameter Settings**************\n");
            fw.write("Number of cover queries = " + settings.getNumberOfCoverQuery() + "\n");
            fw.write("**********************************************\n");
            fw.write("Total Number of users = " + totalUsers + "\n");
            fw.write("Total Number of queries tested = " + totalQueries + "\n");
            fw.write("Averge MAP = " + finalMAP + "\n");
            fw.write("Average KL-Divergence = " + finalKL + "\n");
            fw.write("Average Mutual Information = " + finalMI + "\n");
            fw.close();
        } catch (Throwable ex) {
            Logger.getLogger(MultiThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Method that generate the id of all users for evaluation.
     *
     * @param folder folder path where all user search log resides
     * @return list of all user id
     * @throws java.lang.Throwable
     */
    private ArrayList<String> getAllUserId(String folder, int count) throws Throwable {
        ArrayList<String> allUserIds = new ArrayList<>();
        File dir = new File(folder);
        int userCount = 0;
        for (File f : dir.listFiles()) {
            if (f.isFile()) {
                String fileName = f.getName();
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
                allUserIds.add(fileName);
                userCount++;
            }
            if (userCount == count) {
                break;
            }
        }
        return allUserIds;
    }

    /**
     * Method to load the reference model which is generated previously.
     *
     * @throws java.lang.Throwable
     */
    private void loadRefModel(String filename) throws Throwable {
        referenceModel = new HashMap<>();
        FileOperations fiop = new FileOperations();
        ArrayList<String> lines = fiop.LoadFile(filename, -1);
        for (String line : lines) {
            line = line.trim();
            String[] words = line.split("\t");
            if (words.length == 2) {
                referenceModel.put(words[0], Float.valueOf(words[1]));
            } else {
                System.err.println("Error in " + filename + " format!");
            }
        }
    }

}

class MyThread implements Runnable {

    private Thread t = null;
    private final ArrayList<String> userIds;
    private final HashMap<String, Float> referenceModel;
    private final String threadId;
    private final Settings settings;
    private final SemanticEvaluation semEval;
    private final ArrayList<LanguageModel> langModels;
    private String result;

    public MyThread(ArrayList<String> listUsers, String id, Settings param, HashMap<String, Float> refModel, ArrayList<LanguageModel> models, SemanticEvaluation sem) {
        userIds = listUsers;
        threadId = id;
        settings = param;
        referenceModel = refModel;
        langModels = models;
        semEval = sem;
    }

    /**
     * Overriding the run method of the Thread class.
     */
    @Override
    public void run() {
        try {
            Evaluate evaluate = new Evaluate(settings, langModels);
            evaluate.startEval(userIds, referenceModel, threadId, semEval);
        } catch (Throwable ex) {
            Logger.getLogger(MyThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Method to start the thread.
     */
    public void start() {
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }

    public String getResult() {
        return result;
    }

    /**
     * Method to return the thread object.
     *
     * @return thread object
     */
    public Thread getThread() {
        return t;
    }
}
