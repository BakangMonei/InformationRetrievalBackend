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
    public ResponseEntity<String> importPubMedCollection(
            @RequestParam(value = "filePath", required = false) String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                return new ResponseEntity<>("File path is required", HttpStatus.BAD_REQUEST);
            }
            
            File file = new File(filePath);
            if (!file.exists()) {
                return new ResponseEntity<>("File not found: " + filePath, HttpStatus.NOT_FOUND);
            }
            
            indexService.importPubMedCollection(filePath);
            return new ResponseEntity<>("PubMed collection imported successfully", HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error importing PubMed collection", e);
            return new ResponseEntity<>("Error importing PubMed collection: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Configure tokenizer
    @RequestMapping(value = "/config/tokenizer", method = {RequestMethod.GET, RequestMethod.PUT})
    public ResponseEntity<Void> configureTokenizer(@RequestParam(value = "type", required = false) String tokenizerType) {
        try {
            if (tokenizerType != null) {
                documentService.configureTokenizer(tokenizerType);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error configuring tokenizer", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Configure stemming
    @RequestMapping(value = "/config/stemming", method = {RequestMethod.GET, RequestMethod.PUT})
    public ResponseEntity<Void> configureStemming(@RequestParam(value = "enabled", required = false) Boolean stemming) {
        try {
            if (stemming != null) {
                documentService.configureStemming(stemming);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error configuring stemming", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Configure ranking algorithm
    @RequestMapping(value = "/config/ranking", method = {RequestMethod.GET, RequestMethod.PUT})
    public ResponseEntity<Void> configureRanking(@RequestParam(value = "algorithm", required = false) String algorithm) {
        try {
            if (algorithm != null) {
                documentService.configureRankingAlgorithm(algorithm);
            }
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error configuring ranking algorithm", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Configure length normalization
    @RequestMapping(value = "/config/normalization", method = {RequestMethod.GET, RequestMethod.PUT})
    public ResponseEntity<Void> configureLengthNormalization(@RequestParam(value = "enabled", required = false) Boolean normalization) {
        try {
            if (normalization != null) {
                documentService.configureLengthNormalization(normalization);
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
}