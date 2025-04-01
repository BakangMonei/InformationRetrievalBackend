package com.moneibakang.informationretrievalbackend.service;

import com.moneibakang.informationretrievalbackend.model.Document;
import com.moneibakang.informationretrievalbackend.repository.DocumentRepository;
import com.moneibakang.informationretrievalbackend.dto.SearchRequestDTO;
import com.moneibakang.informationretrievalbackend.dto.SearchResponseDTO;
import com.moneibakang.informationretrievalbackend.dto.DocumentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EvaluationService {

    @Autowired
    private DocumentRepository documentRepository;

    public Map<String, Object> evaluateSearch(String query, String rankingAlgorithm, boolean useStemming) throws IOException {
        long startTime = System.currentTimeMillis();
        
        // Create search request
        SearchRequestDTO searchRequest = new SearchRequestDTO();
        searchRequest.setQuery(query);
        searchRequest.setRankingAlgorithm(rankingAlgorithm);
        searchRequest.setUseStemming(useStemming);
        
        // Perform search
        SearchResponseDTO searchResponse = documentRepository.search(searchRequest);
        List<Document> results = searchResponse.getDocuments().stream()
                .map(this::convertToDocument)
                .collect(Collectors.toList());
        
        // Calculate metrics
        long queryTime = System.currentTimeMillis() - startTime;
        int totalHits = results.size();
        
        // Calculate precision and recall if relevance judgments are available
        double precision = calculatePrecision(results);
        double recall = calculateRecall(results);
        double f1Score = calculateF1Score(precision, recall);
        
        // Calculate tokenization metrics
        Map<String, Object> tokenizationMetrics = calculateTokenizationMetrics(query);
        
        // Calculate ranking metrics
        Map<String, Object> rankingMetrics = calculateRankingMetrics(results);
        
        return Map.of(
            "queryTime", queryTime,
            "totalHits", totalHits,
            "precision", precision,
            "recall", recall,
            "f1Score", f1Score,
            "tokenizationMetrics", tokenizationMetrics,
            "rankingMetrics", rankingMetrics
        );
    }

    private Document convertToDocument(DocumentDTO dto) {
        Document doc = new Document();
        doc.setId(dto.getId());
        doc.setTitle(dto.getTitle());
        doc.setAuthor(dto.getAuthor());
        doc.setContent(dto.getContent());
        return doc;
    }

    public Map<String, Object> compareTokenizers(String query) throws IOException {
        Map<String, Object> standardMetrics = evaluateWithTokenizer(query, "standard");
        Map<String, Object> customMetrics = evaluateWithTokenizer(query, "custom");
        
        return Map.of(
            "standard", standardMetrics,
            "custom", customMetrics
        );
    }

    public Map<String, Object> compareStemming(String query) throws IOException {
        Map<String, Object> withStemming = evaluateWithStemming(query, true);
        Map<String, Object> withoutStemming = evaluateWithStemming(query, false);
        
        return Map.of(
            "withStemming", withStemming,
            "withoutStemming", withoutStemming
        );
    }

    public Map<String, Object> compareRankingAlgorithms(String query) throws IOException {
        Map<String, Object> tfMetrics = evaluateWithRanking(query, "tf");
        Map<String, Object> tfidfMetrics = evaluateWithRanking(query, "tf-idf");
        
        return Map.of(
            "tf", tfMetrics,
            "tfidf", tfidfMetrics
        );
    }

    private Map<String, Object> evaluateWithTokenizer(String query, String tokenizerType) throws IOException {
        documentRepository.setTokenizer(tokenizerType);
        return evaluateSearch(query, "tf-idf", false);
    }

    private Map<String, Object> evaluateWithStemming(String query, boolean useStemming) throws IOException {
        documentRepository.setStemming(useStemming);
        return evaluateSearch(query, "tf-idf", useStemming);
    }

    private Map<String, Object> evaluateWithRanking(String query, String algorithm) throws IOException {
        return evaluateSearch(query, algorithm, false);
    }

    private double calculatePrecision(List<Document> results) {
        // TODO: Implement precision calculation using relevance judgments
        return 0.0;
    }

    private double calculateRecall(List<Document> results) {
        // TODO: Implement recall calculation using relevance judgments
        return 0.0;
    }

    private double calculateF1Score(double precision, double recall) {
        if (precision + recall == 0) return 0.0;
        return 2 * (precision * recall) / (precision + recall);
    }

    private Map<String, Object> calculateTokenizationMetrics(String query) {
        // TODO: Implement tokenization metrics calculation
        return Map.of(
            "tokenCount", 0,
            "uniqueTokenCount", 0,
            "tokenizationTime", 0
        );
    }

    private Map<String, Object> calculateRankingMetrics(List<Document> results) {
        // TODO: Implement ranking metrics calculation
        return Map.of(
            "rankingTime", 0,
            "averageScore", 0.0,
            "scoreDistribution", new ArrayList<>()
        );
    }
} 