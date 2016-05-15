/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.virginia.cs.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 * @author Wasi
 */
public class LanguageModel {

    private String topic_name;
    private HashMap<String, Integer> unigramLM;
    private HashMap<String, Integer> bigramLM;
    private HashMap<String, Integer> trigramLM;
    private HashMap<String, Integer> fourgramLM;
    private int totalUnigrams;
    private int totalUniqueUnigrams;
    private LanguageModel parent;
    private ArrayList<LanguageModel> listOfChildren;
    private final double lambda;
    private int level;
    private double maxProbUnigram;
    private double minProbUnigram;
    private double maxProbBigram;
    private double minProbBigram;
    private double maxProbTrigram;
    private double minProbTrigram;
    private double maxProbFourgram;
    private double minProbFourgram;

    public LanguageModel() {
        this.unigramLM = new HashMap<>();
        this.bigramLM = new HashMap<>();
        this.trigramLM = new HashMap<>();
        this.fourgramLM = new HashMap<>();
        this.listOfChildren = new ArrayList<>();
        lambda = 0.9;
    }

    /**
     * Returns the topic name of the language model.
     *
     * @return topic name of the language model
     */
    public String getTopic_name() {
        return topic_name;
    }

    /**
     * Set the topic name of the language model.
     *
     * @param topic_name
     */
    public void setTopic_name(String topic_name) {
        this.topic_name = topic_name;
    }

    /**
     * get the unigram language model.
     *
     * @return unigram language model
     */
    public HashMap<String, Integer> getUnigramLM() {
        return unigramLM;
    }

    /**
     * Set the unigram language model.
     *
     * @param unigramLM
     */
    public void setUnigramLM(HashMap<String, Integer> unigramLM) {
        this.unigramLM = unigramLM;
    }

    /**
     * get the bigram language model.
     *
     * @return bigram language model
     */
    public HashMap<String, Integer> getBigramLM() {
        return bigramLM;
    }

    /**
     * Set the bigram language model.
     *
     * @param bigramLM
     */
    public void setBigramLM(HashMap<String, Integer> bigramLM) {
        this.bigramLM = bigramLM;
    }

    /**
     * get the trigram language model.
     *
     * @return trigram language model
     */
    public HashMap<String, Integer> getTrigramLM() {
        return trigramLM;
    }

    /**
     * Set the trigram language model.
     *
     * @param trigramLM
     */
    public void setTrigramLM(HashMap<String, Integer> trigramLM) {
        this.trigramLM = trigramLM;
    }

    /**
     * get the fourgram language model.
     *
     * @return fourgram language model
     */
    public HashMap<String, Integer> getFourgramLM() {
        return fourgramLM;
    }

    /**
     * Set the fourgram language model.
     *
     * @param fourgramLM
     */
    public void setFourgramLM(HashMap<String, Integer> fourgramLM) {
        this.fourgramLM = fourgramLM;
    }

    /**
     * get the parent language models.
     *
     * @return parent language model
     */
    public LanguageModel getParent() {
        return parent;
    }

    /**
     * Returns total number of unigrams in the language model.
     * 
     * @return
     */
    public int getTotalUnigrams() {
        return totalUnigrams;
    }

    /**
     * Sets total number of unigrams of the language model.
     * 
     * @param totalUnigrams
     */
    public void setTotalUnigrams(int totalUnigrams) {
        this.totalUnigrams = totalUnigrams;
    }

    /**
     *
     * @return
     */
    public int getTotalUniqueUnigrams() {
        return totalUniqueUnigrams;
    }

    /**
     *
     * @param totalUniqueUnigrams
     */
    public void setTotalUniqueUnigrams(int totalUniqueUnigrams) {
        this.totalUniqueUnigrams = totalUniqueUnigrams;
    }

    /**
     * Set the parent language model.
     *
     * @param parent
     */
    public void setParent(LanguageModel parent) {
        this.parent = parent;
    }

    /**
     * get the children language models.
     *
     * @return list of child language models
     */
    public ArrayList<LanguageModel> getListOfChildren() {
        return listOfChildren;
    }

    /**
     * Set the children language models.
     *
     * @param listOfChildren
     */
    public void setListOfChildren(ArrayList<LanguageModel> listOfChildren) {
        this.listOfChildren = listOfChildren;
    }

    /**
     * Add a children to the list of child language models.
     *
     * @param children
     */
    public void addChildren(LanguageModel children) {
        this.listOfChildren.add(children);
    }

    public boolean hasChildren() {
        return listOfChildren.size() > 0;
    }

    /**
     * Checks whether a language model is empty or not.
     * 
     * @return 
     */
    public boolean isEmpty() {
        return unigramLM.isEmpty();
    }

    /**
     * Computes joint probability or conditional probability of a unigram.
     * 
     * @param unigram
     * @return
     */
    public double getProbabilityUnigram(String unigram) {
        double prob;
        /* Computing probability of a unigram using additive smoothing */
        if (unigramLM.containsKey(unigram)) {
            prob = (1.0 + unigramLM.get(unigram)) / (totalUnigrams + totalUniqueUnigrams);
        } else {
            prob = 1.0 / (totalUnigrams + totalUniqueUnigrams);
        }
        return prob;
    }

    /**
     * Computes joint probability or conditional probability of a bigram.
     * 
     * @param bigram
     * @param isJoint
     * @return
     */
    public double getProbabilityBigram(String bigram, boolean isJoint) {
        double prob;
        /* Computing probability of a bigram using linear interpolation smoothing */
        String[] split = bigram.split(" ");
        if (bigramLM.containsKey(bigram)) {
            prob = (1.0 - lambda) * (bigramLM.get(bigram) / unigramLM.get(split[0]));
        } else {
            prob = 0.0;
        }
        prob += lambda * getProbabilityUnigram(split[0]);
        if (isJoint) {
            prob *= getProbabilityUnigram(split[0]);
        }
        return prob;
    }

