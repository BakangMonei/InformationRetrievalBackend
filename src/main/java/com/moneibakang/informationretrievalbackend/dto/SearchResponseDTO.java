package com.moneibakang.informationretrievalbackend.dto;
/*
 * @Author: Monei Bakang
 * @Date: 16 March 2025
 * @Time: 07:21 hours
 */

import java.util.*;
import lombok.Data;

@Data
public class SearchResponseDTO {
    private List<DocumentDTO> documents;
    private long totalHits;
    private double searchTime;
    private Map<String, Object> metrics;

    public SearchResponseDTO() {
        this.totalHits = 0;
        this.searchTime = 0.0;
    }

    public List<DocumentDTO> getDocuments() {
        return documents;
    }

    public void setDocuments(List<DocumentDTO> documents) {
        this.documents = documents;
        this.totalHits = documents != null ? documents.size() : 0;
    }

    public long getTotalHits() {
        return totalHits;
    }

    public void setTotalHits(long totalHits) {
        this.totalHits = totalHits;
    }

    public double getSearchTime() {
        return searchTime;
    }

    public void setSearchTime(double searchTime) {
        this.searchTime = searchTime;
    }

    public Map<String, Object> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics;
    }
}