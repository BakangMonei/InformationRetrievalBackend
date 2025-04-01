package com.moneibakang.informationretrievalbackend.controller;

import com.moneibakang.informationretrievalbackend.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/evaluation")
@CrossOrigin(origins = "*")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    @PostMapping("/search")
    public ResponseEntity<?> evaluateSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "tf-idf") String rankingAlgorithm,
            @RequestParam(defaultValue = "false") boolean useStemming) {
        try {
            Map<String, Object> metrics = evaluationService.evaluateSearch(query, rankingAlgorithm, useStemming);
            return ResponseEntity.ok(metrics);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to evaluate search",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/compare/tokenizers")
    public ResponseEntity<?> compareTokenizers(@RequestParam String query) {
        try {
            Map<String, Object> comparison = evaluationService.compareTokenizers(query);
            return ResponseEntity.ok(comparison);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to compare tokenizers",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/compare/stemming")
    public ResponseEntity<?> compareStemming(@RequestParam String query) {
        try {
            Map<String, Object> comparison = evaluationService.compareStemming(query);
            return ResponseEntity.ok(comparison);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to compare stemming",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/compare/ranking")
    public ResponseEntity<?> compareRankingAlgorithms(@RequestParam String query) {
        try {
            Map<String, Object> comparison = evaluationService.compareRankingAlgorithms(query);
            return ResponseEntity.ok(comparison);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to compare ranking algorithms",
                "message", e.getMessage()
            ));
        }
    }
} 