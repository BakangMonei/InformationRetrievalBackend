package com.moneibakang.informationretrievalbackend.controller;

import com.moneibakang.informationretrievalbackend.api.ApiResponse;
import com.moneibakang.informationretrievalbackend.model.EvaluationMetrics;
import com.moneibakang.informationretrievalbackend.service.EvaluationService;
import com.moneibakang.informationretrievalbackend.service.IRPlatformService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/evaluation")
@CrossOrigin(origins = "*")
public class EvaluationController {

    private final EvaluationService evaluationService;
    private final IRPlatformService irPlatformService;

    public EvaluationController(EvaluationService evaluationService, IRPlatformService irPlatformService) {
        this.evaluationService = evaluationService;
        this.irPlatformService = irPlatformService;
    }

    /**
     * Last IR evaluation metrics (same data as {@code GET /evaluation/metrics} on {@link IRPlatformController}).
     */
    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<EvaluationMetrics>> metrics() {
        return ResponseEntity.ok(ApiResponse.ok(irPlatformService.getLastMetrics(), "Current metrics", HttpStatus.OK.value()));
    }

    /**
     * Precision–recall curve points from the last {@link IRPlatformService#runEvaluation} call.
     */
    @GetMapping("/pr-curve")
    public ResponseEntity<ApiResponse<List<double[]>>> prCurve() {
        return ResponseEntity.ok(ApiResponse.ok(irPlatformService.getLastPrCurve(), "Precision-recall curve", HttpStatus.OK.value()));
    }

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