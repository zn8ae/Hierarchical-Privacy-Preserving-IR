/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.virginia.cs.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.virginia.cs.utility.SpecialAnalyzer;
import edu.virginia.cs.utility.StringTokenizer;

/**
 *
 * @author Wasi
 */
public class GenerateCoverQuery {

    private final ArrayList<LanguageModel> languageModels;

    public GenerateCoverQuery(ArrayList<LanguageModel> list) {
        languageModels = list;
    }

    /**
     * Generate random number using poisson distribution.
     *
     * @param lambda average query length
     * @return
     */
    private int getPoisson(double lambda) {
        int n = 1;
        double prob = 1.0;
        Random r = new Random();

        while (true) {
            prob *= r.nextDouble();
            if (prob < Math.exp(-lambda)) {
                break;
            }
            n += 1;
        }
        return n - 1;
    }

    /**
     * Generate a random number within a range.
     *
     * @param start
     * @param end
     * @return
     */
    private int getRandom(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    /**
     * Computes and return a cover query topic.
     *
     * @param level
     * @return the cover query topic
     */
    private LanguageModel getCoverQueryTopic(int level) {
        ArrayList<LanguageModel> listModels = new ArrayList<>();
        for (LanguageModel lm : languageModels) {
            if (lm.getLevel() == level) {
                listModels.add(lm);
            }
        }
        int topic = getRandom(0, listModels.size());
//        System.out.println("cover query topic = " + listModels.get(topic).getTopic_name());
//        System.out.println("cover query topic parent = " + listModels.get(topic).getParent().getTopic_name());
        return listModels.get(topic);
    }

    /**
     * Generate cover query of length 1 using unigram language model.
     * 
     * @param level
     * @param bucketNum
     * @return the cover query
     */
    private String getCQfromUnigramLM(int level, int bucketNum) {
        LanguageModel lm = getCoverQueryTopic(level);
//        System.out.println(lm.toString());
        ArrayList<String> possibleCoverQ = new ArrayList<>();
        double max = lm.getMaxProbUnigram();
        double min = lm.getMinProbUnigram();
        for (Map.Entry<String, Integer> entry : lm.getUnigramLM().entrySet()) {
            if (getBucketNumber(entry.getValue(), max, min) == bucketNum) {
                possibleCoverQ.add(entry.getKey());
            }
        }
        if (possibleCoverQ.size() > 0) {
            int coverQNum = getRandom(0, possibleCoverQ.size());
            return possibleCoverQ.get(coverQNum);
        } else {
            return null;
        }
    }

    /**
     * Generate cover query of length 2 using bigram language model.
     *
     * @param level
     * @param bucketNum
     * @return the cover query
     */
    private String getCQfromBigramLM(int level, int bucketNum) {
        LanguageModel lm = getCoverQueryTopic(level);
//        System.out.println(lm.toString());
        ArrayList<String> possibleCoverQ = new ArrayList<>();
        double max = lm.getMaxProbUnigram();
        double min = lm.getMinProbUnigram();
        for (Map.Entry<String, Integer> entry : lm.getBigramLM().entrySet()) {
            if (getBucketNumber(entry.getValue(), max, min) == bucketNum) {
                possibleCoverQ.add(entry.getKey());
            }
        }
        if (possibleCoverQ.size() > 0) {
            int coverQNum = getRandom(0, possibleCoverQ.size());
            return possibleCoverQ.get(coverQNum);
        } else {
            return null;
        }
    }

    /**
     * Generate cover query of length 3 using trigram language model.
     *
     * @param level
     * @param bucketNum
     * @return the cover query
     */
    private String getCQfromTrigramLM(int level, int bucketNum) {
        LanguageModel lm = getCoverQueryTopic(level);
//        System.out.println(lm.toString());
        ArrayList<String> possibleCoverQ = new ArrayList<>();
        double max = lm.getMaxProbUnigram();
        double min = lm.getMinProbUnigram();
        for (Map.Entry<String, Integer> entry : lm.getTrigramLM().entrySet()) {
            if (getBucketNumber(entry.getValue(), max, min) == bucketNum) {
                possibleCoverQ.add(entry.getKey());
            }
        }
        if (possibleCoverQ.size() > 0) {
            int coverQNum = getRandom(0, possibleCoverQ.size());
            return possibleCoverQ.get(coverQNum);
        } else {
            return null;
        }
    }

    /**
     * Generate cover query of length 4 using fourgram language model.
     *
     * @param level
     * @param bucketNum
     * @return the cover query
     */
    private String getCQfromFourgramLM(int level, int bucketNum) {
        LanguageModel lm = getCoverQueryTopic(level);
//        System.out.println(lm.toString());
        ArrayList<String> possibleCoverQ = new ArrayList<>();
        double max = lm.getMaxProbUnigram();
        double min = lm.getMinProbUnigram();
        for (Map.Entry<String, Integer> entry : lm.getFourgramLM().entrySet()) {
            if (getBucketNumber(entry.getValue(), max, min) == bucketNum) {
                possibleCoverQ.add(entry.getKey());
            }
        }
        if (possibleCoverQ.size() > 0) {
            int coverQNum = getRandom(0, possibleCoverQ.size());
            return possibleCoverQ.get(coverQNum);
        } else {
            return null;
        }
    }

    /**
     * Generate cover query of length greater than 4 using a special procedure.
     *
     * @param level
     * @param bucketNum
     * @return the cover query
     */
    private String getCQfromNgramLM(int level, int bucketNum) {
        LanguageModel lm = getCoverQueryTopic(level);
//        System.out.println(lm.toString());
        ArrayList<String> possibleCoverQ = new ArrayList<>();
        double max = lm.getMaxProbUnigram();
        double min = lm.getMinProbUnigram();
        for (Map.Entry<String, Integer> entry : lm.getFourgramLM().entrySet()) {
            if (getBucketNumber(entry.getValue(), max, min) == bucketNum) {
                possibleCoverQ.add(entry.getKey());
            }
        }
        if (possibleCoverQ.size() > 0) {
            int coverQNum = getRandom(0, possibleCoverQ.size());
            return possibleCoverQ.get(coverQNum);
        } else {
            return null;
        }
    }

    /**
     * Creates a cover query based on the original user query length. Unigram,
     * bigram, trigram and fourgram language models are used to generate cover
     * queries of length 1, 2, 3 and 4 respectively. Cover queries of length
     * greater than 4 are created by a special procedure.
     *
     * @param queryLength
     * @param bucketNum
     * @param level
     */
    private String generateCoverQuery(int queryLength, int bucketNum, int level) {
        int coverQuLen = getPoisson(queryLength);
//        System.err.println(coverQuLen);
        if (coverQuLen == 0) {
            return null;
        }
        String coverQ;
        switch (coverQuLen) {
            case 1:
                coverQ = getCQfromUnigramLM(level, bucketNum);
                break;
            case 2:
                coverQ = getCQfromBigramLM(level, bucketNum);
                break;
            case 3:
                coverQ = getCQfromTrigramLM(level, bucketNum);
                break;
            case 4:
                coverQ = getCQfromFourgramLM(level, bucketNum);
                break;
            default:
                coverQ = getCQfromNgramLM(level, bucketNum);
                break;
        }
        return coverQ;
    }

    /**
     *
     * @param probability
     * @param max
     * @param min
     * @return
     */
    private int getBucketNumber(double probability, double max, double min) {
        double difference = (max - min) / 10.0;
        if (probability >= (max - difference)) {
            return 1;
        } else if (probability >= (max - 2 * difference)) {
            return 2;
        } else if (probability >= (max - 3 * difference)) {
            return 3;
        } else if (probability >= (max - 4 * difference)) {
            return 4;
        } else if (probability >= (max - 5 * difference)) {
            return 5;
        } else if (probability >= (max - 6 * difference)) {
            return 6;
        } else if (probability >= (max - 7 * difference)) {
            return 7;
        } else if (probability >= (max - 8 * difference)) {
            return 8;
        } else if (probability >= (max - 9 * difference)) {
            return 9;
        } else {
            return 10;
        }
    }

    /**
     *
     * @param tokens
     * @param n
     * @return
     */
    private int getBestTopic(List<String> tokens, int n) {
        /* probability of query according to best unigram language model */
        double MaxScore = 0.0;
        /* topic index of the best unigram language model */
        int topicNo = -1;
        int index = 0;
        for (LanguageModel lm : languageModels) {
            if (lm.isEmpty()) {
                continue;
            }
            double score = 0.0;
            for (String token : tokens) {
                Double prob = null;
                if (n == 1) {
                    prob = lm.getProbabilityUnigram(token);
                } else if (n == 2) {
                    prob = lm.getProbabilityBigram(token, true);
                } else if (n == 3) {
                    prob = lm.getProbabilityTrigram(token, true);
                } else if (n == 4) {
                    prob = lm.getProbabilityFourgram(token, true);
                }
                if (prob != null) {
                    score += prob;
                }
            }
            if (score > MaxScore) {
                MaxScore = score;
                topicNo = index;
            }
            index++;
        }
        return topicNo;
    }

    /**
     * Generate a list of integers where the first value is the query length,
     * second value is the inferred topic number and rest of the values
     * represent bucket numbers to which the query tokens belong.
     *
     * @param query true user query
     * @return list of integers
     */
    private ArrayList<Integer> getScore(String query) {
        ArrayList<Integer> scores = new ArrayList<>();
        List<String> tokens = StringTokenizer.TokenizeString(query);
        int topicNo = -1;
        if (tokens.size() == 1 || tokens.size() > 4) {
            topicNo = getBestTopic(tokens, 1);
        } else if (tokens.size() == 2) {
            List<String> tempTokens = new ArrayList<>();
            tempTokens.add(tokens.get(0) + " " + tokens.get(1));
            topicNo = getBestTopic(tempTokens, 2);
        } else if (tokens.size() == 3) {
            List<String> tempTokens = new ArrayList<>();
            tempTokens.add(tokens.get(0) + " " + tokens.get(1) + " " + tokens.get(2));
            topicNo = getBestTopic(tempTokens, 3);
        } else if (tokens.size() == 4) {
            List<String> tempTokens = new ArrayList<>();
            tempTokens.add(tokens.get(0) + " " + tokens.get(1) + " " + tokens.get(2) + " " + tokens.get(3));
            topicNo = getBestTopic(tempTokens, 4);
        }
        scores.add(tokens.size());
        scores.add(topicNo);
//        System.out.println(topicNo + " " + MaxScore);
//        System.out.println("Inferred topic: " + languageModels.get(topicNo).getTopic_name());
//        System.out.println("Parent topic: " + languageModels.get(topicNo).getParent().getTopic_name());
//        System.out.println("Grand Parent topic: " + languageModels.get(topicNo).getParent().getParent().getTopic_name());

        LanguageModel selectedModel = languageModels.get(topicNo);
        double max = selectedModel.getMaxProbUnigram();
        double min = selectedModel.getMinProbUnigram();
        for (String token : tokens) {
            Double prob = selectedModel.getProbabilityUnigram(token);
            scores.add(getBucketNumber(prob, max, min));
        }
        return scores;
    }

    /**
     * Creates N cover queries based on true user query.
     *
     * @param query true user query
     * @param N number of cover queries required
     * @return list of cover queries
     */
    public ArrayList<String> generateNQueries(String query, int N) {
        ArrayList<String> retValue = new ArrayList<>();
        ArrayList<Integer> scores = getScore(query);
        if (scores.get(1) == -1) {
            /* if topic of the query can not be inferred */
            return null;
        }
//        System.out.println(scores.toString());
        int queryTopicLevel = languageModels.get(scores.get(1)).getLevel();
        Integer bucketNum = mostCommon(new ArrayList<Integer>(scores.subList(2, scores.size())));
        int count = 0;
        while (true) {
            String cQuery = generateCoverQuery(scores.get(0), bucketNum, queryTopicLevel);
            if (cQuery != null) {
                retValue.add(cQuery);
                count++;
            }
            if (count == N) {
                break;
            }
        }
        return retValue;
    }

    /**
     * Returns the most common value of the list.
     *
     * @param <T>
     * @param list
     * @return
     */
    private <T> T mostCommon(List<T> list) {
        Map<T, Integer> map = new HashMap<>();
        for (T t : list) {
            Integer val = map.get(t);
            map.put(t, val == null ? 1 : val + 1);
        }
        Entry<T, Integer> max = null;
        for (Entry<T, Integer> e : map.entrySet()) {
            if (max == null || e.getValue() > max.getValue()) {
                max = e;
            }
        }
        return max.getKey();
    }

    /**
     * This method is for testing purpose.
     *
     * @param filename
     */
    public void test(String filename) {
        BufferedReader br;
        FileWriter fw;
        try {
            br = new BufferedReader(new FileReader(new File(filename)));
            fw = new FileWriter("./data/random_query_with_cover_query.txt");
            String line;
            while ((line = br.readLine()) != null) {
                ArrayList<String> coverQueries = generateNQueries(line, 1);
                if (coverQueries == null) {
                    System.out.println("Topic can not be inferred for query = " + line);
                } else {
                    fw.write(line + "\n");
                    for (String cq : coverQueries) {
                        fw.write(cq + "\n");
                        fw.flush();
                    }
                    fw.write("\n");
                    fw.flush();
                }
            }
            br.close();
            fw.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GenerateCoverQuery.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GenerateCoverQuery.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) throws Throwable {
        LoadLanguageModel llm = new LoadLanguageModel();
        llm.loadModels(3);
        ArrayList<LanguageModel> list = llm.getLanguageModels();
        GenerateCoverQuery GQ = new GenerateCoverQuery(list);
        GQ.test("./data/random_1000_query.txt");
    }
}
