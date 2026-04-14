package com.moneibakang.informationretrievalbackend.controller;

import com.moneibakang.informationretrievalbackend.api.ApiResponse;
import com.moneibakang.informationretrievalbackend.service.IRPlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Same search contract as {@code GET /search} on {@link IRPlatformController}, exposed under {@code /api}
 * for clients that use a single {@code /api} base URL.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiSearchController {

    private static final Logger log = LoggerFactory.getLogger(ApiSearchController.class);
    private final IRPlatformService irPlatformService;

    public ApiSearchController(IRPlatformService irPlatformService) {
        this.irPlatformService = irPlatformService;
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Map<String, Object>>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "bm25") String model,
            @RequestParam(defaultValue = "standard") String tokenizer,
            @RequestParam(defaultValue = "false") boolean stemming,
            @RequestParam(defaultValue = "false") boolean expansion,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String keywords,
            @RequestParam(defaultValue = "AND") String operator,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "true") boolean lengthNorm) throws IOException {
        log.info("GET /api/search called query={} model={} tokenizer={} page={} size={}", query, model, tokenizer, page, size);
        Map<String, Object> result = irPlatformService.search(
                query, model, tokenizer, stemming, expansion, category, year, keywords, operator, page, size, lengthNorm);
        return ResponseEntity.ok(ApiResponse.ok(result, "Search completed", 200));
    }

    @PostMapping("/search/expand")
    public ResponseEntity<ApiResponse<Map<String, String>>> expandQuery(@RequestParam String query) throws IOException {
        log.info("POST /api/search/expand called query={}", query);
        String expanded = irPlatformService.expandQuery(query, 5);
        return ResponseEntity.ok(
                ApiResponse.ok(Map.of("original", query, "expanded", expanded), "Query expanded", 200));
    }
}