    /**
     * Computes joint probability or conditional probability of a trigram.
     *
     * @param trigram
     * @param isJoint
     * @return
     */
    public double getProbabilityTrigram(String trigram, boolean isJoint) {
        double prob;
        /* Computing probability of a trigram using linear interpolation smoothing */
        String[] split = trigram.split(" ");
        String prevBigram = split[0] + " " + split[1];
        if (trigramLM.containsKey(trigram)) {
            prob = (1.0 - lambda) * (trigramLM.get(trigram) / bigramLM.get(prevBigram));
        } else {
            prob = 0.0;
        }
        String bigram = split[1] + " " + split[2];
        prob += lambda * getProbabilityBigram(bigram, false);
        if (isJoint) {
            prob *= getProbabilityBigram(prevBigram, false);
            prob *= getProbabilityUnigram(split[0]);
        }
        return prob;
    }

    /**
     * Computes joint probability or conditional probability of a fourgram.
     *
     * @param fourgram
     * @param isJoint
     * @return if isJoint is true, returns joint probability, otherwise
     * conditional probability
     */
    public double getProbabilityFourgram(String fourgram, boolean isJoint) {
        double prob;
        /* Computing probability of a fourgram using linear interpolation smoothing */
        String[] split = fourgram.split(" ");
        String prevTrigram = split[0] + " " + split[1] + " " + split[2];
        if (fourgramLM.containsKey(fourgram)) {
            prob = (1.0 - lambda) * (fourgramLM.get(fourgram) / trigramLM.get(prevTrigram));
        } else {
            prob = 0.0;
        }
        String trigram = split[1] + " " + split[2] + " " + split[3];
        prob += lambda * getProbabilityTrigram(trigram, false);
        if (isJoint) {
            prob *= getProbabilityBigram(prevTrigram, false);
            String bigram = split[0] + " " + split[1];
            prob *= getProbabilityBigram(bigram, false);
            prob *= getProbabilityUnigram(split[0]);
        }
        return prob;
    }

    /**
     * Set the Maximum and Minimum probability of the language model.
     *
     * @param param either unigram or bigram or trigram or fourgram
     */
    public void setMaxMinProb(String param) {
        double max = -1.0;
        double min = -1.0;
        if (param.equals("unigram")) {
            for (Entry<String, Integer> entry : unigramLM.entrySet()) {
                double prob = getProbabilityUnigram(entry.getKey());
                if (max < prob) {
                    max = prob;
                }
                if (min > prob) {
                    min = prob;
                }
            }
            maxProbUnigram = max;
            minProbUnigram = min;
        } else if (param.equals("bigram")) {
            for (Entry<String, Integer> entry : bigramLM.entrySet()) {
                double prob = getProbabilityBigram(entry.getKey(), true);
                if (max < prob) {
                    max = prob;
                }
                if (min > prob) {
                    min = prob;
                }
            }
            maxProbBigram = max;
            minProbBigram = min;
        } else if (param.equals("trigram")) {
            for (Entry<String, Integer> entry : trigramLM.entrySet()) {
                double prob = getProbabilityTrigram(entry.getKey(), true);
                if (max < prob) {
                    max = prob;
                }
                if (min > prob) {
                    min = prob;
                }
            }
            maxProbTrigram = max;
            minProbTrigram = min;
        } else if (param.equals("fourgram")) {
            for (Entry<String, Integer> entry : fourgramLM.entrySet()) {
                double prob = getProbabilityFourgram(entry.getKey(), true);
                if (max < prob) {
                    max = prob;
                }
                if (min > prob) {
                    min = prob;
                }
            }
            maxProbFourgram = max;
            minProbFourgram = min;
        } else {
            System.err.println("Unknown Parameter...!");
        }
    }

    /**
     * Returns the level of the language model in the tree hierarchy.
     * 
     * @return
     */
    public int getLevel() {
        return level;
    }

    /**
     * Sets the level of the language model in the tree hierarchy.
     * 
     * @param level
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * Returns the upper bound of the unigram language model.
     * 
     * @return maximum probability
     */
    public double getMaxProbUnigram() {
        return maxProbUnigram;
    }

    /**
     * Returns the lower bound of the unigram language model.
     * 
     * @return
     */
    public double getMinProbUnigram() {
        return minProbUnigram;
    }

    /**
     * Returns the upper bound of the bigram language model.
     * 
     * @return
     */
    public double getMaxProbBigram() {
        return maxProbBigram;
    }

    /**
     * Returns the lower bound of the bigram language model.
     * 
     * @return
     */
    public double getMinProbBigram() {
        return minProbBigram;
    }

    /**
     * Returns the upper bound of the trigram language model.
     * 
     * @return
     */
    public double getMaxProbTrigram() {
        return maxProbTrigram;
    }

    /**
     * Returns the lower bound of the trigram language model.
     * 
     * @return
     */
    public double getMinProbTrigram() {
        return minProbTrigram;
    }

    /**
     * Returns the upper bound of the fourgram language model.
     * 
     * @return
     */
    public double getMaxProbFourgram() {
        return maxProbFourgram;
    }

    /**
     * Returns the lower bound of the fourgram language model.
     * 
     * @return
     */
    public double getMinProbFourgram() {
        return minProbFourgram;
    }

    @Override
    public String toString() {
        return "LanguageModel{" + "topic_name=" + topic_name + ", unigramLM=" + unigramLM + ", bigramLM=" + bigramLM + ", trigramLM=" + trigramLM + ", parent=" + parent + ", listOfChildren=" + listOfChildren + '}';
    }

}
