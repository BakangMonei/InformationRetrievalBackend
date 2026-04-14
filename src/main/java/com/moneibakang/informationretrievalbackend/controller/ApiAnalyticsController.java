package com.moneibakang.informationretrievalbackend.controller;

import com.moneibakang.informationretrievalbackend.api.ApiResponse;
import com.moneibakang.informationretrievalbackend.service.IRPlatformService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

/**
 * Same analytics as {@link IRPlatformController} {@code /analytics/*}, under {@code /api}.
 */
@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class ApiAnalyticsController {

    private final IRPlatformService irPlatformService;

    public ApiAnalyticsController(IRPlatformService irPlatformService) {
        this.irPlatformService = irPlatformService;
    }

    @GetMapping("/term-distribution")
    public ResponseEntity<ApiResponse<Map<String, Object>>> termDistribution() throws IOException {
        return ResponseEntity.ok(ApiResponse.ok(
                irPlatformService.termDistributionStats(), "Term distribution stats", HttpStatus.OK.value()));
    }

    @GetMapping("/zipf")
    public ResponseEntity<ApiResponse<Map<String, Object>>> zipf(
            @RequestParam(defaultValue = "50") int topN,
            @RequestParam(defaultValue = "1") int minFrequency) throws IOException {
        return ResponseEntity.ok(ApiResponse.ok(
                irPlatformService.zipfStats(topN, minFrequency), "Zipf analysis", HttpStatus.OK.value()));
    }
}
