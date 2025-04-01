package com.moneibakang.informationretrievalbackend.controller;

import com.moneibakang.informationretrievalbackend.model.Document;
import com.moneibakang.informationretrievalbackend.service.EvaluationService;
import com.moneibakang.informationretrievalbackend.service.IndexingService;
import com.moneibakang.informationretrievalbackend.service.SearchService;
import com.moneibakang.informationretrievalbackend.dto.SearchRequestDTO;
import com.moneibakang.informationretrievalbackend.dto.SearchResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/ir")
public class IRController {
    private final IndexingService indexingService;
    private final SearchService searchService;
    private final EvaluationService evaluationService;

    public IRController(IndexingService indexingService, SearchService searchService, EvaluationService evaluationService) {
        this.indexingService = indexingService;
        this.searchService = searchService;
        this.evaluationService = evaluationService;
    }

    @PostMapping("/index")
    public ResponseEntity<Map<String, Object>> indexDocuments(@RequestBody List<Document> documents) throws IOException {
        long startTime = System.currentTimeMillis();
        indexingService.indexDocuments(documents);
        long endTime = System.currentTimeMillis();
        
        // Calculate total tokens
        int totalTokens = documents.stream()
                .mapToInt(doc -> doc.getContent().split("\\s+").length)
                .sum();
        
        // Get index size (approximate)
        long indexSize = java.nio.file.Files.size(java.nio.file.Paths.get("index"));
        
        Map<String, Object> metrics = Map.of(
            "indexingTime", endTime - startTime,
            "totalTokens", totalTokens,
            "indexSize", indexSize
        );
        
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/search")
    public ResponseEntity<SearchResponseDTO> search(@RequestBody SearchRequestDTO request) throws IOException {
        SearchResponseDTO response = searchService.search(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/evaluate")
    public ResponseEntity<Map<String, Object>> evaluate(
            @RequestParam String query,
            @RequestParam(defaultValue = "tf-idf") String rankingAlgorithm,
            @RequestParam(defaultValue = "false") boolean useStemming) throws IOException {
        Map<String, Object> metrics = evaluationService.evaluateSearch(query, rankingAlgorithm, useStemming);
        return ResponseEntity.ok(metrics);
    }
} 