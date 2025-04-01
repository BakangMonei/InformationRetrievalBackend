package com.moneibakang.informationretrievalbackend.service.ranking;

import com.moneibakang.informationretrievalbackend.model.Document;
import java.util.List;
import java.util.Map;

public interface RankingAlgorithm {
    List<Document> rank(List<Document> documents, String query);
    String getName();
    boolean isLengthNormalizationEnabled();
    void setLengthNormalizationEnabled(boolean enabled);
    Map<String, Double> getTermWeights(String query);
} 