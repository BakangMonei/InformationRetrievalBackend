package com.moneibakang.informationretrievalbackend.dto;
/*
 * @Author: Monei Bakang
 * @Date: 16 March 2025
 * @Time: 07:21 hours
 */

public class SearchRequestDTO {
    private String query, collection, rankingAlgorithm = "tf-idf", tokenizerType = "standard"; // default value
    private boolean useStemming = false, applyLengthNormalization = true;
    private int resultsPerPage = 10, page = 0;

    // Getters and Setters
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getRankingAlgorithm() {
        return rankingAlgorithm;
    }

    public void setRankingAlgorithm(String rankingAlgorithm) {
        this.rankingAlgorithm = rankingAlgorithm;
    }

    public boolean isUseStemming() {
        return useStemming;
    }

    public void setUseStemming(boolean useStemming) {
        this.useStemming = useStemming;
    }

    public boolean isApplyLengthNormalization() {
        return applyLengthNormalization;
    }

    public void setApplyLengthNormalization(boolean applyLengthNormalization) {
        this.applyLengthNormalization = applyLengthNormalization;
    }

    public String getTokenizerType() {
        return tokenizerType;
    }

    public void setTokenizerType(String tokenizerType) {
        this.tokenizerType = tokenizerType;
    }

    public int getResultsPerPage() {
        return resultsPerPage;
    }

    public void setResultsPerPage(int resultsPerPage) {
        this.resultsPerPage = resultsPerPage;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}