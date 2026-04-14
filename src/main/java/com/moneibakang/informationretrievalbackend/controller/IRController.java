package com.moneibakang.informationretrievalbackend.controller;

import com.moneibakang.informationretrievalbackend.dto.DocumentDTO;
import com.moneibakang.informationretrievalbackend.dto.SearchRequestDTO;
import com.moneibakang.informationretrievalbackend.dto.SearchResponseDTO;
import com.moneibakang.informationretrievalbackend.model.Document;
import com.moneibakang.informationretrievalbackend.service.EvaluationService;
import com.moneibakang.informationretrievalbackend.service.IRPlatformService;
import com.moneibakang.informationretrievalbackend.service.IndexingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/ir")
public class IRController {
    private final IndexingService indexingService;
    private final IRPlatformService irPlatformService;
    private final EvaluationService evaluationService;

    public IRController(
            IndexingService indexingService,
            IRPlatformService irPlatformService,
            EvaluationService evaluationService) {
        this.indexingService = indexingService;
        this.irPlatformService = irPlatformService;
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

    /**
     * Ad-hoc search over the same Lucene index as {@code GET /search} (directory {@code index/}),
     * for clients that post {@link SearchRequestDTO} under the {@code /api} prefix.
     */
    @PostMapping("/search")
    public ResponseEntity<SearchResponseDTO> search(@RequestBody SearchRequestDTO request) throws IOException {
        if (request.getQuery() == null || request.getQuery().isBlank()) {
            SearchResponseDTO empty = new SearchResponseDTO();
            empty.setDocuments(List.of());
            empty.setTotalHits(0);
            return ResponseEntity.ok(empty);
        }
        String model = normalizeRankingModel(request.getRankingAlgorithm());
        String tokenizer = request.getTokenizerType() != null ? request.getTokenizerType() : "standard";
        Map<String, Object> raw = irPlatformService.search(
                request.getQuery(),
                model,
                tokenizer,
                request.isUseStemming(),
                false,
                request.getCollection(),
                null,
                null,
                "AND",
                request.getPage(),
                request.getResultsPerPage() > 0 ? request.getResultsPerPage() : 10);
        return ResponseEntity.ok(toSearchResponse(raw));
    }

    private static String normalizeRankingModel(String rankingAlgorithm) {
        if (rankingAlgorithm == null || rankingAlgorithm.isBlank()) {
            return "bm25";
        }
        String r = rankingAlgorithm.trim().toLowerCase(Locale.ROOT);
        if (r.contains("tf-idf") || r.equals("tfidf")) {
            return "tfidf";
        }
        if (r.equals("tf")) {
            return "tf";
        }
        if (r.contains("bm25")) {
            return "bm25";
        }
        if (r.contains("norm")) {
            return "normalized";
        }
        return "bm25";
    }

    @SuppressWarnings("unchecked")
    private static SearchResponseDTO toSearchResponse(Map<String, Object> raw) {
        SearchResponseDTO dto = new SearchResponseDTO();
        List<Map<String, Object>> hits = (List<Map<String, Object>>) raw.getOrDefault("results", List.of());
        List<DocumentDTO> docs = new ArrayList<>();
        for (Map<String, Object> h : hits) {
            DocumentDTO d = new DocumentDTO();
            d.setId(str(h.get("id")));
            d.setTitle(str(h.get("title")));
            d.setContent(str(h.get("content")));
            d.setAuthor(str(h.get("author")));
            d.setDataset(str(h.get("dataset")));
            d.setCollection(str(h.get("category")));
            docs.add(d);
        }
        dto.setDocuments(docs);
        Object total = raw.get("totalHits");
        dto.setTotalHits(total instanceof Number ? ((Number) total).longValue() : docs.size());
        Object lat = raw.get("latencyMs");
        dto.setSearchTime(lat instanceof Number ? ((Number) lat).doubleValue() : 0.0);
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("queryId", raw.get("queryId"));
        metrics.put("page", raw.get("page"));
        metrics.put("size", raw.get("size"));
        dto.setMetrics(metrics);
        return dto;
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o);
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