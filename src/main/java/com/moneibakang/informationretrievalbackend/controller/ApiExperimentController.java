package com.moneibakang.informationretrievalbackend.controller;

import com.moneibakang.informationretrievalbackend.api.ApiResponse;
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
import java.util.Map;

/**
 * Mirrors CISI experiment / dataset evaluation under {@code /api} for frontends using one base URL.
 */
@RestController
@RequestMapping("/api/experiments")
@CrossOrigin(origins = "*")
public class ApiExperimentController {

    private final IRPlatformService irPlatformService;

    public ApiExperimentController(IRPlatformService irPlatformService) {
        this.irPlatformService = irPlatformService;
    }

    @PostMapping("/run")
    public ResponseEntity<ApiResponse<Map<String, Object>>> runCisiBenchmark() throws IOException {
        Map<String, Object> result = irPlatformService.runCisiExperiment();
        return ResponseEntity.ok(ApiResponse.ok(result, "CISI experiment complete", HttpStatus.OK.value()));
    }

    @GetMapping("/dataset-eval")
    public ResponseEntity<ApiResponse<Map<String, Object>>> evaluateDataset(
            @RequestParam(defaultValue = "CISI") String dataset,
            @RequestParam(required = false) String queryFilePath,
            @RequestParam(required = false) String relevanceFilePath) throws IOException {
        Map<String, Object> result =
                irPlatformService.runDatasetEvaluation(dataset, queryFilePath, relevanceFilePath);
        return ResponseEntity.ok(ApiResponse.ok(result, "Dataset evaluation complete", HttpStatus.OK.value()));
    }
}
