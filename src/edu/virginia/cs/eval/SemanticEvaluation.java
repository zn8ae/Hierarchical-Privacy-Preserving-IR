package edu.virginia.cs.eval;

import java.io.IOException;
import java.util.ArrayList;
import edu.virginia.cs.utility.SpecialAnalyzer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

public class SemanticEvaluation {

    /* data structure for mutual information measurement */
    private HashMap<String, Integer> dictionaryWords;
    int[][] termDocMatrix = new int[23225][2225];
    private QueryParser parser;

    /**
     * Initialize necessary data structures.
     */
    public void initiliaze() {
        SpecialAnalyzer analyzer = new SpecialAnalyzer();
        parser = new QueryParser(Version.LUCENE_46, "", analyzer);
        BufferedReader br;
        try {
            dictionaryWords = new HashMap<>();
            String line;
            br = new BufferedReader(new FileReader("data/mutual_info_data/bbcDictionary.txt"));
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("\t");
                if (tokens.length == 2) {
                    Integer serial = Integer.parseInt(tokens[0]);
                    String key = tokens[1];
                    dictionaryWords.put(key, serial);
                }
            }
            br.close();
        } catch (IOException | NumberFormatException ex) {
            Logger.getLogger(SemanticEvaluation.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            String line;
            br = new BufferedReader(new FileReader("data/mutual_info_data/bbcTermDocMatrix.txt"));
            int countRow = 0;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(" ");
                for (int k = 0; k < tokens.length; k++) {
                    termDocMatrix[countRow][k] = Integer.parseInt(tokens[k]);
                }
                countRow++;
            }
            br.close();
        } catch (IOException | NumberFormatException ex) {
            Logger.getLogger(SemanticEvaluation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Computing probability for a query.
     * 
     * @param query
     * @return
     */
    private double getProbFromTermDocMatrix(String query) {
        double probQuery = 0;
        try {
            if (query.isEmpty()) {
                return 0;
            }
            Query textQuery = parser.parse(QueryParser.escape(query));
            String tokens[] = textQuery.toString().trim().split(" ");
            int docFreqCount = 0;
            for (int docIndex = 0; docIndex < termDocMatrix[0].length; docIndex++) {
                boolean foundFlag = true;
                for (String token : tokens) {
                    Integer termIndex = dictionaryWords.get(token);
                    if (termIndex != null) {
                        boolean checkbound = true;
                        if (termIndex > termDocMatrix.length && docIndex > termDocMatrix[0].length) {
                            checkbound = false;
                        }
                        if (checkbound && termDocMatrix[termIndex][docIndex] < 1) {
                            foundFlag = false;
                            break;
                        }
                    } else {
                        foundFlag = false;
                        break;
                    }
                }
                if (foundFlag) {
                    docFreqCount++;
                }
            }
            probQuery = (docFreqCount * 1.0) / 2225;
        } catch (ParseException ex) {
            Logger.getLogger(SemanticEvaluation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return probQuery;
    }

    /**
     * Computing normalized mutual information between set of true queries and
     * cover queries.
     *
     * @param origQuery
     * @param coverQuery
     * @return
     */
    public double calculateNMI(ArrayList<String> origQuery, ArrayList<String> coverQuery) {
        HashMap<String, Double> Px = new HashMap<>();
        HashMap<String, Double> Py = new HashMap<>();
        HashMap<String, Double> Pxy = new HashMap<>();
        /* computing P(x) */
        for (String qr : origQuery) {
            double prob = getProbFromTermDocMatrix(qr);
            Px.put(qr, prob);
        }
        /* computing P(y) */
        for (String qr : coverQuery) {
            double prob = getProbFromTermDocMatrix(qr);
            Py.put(qr, prob);
        }
        /* computing P(x, y) */
        for (String origQuery1 : origQuery) {
            for (String coverQuery1 : coverQuery) {
                String combineQuery = origQuery1 + " " + coverQuery1;
                double prob = getProbFromTermDocMatrix(combineQuery);
                Pxy.put(combineQuery, prob);
            }
        }
        /* computing mutual information */
        double muInfo = 0;
        for (String origQuery1 : origQuery) {
            for (String coverQuery1 : coverQuery) {
                String combineQuery = origQuery1 + " " + coverQuery1;
                double pxy = Pxy.get(combineQuery);
                double px = Px.get(origQuery1);
                double py = Py.get(coverQuery1);
                if (pxy > 0 && px > 0 && py > 0) {
                    double partER = pxy * ((Math.log10(pxy / (px * py))) / Math.log10(2));
                    muInfo += partER;
                }
            }
        }
        return muInfo;
    }

}
