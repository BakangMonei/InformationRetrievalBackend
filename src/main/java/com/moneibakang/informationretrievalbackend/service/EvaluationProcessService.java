package com.moneibakang.informationretrievalbackend.service;

import com.moneibakang.informationretrievalbackend.model.Document;
import com.moneibakang.informationretrievalbackend.service.tokenizer.Tokenizer;
import com.moneibakang.informationretrievalbackend.util.CISIParser;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class EvaluationProcessService {
    private final IndexingService indexingService;
    private final SearchService searchService;
    private final EvaluationService evaluationService;
    private final CISIParser cisiParser;

    public EvaluationProcessService(
            IndexingService indexingService,
            SearchService searchService,
            EvaluationService evaluationService,
            CISIParser cisiParser) {
        this.indexingService = indexingService;
        this.searchService = searchService;
        this.evaluationService = evaluationService;
        this.cisiParser = cisiParser;
    }

    public Map<String, Object> evaluateSystem(
            String documentsPath,
            String queriesPath,
            String relevancePath,
            Tokenizer tokenizer,
            String rankingAlgorithm) throws IOException {
        
        Map<String, Object> results = new HashMap<>();
        
        // Parse dataset
        List<Document> documents = cisiParser.parseDocuments(documentsPath);
        List<String> queries = cisiParser.parseQueries(queriesPath);
        Map<String, Set<String>> relevanceJudgments = cisiParser.parseRelevanceJudgments(relevancePath);
        
        // Index documents
        long startTime = System.currentTimeMillis();
        indexingService.indexDocuments(documents);
        long endTime = System.currentTimeMillis();
        
        // Calculate indexing metrics
        int totalTokens = documents.stream()
                .mapToInt(doc -> doc.getContent().split("\\s+").length)
                .sum();
        
        long indexSize = java.nio.file.Files.size(java.nio.file.Paths.get("index"));
        
        Map<String, Object> indexingMetrics = Map.of(
            "indexingTime", endTime - startTime,
            "totalTokens", totalTokens,
            "indexSize", indexSize
        );
        
        // Run queries and evaluate results
        List<Map<String, Object>> queryResults = new ArrayList<>();
        for (String query : queries) {
            Map<String, Object> metrics = evaluationService.evaluateSearch(query, rankingAlgorithm, false);
            queryResults.add(metrics);
        }
        
        // Calculate average metrics
        Map<String, Object> averageMetrics = calculateAverageMetrics(queryResults);
        
        results.put("indexingMetrics", indexingMetrics);
        results.put("queryResults", queryResults);
        results.put("averageMetrics", averageMetrics);
        
        return results;
    }
    
    private Map<String, Object> calculateAverageMetrics(List<Map<String, Object>> queryResults) {
        Map<String, Object> averages = new HashMap<>();
        
        if (queryResults.isEmpty()) {
            return averages;
        }
        
        // Initialize sums
        Map<String, Double> sums = new HashMap<>();
        for (String metric : queryResults.get(0).keySet()) {
            if (queryResults.get(0).get(metric) instanceof Number) {
                sums.put(metric, 0.0);
            }
        }
        
        // Sum up all metrics
        for (Map<String, Object> result : queryResults) {
            for (Map.Entry<String, Object> entry : result.entrySet()) {
                if (entry.getValue() instanceof Number) {
                    sums.merge(entry.getKey(), ((Number) entry.getValue()).doubleValue(), Double::sum);
                }
            }
        }
        
        // Calculate averages
        int count = queryResults.size();
        for (Map.Entry<String, Double> entry : sums.entrySet()) {
            averages.put(entry.getKey(), entry.getValue() / count);
        }
        
        return averages;
    }
} 