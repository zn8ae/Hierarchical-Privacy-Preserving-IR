/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.virginia.cs.eval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Wasi
 */
public class PlausibilityEvaluation {
    
    private final HashSet<String> randomQueries;
    
    public PlausibilityEvaluation() {
        randomQueries = new HashSet<>();
    }
    
    public static void main(String[] args) {
        PlausibilityEvaluation rql = new PlausibilityEvaluation();
        rql.loadSearchLogs("./data/updated_search_log(top 1000)", 1000);
    }
    
    /**
     * 
     * @param filename 
     */
    private void loadFile(String filename) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
            String line;
            boolean flag = false;
            int count = 0;
            while ((line = br.readLine()) != null) {
                if (!flag) {
                    String[] split = line.split("\t");
                    randomQueries.add(split[0]);
                    flag = true;
                    count++;
                }
                if (line.isEmpty()) {
                    flag = false;
                }
                if (count == 5) {
                    break;
                }
            }
            br.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PlausibilityEvaluation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PlausibilityEvaluation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * 
     * @param folder
     * @param n 
     */
    private void loadSearchLogs(String folder, int n) {
        File dir = new File(folder);
        for (File f : dir.listFiles()) {
            if (f.isFile()) {
                loadFile(f.getAbsolutePath());
            } else if (f.isDirectory()) {
                loadSearchLogs(f.getAbsolutePath(), n);
            }
            if (randomQueries.size() >= 1000) {
                break;
            }
        }
        storeQueryInFile("./data/random_" + n + "_query.txt", n);
    }
    
    /**
     * 
     * @param filename
     * @param n 
     */
    private void storeQueryInFile(String filename, int n) {
        FileWriter fw;
        try {
            fw = new FileWriter(filename);
            int count = 0;
            for (String str : randomQueries) {
                fw.write(str + "\n");
                count++;
                if (count == n) {
                    break;
                }
            }
            fw.close();
        } catch (IOException ex) {
            Logger.getLogger(PlausibilityEvaluation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
