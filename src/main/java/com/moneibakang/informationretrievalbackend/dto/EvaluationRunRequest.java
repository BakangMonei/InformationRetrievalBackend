package com.moneibakang.informationretrievalbackend.dto;

import java.util.List;

public class EvaluationRunRequest {
    private List<String> retrievedDocIds;
    private List<String> relevantDocIds;

    public List<String> getRetrievedDocIds() {
        return retrievedDocIds;
    }

    public void setRetrievedDocIds(List<String> retrievedDocIds) {
        this.retrievedDocIds = retrievedDocIds;
    }

    public List<String> getRelevantDocIds() {
        return relevantDocIds;
    }

    public void setRelevantDocIds(List<String> relevantDocIds) {
        this.relevantDocIds = relevantDocIds;
    }
}
