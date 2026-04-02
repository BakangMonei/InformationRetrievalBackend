package com.moneibakang.informationretrievalbackend.controller;
/*
 * @Author: Monei Bakang
 * @Date: 16 March 2026
 * @Time: 07:21 hours
 */
import com.moneibakang.informationretrievalbackend.service.DocumentService;
import com.moneibakang.informationretrievalbackend.service.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/index")
@CrossOrigin(origins = "http://localhost:3000")// Allow requests from React frontend
public class IndexController {
    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    private final IndexService indexService;
    private final DocumentService documentService;

    @Autowired
    public IndexController(IndexService indexService, DocumentService documentService) {
        this.indexService = indexService;
        this.documentService = documentService;
    }

    // Recreate the index
    @PostMapping("/recreate")
    public ResponseEntity<Void> recreateIndex() {
        try {
            indexService.recreateIndex();
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error recreating index", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get index statistics
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getIndexStats() {
        try {
            Map<String, Object> stats = indexService.getIndexStatistics();
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error getting index statistics", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Import CISI collection
    @PostMapping("/import/cisi")
    public ResponseEntity<String> importCISICollection(
            @RequestParam(required = false) String filePath) {
        try {
            String path = (filePath != null && !filePath.isEmpty())
                    ? filePath
                    : "src/main/resources/CISI.ALL";
            indexService.importCISICollection(path);
            return new ResponseEntity<>("CISI collection imported successfully", HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error importing CISI collection", e);
            return new ResponseEntity<>("Error importing CISI collection: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Import PubMed collection
    @PostMapping("/import/pubmed")
    public ResponseEntity<String> importPubMedCollection(
            @RequestParam(required = false) String filePath) {
        try {
            // Use the provided filePath or fall back to the default one from properties
            String path = (filePath != null && !filePath.isEmpty())
                    ? filePath
                    : "src/main/resources/pubmed25n0006.xml";
            indexService.importPubMedCollection(path);
            return new ResponseEntity<>("PUBMED collection imported successfully", HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error importing PUBMED collection", e);
            return new ResponseEntity<>("Error importing PUBMED collection: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Configure tokenizer
    @PutMapping("/config/tokenizer")
    public ResponseEntity<Void> configureTokenizer(@RequestBody Map<String, String> config) {
        try {
            String type = config.get("type");
            if (type != null) {
                documentService.configureTokenizer(type);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error configuring tokenizer", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Configure stemming
    @PutMapping("/config/stemming")
    public ResponseEntity<Void> configureStemming(@RequestBody Map<String, Boolean> config) {
        try {
            Boolean enabled = config.get("enabled");
            if (enabled != null) {
                documentService.configureStemming(enabled);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error configuring stemming", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Configure ranking algorithm
    @PutMapping("/config/ranking")
    public ResponseEntity<Void> configureRanking(@RequestBody Map<String, String> config) {
        String algorithm = config.get("algorithm");
        if (algorithm != null) {
            documentService.configureRankingAlgorithm(algorithm);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // Configure length normalization
    @PutMapping("/config/normalization")
    public ResponseEntity<Void> configureLengthNormalization(@RequestBody Map<String, Boolean> config) {
        try {
            Boolean enabled = config.get("enabled");
            if (enabled != null) {
                documentService.configureLengthNormalization(enabled);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error configuring length normalization", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get performance metrics
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics(
            @RequestParam(value = "queryId", required = false) String queryId,
            @RequestParam(value = "relevanceFile", required = false) String relevanceFilePath) {
        try {
            // Return empty metrics if parameters are not provided
            if (queryId == null || relevanceFilePath == null) {
                return new ResponseEntity<>(new HashMap<>(), HttpStatus.OK);
            }
            Map<String, Object> metrics = indexService.getPerformanceMetrics(queryId, relevanceFilePath);
            return new ResponseEntity<>(metrics, HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error getting performance metrics", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get current tokenizer configuration
    @GetMapping("/config/tokenizer")
    public ResponseEntity<Map<String, String>> getTokenizerConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("type", documentService.getCurrentTokenizerType());
        return new ResponseEntity<>(config, HttpStatus.OK);
    }

    // Get current stemming configuration
    @GetMapping("/config/stemming")
    public ResponseEntity<Map<String, Boolean>> getStemmingConfig() {
        Map<String, Boolean> config = new HashMap<>();
        config.put("enabled", documentService.isStemmingEnabled());
        return new ResponseEntity<>(config, HttpStatus.OK);
    }

    // Get current ranking configuration
    @GetMapping("/config/ranking")
    public ResponseEntity<Map<String, String>> getRankingConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("algorithm", documentService.getCurrentRankingAlgorithm());
        return new ResponseEntity<>(config, HttpStatus.OK);
    }

    // Get current normalization configuration
    @GetMapping("/config/normalization")
    public ResponseEntity<Map<String, Boolean>> getNormalizationConfig() {
        Map<String, Boolean> config = new HashMap<>();
        config.put("enabled", documentService.isLengthNormalizationEnabled());
        return new ResponseEntity<>(config, HttpStatus.OK);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}