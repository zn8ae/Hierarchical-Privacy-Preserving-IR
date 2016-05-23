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
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.virginia.cs.utility.StringTokenizer;

/**
 *
 * @author Wasi
 */
public class GenerateCoverQuery {

    private final ArrayList<LanguageModel> languageModels;
    private ArrayList<Integer> coverQueryTopics;
    private int currentQueryTopicNo;
    private final StringTokenizer tokenizer;

    public GenerateCoverQuery(ArrayList<LanguageModel> list) {
        languageModels = list;
        /* No stopword removal and no stemming during inference and generation step */
        tokenizer = new StringTokenizer(false, true);
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
     * @param queryTopic
     * @param fromSibling
     * @return the cover query topic
     */
    private LanguageModel getCoverQueryTopic(int level, int queryTopic, boolean fromSibling) {
        ArrayList<LanguageModel> listModels = new ArrayList<>();
        if (fromSibling) {
            /**
             * Selecting a cover query topic only from sibling topics.
             */
            int topicId = languageModels.get(queryTopic).getTopic_id();
            for (LanguageModel lm : languageModels.get(queryTopic).getParent().getListOfChildren()) {
                if (lm.getTopic_id() != topicId) {
                    listModels.add(lm);
                }
            }
        } else {
            /**
             * Selecting a cover query topic from same level but no from sibling
             * topics.
             */
            int parentTopicId = languageModels.get(queryTopic).getParent().getTopic_id();
            for (LanguageModel lm : languageModels) {
                if (lm.getLevel() == level && lm.getParent().getTopic_id() != parentTopicId) {
                    listModels.add(lm);
                }
            }
        }
        int topic = getRandom(0, listModels.size());
        return listModels.get(topic);
    }

    /**
     * Generate cover query of length 1 using unigram language model.
     *
     * @param level
     * @param bucketNum
     * @param trueQuTopic
     * @param fromSibling
     * @param coverQuTopic
     * @return the cover query
     */
    private String getCQfromUnigramLM(int level, int bucketNum, int trueQuTopic, boolean fromSibling, int coverQuTopic) {
        LanguageModel lm;
        if (coverQuTopic == -1) {
            lm = getCoverQueryTopic(level, trueQuTopic, fromSibling);
        } else {
            lm = languageModels.get(coverQuTopic);
        }
        ArrayList<String> possibleCoverQ = new ArrayList<>();
        double max = lm.getMaxProbUnigram();
        double min = lm.getMinProbUnigram();
        for (Map.Entry<String, Integer> entry : lm.getUnigramLM().entrySet()) {
            if (getBucketNumber(entry.getValue(), max, min) == bucketNum) {
                possibleCoverQ.add(entry.getKey());
            }
        }
        if (possibleCoverQ.size() > 0) {
            StringTokenizer st = new StringTokenizer(true, true);
            for (int i = 0; i < possibleCoverQ.size(); i++) {
                int coverQNum = getRandom(0, possibleCoverQ.size());
                String cQuery = possibleCoverQ.get(coverQNum);
                if (st.TokenizeString(cQuery).size() == 1) {
                    coverQueryTopics.add(lm.getTopic_id());
                    return cQuery;
                }
            }
            return null;
        } else {
            return null;
        }
    }

    /**
     * Returns a unigram from a given language model and bucket number.
     *
     * @param lm
     * @param bucketNum
     * @return
     */
    private String getUniGramFromLM(LanguageModel lm, int bucketNum) {
        ArrayList<String> possibleCoverQ = new ArrayList<>();
        double max = lm.getMaxProbUnigram();
        double min = lm.getMinProbUnigram();
        for (Map.Entry<String, Integer> entry : lm.getUnigramLM().entrySet()) {
            if (getBucketNumber(entry.getValue(), max, min) == bucketNum) {
                possibleCoverQ.add(entry.getKey());
            }
        }
        if (possibleCoverQ.size() > 0) {
            StringTokenizer st = new StringTokenizer(true, true);
            for (int i = 0; i < possibleCoverQ.size(); i++) {
                int coverQNum = getRandom(0, possibleCoverQ.size());
                String cQuery = possibleCoverQ.get(coverQNum);
                if (st.TokenizeString(cQuery).size() == 1) {
                    coverQueryTopics.add(lm.getTopic_id());
                    return cQuery;
                }
            }
            return null;
        } else {
            return null;
        }
    }

    /**
     * Generate cover query of length 2 using bigram language model.
     *
     * @param level
     * @param bucketNum
     * @param trueQuTopic
     * @param fromSibling
     * @param coverQuTopic
     * @return the cover query
     */
    private String getCQfromBigramLM(int level, int bucketNum, int trueQuTopic, boolean fromSibling, int coverQuTopic) {
        LanguageModel lm;
        if (coverQuTopic == -1) {
            lm = getCoverQueryTopic(level, trueQuTopic, fromSibling);
        } else {
            lm = languageModels.get(coverQuTopic);
        }
        ArrayList<String> possibleCoverQ = new ArrayList<>();
        double max = lm.getMaxProbBigram();
        double min = lm.getMinProbBigram();
        for (Map.Entry<String, Integer> entry : lm.getBigramLM().entrySet()) {
            if (getBucketNumber(entry.getValue(), max, min) == bucketNum) {
                possibleCoverQ.add(entry.getKey());
            }
        }
        if (possibleCoverQ.size() > 0) {
            StringTokenizer st = new StringTokenizer(true, true);
            for (int i = 0; i < possibleCoverQ.size(); i++) {
                int coverQNum = getRandom(0, possibleCoverQ.size());
                String cQuery = possibleCoverQ.get(coverQNum);
                if (st.TokenizeString(cQuery).size() == 2) {
                    coverQueryTopics.add(lm.getTopic_id());
                    return cQuery;
                }
            }
            return null;
        } else {
            return null;
        }
    }

    /**
     * Returns a bigram from a given language model and bucket number. Returns a
     * bigram which contains the trigram if provided.
     *
     * @param lm
     * @param bucketNum
     * @param unigram
     * @return
     */
    private String getCQfromBigramLM(LanguageModel lm, int bucketNum, String unigram) {
        ArrayList<String> possibleCoverQ = new ArrayList<>();
        double max = lm.getMaxProbBigram();
        double min = lm.getMinProbBigram();
        if (unigram == null) {
            for (Map.Entry<String, Integer> entry : lm.getBigramLM().entrySet()) {
                if (getBucketNumber(entry.getValue(), max, min) == bucketNum) {
                    possibleCoverQ.add(entry.getKey());
                }
            }
        } else {
            for (Map.Entry<String, Integer> entry : lm.getBigramLM().entrySet()) {
                if (getBucketNumber(entry.getValue(), max, min) == bucketNum) {
                    if (entry.getKey().contains(unigram)) {
                        possibleCoverQ.add(entry.getKey());
                    }
                }
            }
        }
        if (possibleCoverQ.size() > 0) {
            StringTokenizer st = new StringTokenizer(true, true);
            for (int i = 0; i < possibleCoverQ.size(); i++) {
                int coverQNum = getRandom(0, possibleCoverQ.size());
                String cQuery = possibleCoverQ.get(coverQNum);
                if (st.TokenizeString(cQuery).size() == 2) {
                    coverQueryTopics.add(lm.getTopic_id());
                    return cQuery;
                }
            }
            return null;
        } else {
            return null;
        }
    }

    /**
     * Generate cover query of length 3 using trigram language model.
     *
     * @param level
     * @param bucketNum
     * @param trueQuTopic
     * @param fromSibling
     * @param coverQuTopic
     * @return the cover query
     */
    private String getCQfromTrigramLM(int level, int bucketNum, int trueQuTopic, boolean fromSibling, int coverQuTopic) {
        LanguageModel lm;
        if (coverQuTopic == -1) {
            lm = getCoverQueryTopic(level, trueQuTopic, fromSibling);
        } else {
            lm = languageModels.get(coverQuTopic);
        }
        ArrayList<String> possibleCoverQ = new ArrayList<>();
        double max = lm.getMaxProbTrigram();
        double min = lm.getMinProbTrigram();
        for (Map.Entry<String, Integer> entry : lm.getTrigramLM().entrySet()) {
            if (getBucketNumber(entry.getValue(), max, min) == bucketNum) {
                possibleCoverQ.add(entry.getKey());
            }
        }
        if (possibleCoverQ.size() > 0) {
            int coverQNum = getRandom(0, possibleCoverQ.size());
            coverQueryTopics.add(lm.getTopic_id());
            return possibleCoverQ.get(coverQNum);
        } else {
            return null;
        }
    }

    /**
     * Returns a trigram from a given language model and bucket number. Returns
     * a trigram which contains the bigram if provided.
     *
     * @param lm
     * @param bucketNum
     * @param bigram
     * @return
     */
    private String getTriGramFromLM(LanguageModel lm, int bucketNum, String bigram) {
        ArrayList<String> possibleCoverQ = new ArrayList<>();
        double max = lm.getMaxProbTrigram();
        double min = lm.getMinProbTrigram();
        if (bigram == null) {
            for (Map.Entry<String, Integer> entry : lm.getTrigramLM().entrySet()) {
                if (getBucketNumber(entry.getValue(), max, min) == bucketNum) {
                    possibleCoverQ.add(entry.getKey());
                }
            }
        } else {
            for (Map.Entry<String, Integer> entry : lm.getTrigramLM().entrySet()) {
                if (getBucketNumber(entry.getValue(), max, min) == bucketNum) {
                    if (entry.getKey().contains(bigram)) {
                        possibleCoverQ.add(entry.getKey());
                    }
                }
            }
        }
        if (possibleCoverQ.size() > 0) {
            int coverQNum = getRandom(0, possibleCoverQ.size());
            if (bigram == null) {
                coverQueryTopics.add(lm.getTopic_id());
            }
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
     * @param trueQuTopic
     * @param fromSibling
     * @param coverQuTopic
     * @return the cover query
     */
    private String getCQfromFourgramLM(int level, int bucketNum, int trueQuTopic, boolean fromSibling, int coverQuTopic) {
        LanguageModel lm;
        if (coverQuTopic == -1) {
            lm = getCoverQueryTopic(level, trueQuTopic, fromSibling);
        } else {
            lm = languageModels.get(coverQuTopic);
        }
        ArrayList<String> possibleCoverQ = new ArrayList<>();
        double max = lm.getMaxProbFourgram();
        double min = lm.getMinProbFourgram();
        for (Map.Entry<String, Integer> entry : lm.getFourgramLM().entrySet()) {
            if (getBucketNumber(entry.getValue(), max, min) == bucketNum) {
                possibleCoverQ.add(entry.getKey());
            }
        }
        if (possibleCoverQ.size() > 0) {
            int coverQNum = getRandom(0, possibleCoverQ.size());
            coverQueryTopics.add(lm.getTopic_id());
            return possibleCoverQ.get(coverQNum);
        } else {
            return null;
        }
    }

    /**
     * Returns a fourgram from a given language model and bucket number. Returns
     * a fourgram which contains the trigram if provided.
     *
     * @param lm
     * @param bucketNum
     * @param trigram
     * @return
     */
    private String getFourGramFromLM(LanguageModel lm, int bucketNum, String trigram) {
        ArrayList<String> possibleCoverQ = new ArrayList<>();
        double max = lm.getMaxProbFourgram();
        double min = lm.getMinProbFourgram();
        if (trigram == null) {
            for (Map.Entry<String, Integer> entry : lm.getFourgramLM().entrySet()) {
                if (getBucketNumber(entry.getValue(), max, min) == bucketNum) {
                    possibleCoverQ.add(entry.getKey());
                }
            }
        } else {
            for (Map.Entry<String, Integer> entry : lm.getFourgramLM().entrySet()) {
                if (getBucketNumber(entry.getValue(), max, min) == bucketNum) {
                    if (entry.getKey().contains(trigram)) {
                        possibleCoverQ.add(entry.getKey());
                    }
                }
            }
        }
        if (possibleCoverQ.size() > 0) {
            int coverQNum = getRandom(0, possibleCoverQ.size());
            if (trigram == null) {
                coverQueryTopics.add(lm.getTopic_id());
            }
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
     * @param trueQuTopic
     * @param fromSibling
     * @param coverQuTopic
     * @return the cover query
     */
    private String getCQfromNgramLM(int length, int level, int bucketNum, int trueQuTopic, boolean fromSibling, int coverQuTopic) {
        LanguageModel lm;
        if (coverQuTopic == -1) {
            lm = getCoverQueryTopic(level, trueQuTopic, fromSibling);
        } else {
            lm = languageModels.get(coverQuTopic);
        }
        ArrayList<String> cQuery = new ArrayList<>();
        for (int k = 0; k < length; k++) {
            String tempFourgram;
            if (cQuery.isEmpty()) {
                tempFourgram = getFourGramFromLM(lm, bucketNum, null);
            } else if (cQuery.size() >= 3) {
                String trigram = "";
                for (int x = cQuery.size() - 3; x < cQuery.size(); x++) {
                    trigram += cQuery.get(x) + " ";
                }
                trigram = trigram.trim();
                tempFourgram = getFourGramFromLM(lm, bucketNum, trigram);
            } else {
                tempFourgram = null;
            }
            if (tempFourgram == null) {
                String tempTrigram;
                if (cQuery.isEmpty()) {
                    tempTrigram = getTriGramFromLM(lm, bucketNum, null);
                } else if (cQuery.size() >= 2) {
                    int l = cQuery.size() - 1;
                    String bigram = cQuery.get(l - 1) + " " + cQuery.get(l);
                    tempTrigram = getTriGramFromLM(lm, bucketNum, bigram);
                } else {
                    tempTrigram = null;
                }
                if (tempTrigram == null) {
                    String tempBigram;
                    if (cQuery.isEmpty()) {
                        tempBigram = getCQfromBigramLM(lm, bucketNum, null);
                    } else if (cQuery.size() >= 1) {
                        String unigram = cQuery.get(cQuery.size() - 1);
                        tempBigram = getCQfromBigramLM(lm, bucketNum, unigram);
                    } else {
                        tempBigram = null;
                    }
                    if (tempBigram == null) {
                        String tempUnigram = getUniGramFromLM(lm, bucketNum);
                        if (tempUnigram != null) {
                            cQuery.add(tempUnigram);
                        }
                    } else {
                        k = k + 2;
                        for (String str : tokenizer.TokenizeString(tempBigram)) {
                            cQuery.add(str);
                        }
                    }
                } else {
                    k = k + 3;
                    for (String str : tokenizer.TokenizeString(tempTrigram)) {
                        cQuery.add(str);
                    }
                }
            } else {
                k = k + 4;
                for (String str : tokenizer.TokenizeString(tempFourgram)) {
                    cQuery.add(str);
                }
            }
        }
        if (cQuery.isEmpty()) {
            return null;
        } else {
            String coverQ = "";
            for (String term : cQuery) {
                coverQ += term + " ";
            }
            coverQ = coverQ.trim();
            return coverQ;
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
     * @param trueQuTopic
     * @param fromSibling
     * @param coverQuTopic
     */
    private String generateCoverQuery(int queryLength, int bucketNum, int level, int trueQuTopic, boolean fromSibling, int coverQuTopic) {
        int coverQuLen = getPoisson(queryLength);
        System.out.println("Co q length = " + coverQuLen);
        if (coverQuLen == 0) {
            return null;
        }
        String coverQ;
        switch (coverQuLen) {
            case 1:
                coverQ = getCQfromUnigramLM(level, bucketNum, trueQuTopic, fromSibling, coverQuTopic);
                break;
            case 2:
                coverQ = getCQfromBigramLM(level, bucketNum, trueQuTopic, fromSibling, coverQuTopic);
                break;
            case 3:
                coverQ = getCQfromTrigramLM(level, bucketNum, trueQuTopic, fromSibling, coverQuTopic);
                break;
            case 4:
                coverQ = getCQfromFourgramLM(level, bucketNum, trueQuTopic, fromSibling, coverQuTopic);
                break;
            default:
                coverQ = getCQfromNgramLM(coverQuLen, level, bucketNum, trueQuTopic, fromSibling, coverQuTopic);
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
     * Returns the best topic that characterize the given query.
     *
     * @param tokens
     * @param n size of the query
     * @return
     */
    private int getBestTopic(String query, int n) {
        /* probability of query according to best unigram language model */
        double maxProb = 0.0;
        /* topic index of the best unigram language model */
        int topicNo = -1;
        int index = 0;
        for (LanguageModel lm : languageModels) {
            if (lm.isEmpty()) {
                continue;
            }
            double prob;
            if (n == 1) {
                prob = lm.getProbabilityUnigram(query);
            } else if (n == 2) {
                prob = lm.getProbabilityBigram(query, true);
            } else if (n == 3) {
                prob = lm.getProbabilityTrigram(query, true);
            } else if (n == 4) {
                prob = lm.getProbabilityFourgram(query, true);
            } else {
                prob = lm.getProbabilityNgram(query, n);
            }
            if (prob > maxProb) {
                maxProb = prob;
                topicNo = index;
            }
            index++;
        }
        return topicNo;
    }

    /**
     * Generate a list of integers where the first value is the query length,
     * second value is the inferred topic number and the last one is the bucket
     * number from to which the query belongs.
     *
     * @param query true user query
     * @return list of integers
     */
    private ArrayList<Integer> getScore(String query) {
        ArrayList<Integer> scores = new ArrayList<>();
        List<String> tokens = tokenizer.TokenizeString(query);
        String modifiedQuery = "";
        for (String token : tokens) {
            modifiedQuery += token + " ";
        }
        modifiedQuery = modifiedQuery.trim();
        int topicNo = getBestTopic(modifiedQuery, tokens.size());
        scores.add(tokens.size());
        scores.add(topicNo);
//        System.out.println(topicNo + " " + MaxScore);
//        System.out.println("Inferred topic: " + languageModels.get(topicNo).getTopic_name());
//        System.out.println("Parent topic: " + languageModels.get(topicNo).getParent().getTopic_name());
//        System.out.println("Grand Parent topic: " + languageModels.get(topicNo).getParent().getParent().getTopic_name());

        LanguageModel selectedModel = languageModels.get(topicNo);
        double max, min;
        int n = tokens.size();
        if (n == 1) {
            max = selectedModel.getMaxProbUnigram();
            min = selectedModel.getMinProbUnigram();
            double prob = selectedModel.getProbabilityUnigram(modifiedQuery);
            scores.add(getBucketNumber(prob, max, min));
        } else if (n == 2) {
            max = selectedModel.getMaxProbBigram();
            min = selectedModel.getMinProbBigram();
            double prob = selectedModel.getProbabilityBigram(modifiedQuery, true);
            scores.add(getBucketNumber(prob, max, min));
        } else if (n == 3) {
            max = selectedModel.getMaxProbTrigram();
            min = selectedModel.getMinProbTrigram();
            double prob = selectedModel.getProbabilityTrigram(modifiedQuery, true);
            scores.add(getBucketNumber(prob, max, min));
        } else if (n == 4) {
            max = selectedModel.getMaxProbFourgram();
            min = selectedModel.getMinProbFourgram();
            double prob = selectedModel.getProbabilityFourgram(modifiedQuery, true);
            scores.add(getBucketNumber(prob, max, min));
        } else {
            max = selectedModel.getMaxProbFourgram();
            min = selectedModel.getMinProbFourgram();
            double prob = selectedModel.getProbabilityNgram(modifiedQuery, n);
            scores.add(getBucketNumber(prob, max, min));
        }
        return scores;
    }

    /**
     * Check if the user query is sequentially edited or not.
     *
     * @param previousQuTopic
     * @param currentQuTopic
     * @return
     */
    private int checkSequentialEdited(int previousQuTopic, int currentQuTopic) {
        int currentTopicId = languageModels.get(currentQuTopic).getParent().getTopic_id();
        int parentId = languageModels.get(previousQuTopic).getParent().getTopic_id();
        /**
         * Check if current query topic is the parent of previous query topic,
         * then sequential editing is true.
         */
        if (parentId == currentTopicId) {
            return 1;
        }
        /**
         * Check if current query topic is the child of previous query topic,
         * then sequential editing is true.
         */
        for (LanguageModel lm : languageModels.get(previousQuTopic).getListOfChildren()) {
            int childTopicId = lm.getTopic_id();
            if (childTopicId == currentTopicId) {
                return -1;
            }
        }
        /**
         * Current query topic is neither parent nor child of the previous query
         * topic, so return 0.
         */
        return 0;
    }

    /**
     * Creates N cover queries based on true user query. Handle sequentially
     * edited queries by using lastQueryTopicNo.
     *
     * @param query true user query
     * @param N number of cover queries required
     * @param previousQueryTopicNo -1 if the previous query and current query
     * belong to the same session.
     * @param coverQTopics
     * @return list of cover queries
     */
    public ArrayList<String> generateNQueries(String query, int N, int previousQueryTopicNo, ArrayList<Integer> coverQTopics) {
        ArrayList<String> coverQueries = new ArrayList<>();
        ArrayList<Integer> scores = getScore(query);
        if (scores.get(1) == -1) {
            /* if topic of the query can not be inferred */
            return null;
        }
        currentQueryTopicNo = scores.get(1);
        int currentQueryTopicLevel = languageModels.get(currentQueryTopicNo).getLevel();
        int seqEdited = 0;
        if (previousQueryTopicNo != -1) {
            seqEdited = checkSequentialEdited(previousQueryTopicNo, currentQueryTopicNo);
        }
        int bucketNum = scores.get(2);
        int count = 0;
        coverQueryTopics = new ArrayList<>();
        while (true) {
            String cQuery;
            if (seqEdited == 0) {
                /* Current query is not sequentially edited. */
                if (count < N / 2) {
                    /* Generating cover query from sibling topics. */
                    cQuery = generateCoverQuery(scores.get(0), bucketNum, currentQueryTopicLevel, currentQueryTopicNo, true, -1);
                } else {
                    /* Generating cover query from same level of original query but not from sibling topics */
                    cQuery = generateCoverQuery(scores.get(0), bucketNum, currentQueryTopicLevel, currentQueryTopicNo, false, -1);
                }
            } else {
                /**
                 * Current query is sequentially edited, so cover queries will
                 * be generated based on previous cover query topics.
                 */
                int coverQuTopic = coverQTopics.get(count);
                if (seqEdited == 1) {
                    /**
                     * Cover query should be generated from parent topic of the
                     * previous cover query.
                     */
                    int topicId = languageModels.get(coverQuTopic).getParent().getTopic_id();
                    cQuery = generateCoverQuery(scores.get(0), bucketNum, currentQueryTopicLevel, currentQueryTopicNo, false, topicId);
                } else {
                    /**
                     * Cover query should be generated from child topic of the
                     * previous cover query.
                     */
                    int rand = getRandom(0, languageModels.get(coverQuTopic).getListOfChildren().size());
                    int topicId = languageModels.get(coverQuTopic).getListOfChildren().get(rand).getTopic_id();
                    cQuery = generateCoverQuery(scores.get(0), bucketNum, currentQueryTopicLevel, currentQueryTopicNo, false, topicId);
                }
            }
            if (cQuery != null) {
                coverQueries.add(cQuery);
                count++;
            }
            if (count == N) {
                break;
            }
        }
        return coverQueries;
    }

    /**
     *
     * @return
     */
    public int getLastQueryTopicNo() {
        return currentQueryTopicNo;
    }

    public ArrayList<Integer> getCoverQueryTopics() {
        return coverQueryTopics;
    }

    /**
     * This method is for testing purpose.
     *
     * @param filename
     */
    private void test(String filename) {
        BufferedReader br;
        FileWriter fw;
        try {
            br = new BufferedReader(new FileReader(new File(filename)));
            fw = new FileWriter("./data/Random-Query-with-Cover-Query.txt");
            String line;
            while ((line = br.readLine()) != null) {
                ArrayList<String> coverQueries = generateNQueries(line, 1, -1, new ArrayList<>());
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
        llm.loadModels(2);
        ArrayList<LanguageModel> list = llm.getLanguageModels();
        GenerateCoverQuery GQ = new GenerateCoverQuery(list);
//        GQ.test("./data/Random-1000-Query.txt");
        ArrayList<String> coverQueries = GQ.generateNQueries("how to take revenge againts old lover", 2, -1, new ArrayList<>());
        System.out.println(coverQueries.toString());
    }
}
