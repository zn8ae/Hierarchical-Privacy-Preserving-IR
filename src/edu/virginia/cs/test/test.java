/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.virginia.cs.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author Wasi
 */
public class test {

    private int totalQuery;

    public test() {
        totalQuery = 0;
    }

    public static void main(String[] args) throws Throwable {
        test t = new test();
        t.LoadDirectory("./data/user_search_logs");
    }

    /**
     * Read file content from input stream.
     *
     * @param is
     * @throws Throwable
     */
    private void LoadFile(InputStream is) throws Throwable {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        boolean isQuery = false;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                isQuery = false;
                continue;
            }
            if (!isQuery) {
                isQuery = true;
                totalQuery++;
            }
        }
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
                LoadFile(new FileInputStream(f));
                numberOfDocumentsLoaded++;
            } else if (f.isDirectory()) {
                LoadDirectory(f.getAbsolutePath());
            }
        }
        System.out.println("Loaded " + numberOfDocumentsLoaded + " documents from " + folder);
        System.out.println("Total User Query = " + totalQuery);
    }
}
