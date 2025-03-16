package com.moneibakang.informationretrievalbackend.controller;
/*
 * @Author: Monei Bakang
 * @Date: 16 March 2025
 * @Time: 07:21 hours
 */

import com.moneibakang.informationretrievalbackend.service.DocumentService;
import com.moneibakang.informationretrievalbackend.service.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/index")
@CrossOrigin(origins = "*") // Allow requests from React frontend
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
            // Use the provided filePath or fall back to the default one from properties
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
    public ResponseEntity<Void> importPubMedCollection(@RequestParam("filePath") String filePath) {
        try {
            indexService.importPubMedCollection(filePath);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error importing PubMed collection", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Configure tokenizer
    @PutMapping("/config/tokenizer")
    public ResponseEntity<Void> configureTokenizer(@RequestParam("type") String tokenizerType) {
        try {
            documentService.configureTokenizer(tokenizerType);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error configuring tokenizer", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Configure stemming
    @PutMapping("/config/stemming")
    public ResponseEntity<Void> configureStemming(@RequestParam("enabled") boolean stemming) {
        try {
            documentService.configureStemming(stemming);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error configuring stemming", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Configure ranking algorithm
    @PutMapping("/config/ranking")
    public ResponseEntity<Void> configureRanking(@RequestParam("algorithm") String algorithm) {
        try {
            documentService.configureRankingAlgorithm(algorithm);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error configuring ranking algorithm", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Configure length normalization
    @PutMapping("/config/normalization")
    public ResponseEntity<Void> configureLengthNormalization(@RequestParam("enabled") boolean normalization) {
        try {
            documentService.configureLengthNormalization(normalization);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error configuring length normalization", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get performance metrics
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics(
            @RequestParam("queryId") String queryId,
            @RequestParam("relevanceFile") String relevanceFilePath) {
        try {
            Map<String, Object> metrics = indexService.getPerformanceMetrics(queryId, relevanceFilePath);
            return new ResponseEntity<>(metrics, HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error getting performance metrics", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}