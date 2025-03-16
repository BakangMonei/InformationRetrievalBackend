package com.moneibakang.informationretrievalbackend.dto;
/*
 * @Author: Monei Bakang
 * @Date: 16 March 2025
 * @Time: 07:21 hours
 */

import java.util.*;

public class SearchResponseDTO {
    private List<DocumentDTO> results;
    private int totalHits, page, totalPages;
    private double queryTime; // in milliseconds
    private Map<String, Object> metrics; // For precision-recall data

    // Constructors
    public SearchResponseDTO() {
    }

    public SearchResponseDTO(List<DocumentDTO> results, int totalHits, double queryTime) {
        this.results = results;
        this.totalHits = totalHits;
        this.queryTime = queryTime;
    }

    // Getters and Setters
    public List<DocumentDTO> getResults() {
        return results;
    }

    public void setResults(List<DocumentDTO> results) {
        this.results = results;
    }

    public int getTotalHits() {
        return totalHits;
    }

    public void setTotalHits(int totalHits) {
        this.totalHits = totalHits;
    }

    public double getQueryTime() {
        return queryTime;
    }

    public void setQueryTime(double queryTime) {
        this.queryTime = queryTime;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public Map<String, Object> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics;
    }
}