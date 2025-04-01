package com.moneibakang.informationretrievalbackend.service.ranking;

import com.moneibakang.informationretrievalbackend.model.Document;
import com.moneibakang.informationretrievalbackend.service.tokenizer.Tokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TFRanking implements RankingAlgorithm {
    private boolean lengthNormalizationEnabled = true;
    private final Tokenizer tokenizer;

    @Autowired
    public TFRanking(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    @Override
    public List<Document> rank(List<Document> documents, String query) {
        // Tokenize query
        List<String> queryTokens = tokenizer.tokenize(query);
        
        // Calculate TF scores
        Map<String, Double> queryWeights = getTermWeights(query);
        Map<Document, Double> documentScores = new HashMap<>();
        
        for (Document doc : documents) {
            List<String> docTokens = tokenizer.tokenize(doc.getContent());
            Map<String, Double> docWeights = calculateDocumentWeights(docTokens);
            
            double score = calculateCosineSimilarity(queryWeights, docWeights);
            if (lengthNormalizationEnabled) {
                score = normalizeScore(score, docTokens.size());
            }
            
            documentScores.put(doc, score);
        }
        
        // Sort documents by score
        return documentScores.entrySet().stream()
                .sorted(Map.Entry.<Document, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Override
    public String getName() {
        return "tf";
    }

    @Override
    public boolean isLengthNormalizationEnabled() {
        return lengthNormalizationEnabled;
    }

    @Override
    public void setLengthNormalizationEnabled(boolean enabled) {
        this.lengthNormalizationEnabled = enabled;
    }

    @Override
    public Map<String, Double> getTermWeights(String query) {
        List<String> tokens = tokenizer.tokenize(query);
        return calculateDocumentWeights(tokens);
    }

    private Map<String, Double> calculateDocumentWeights(List<String> tokens) {
        Map<String, Double> weights = new HashMap<>();
        Map<String, Integer> termFreq = new HashMap<>();
        
        // Calculate term frequencies
        for (String token : tokens) {
            termFreq.merge(token, 1, Integer::sum);
        }
        
        // Calculate TF weights
        for (Map.Entry<String, Integer> entry : termFreq.entrySet()) {
            String term = entry.getKey();
            int tf = entry.getValue();
            weights.put(term, (double) tf);
        }
        
        return weights;
    }

    private double calculateCosineSimilarity(Map<String, Double> weights1, Map<String, Double> weights2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        Set<String> allTerms = new HashSet<>(weights1.keySet());
        allTerms.addAll(weights2.keySet());
        
        for (String term : allTerms) {
            double w1 = weights1.getOrDefault(term, 0.0);
            double w2 = weights2.getOrDefault(term, 0.0);
            
            dotProduct += w1 * w2;
            norm1 += w1 * w1;
            norm2 += w2 * w2;
        }
        
        if (norm1 == 0.0 || norm2 == 0.0) return 0.0;
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private double normalizeScore(double score, int documentLength) {
        return score / Math.sqrt(documentLength);
    }
} 