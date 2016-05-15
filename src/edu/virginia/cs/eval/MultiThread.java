/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.virginia.cs.eval;

import edu.virginia.cs.model.LanguageModel;
import edu.virginia.cs.model.LoadLanguageModel;
import edu.virginia.cs.utility.FileOperations;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

    /* folder path where a specified number of user's search log is present */
    private final String _judgeFile = "./data/user_search_logs/";
    /* Reference model to smooth language model while generating the cover queries */
    private HashMap<String, Float> referenceModel;
    /* First parameter of our approach, Entropy range */
    private double entropyRange;
    /* Second parameter of our approach, number of cover query for each user query */
    private int numOfCoverQ;
    /* Flag to enable client side re-ranking */
    private boolean enableClientSideRanking;
    /* Number of threads to be executed */
    private int numberOfThreads;

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
            BufferedReader br = new BufferedReader(new FileReader(new File("parameters.txt")));
            numOfCoverQ = Integer.parseInt(br.readLine().replace("number of cover queries =", "").trim());
            entropyRange = Double.parseDouble(br.readLine().replace("entropy range =", "").trim());
            String cRanking = br.readLine().replace("client side re-ranking =", "").trim();
            enableClientSideRanking = cRanking.equals("on");
            numberOfThreads = Integer.parseInt(br.readLine().replace("number of threads =", "").trim());
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
        file = new File("lda-output-files");
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
            MyThread[] myT = new MyThread[numberOfThreads];
            ArrayList<String> allUserId = getAllUserId(_judgeFile);
            loadRefModel();
            LoadLanguageModel llm = new LoadLanguageModel();
            llm.loadModels(3);
            ArrayList<LanguageModel> langModels = llm.getLanguageModels();
            int limit = allUserId.size() / numberOfThreads;
            for (int i = 0; i < numberOfThreads; i++) {
                int start = i * limit;
                ArrayList<String> list;
                if (i == numberOfThreads - 1) {
                    list = new ArrayList<>(allUserId.subList(start, allUserId.size()));
                } else {
                    list = new ArrayList<>(allUserId.subList(start, start + limit));
                }
                myT[i] = new MyThread(list, referenceModel, "thread_" + i, entropyRange, numOfCoverQ, enableClientSideRanking, langModels);
                myT[i].start();
            }
            for (int i = 0; i < numberOfThreads; i++) {
                myT[i].getThread().join();
            }
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
    private ArrayList<String> getAllUserId(String folder) throws Throwable {
        ArrayList<String> allUserIds = new ArrayList<>();
        File dir = new File(folder);
        for (File f : dir.listFiles()) {
            if (f.isFile()) {
                String fileName = f.getName();
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
                allUserIds.add(fileName);
            }
        }
        return allUserIds;
    }

    /**
     * Method to load the reference model which is generated previously.
     *
     * @throws java.lang.Throwable
     */
    private void loadRefModel() throws Throwable {
        referenceModel = new HashMap<>();
        FileOperations fiop = new FileOperations();
        ArrayList<String> lines = fiop.LoadFile("./data/reference_model.txt", -1);
        for (String line : lines) {
            line = line.trim();
            String[] words = line.split(" ");
            if (words.length == 2) { // for unigrams in the reference model
                referenceModel.put(words[0], Float.valueOf(words[1]));
            } else if (words.length == 3) { // for bigrams in the reference model
                referenceModel.put(words[0] + " " + words[1], Float.valueOf(words[2]));
            }
        }
    }

}

class MyThread implements Runnable {

    private Thread t = null;
    private final ArrayList<String> userIds;
    private final HashMap<String, Float> referenceModel;
    private final String threadId;
    private final double entropyRange;
    private final int numOfCoverQ;
    private final boolean enableClientSideRanking;
    ArrayList<LanguageModel> langModels;

    public MyThread(ArrayList<String> param, HashMap<String, Float> refModel, String id, double entropy, int totalCoverQ, boolean flag, ArrayList<LanguageModel> models) {
        userIds = param;
        referenceModel = refModel;
        threadId = id;
        entropyRange = entropy;
        numOfCoverQ = totalCoverQ;
        enableClientSideRanking = flag;
        langModels = models;
    }

    /**
     * Overriding the run method of the Thread class.
     */
    @Override
    public void run() {
        try {
            Evaluate evaluate = new Evaluate(entropyRange, numOfCoverQ, enableClientSideRanking, langModels);
            evaluate.startEval(userIds, referenceModel, threadId);
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

    /**
     * Method to return the thread object.
     *
     * @return thread object
     */
    public Thread getThread() {
        return t;
    }
}
