package com.moneibakang.informationretrievalbackend.controller;
/*
 * @Author: Monei Bakang
 * @Date: 16 March 2026
 * @Time: 07:21 hours
 */

import com.moneibakang.informationretrievalbackend.dto.*;
import com.moneibakang.informationretrievalbackend.model.Document;
import com.moneibakang.informationretrievalbackend.service.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
public class DocumentController {
    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentService documentService;

    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    // CREATE - Add new document
    @PostMapping
    public ResponseEntity<DocumentDTO> createDocument(@RequestBody DocumentDTO documentDTO) {
        try {
            Document doc = documentService.save(documentDTO.toDocument());
            return new ResponseEntity<>(new DocumentDTO(doc), HttpStatus.CREATED);
        } catch (IOException e) {
            logger.error("Error creating document", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // READ - Get document by ID
    @GetMapping("/{id}")
    public ResponseEntity<DocumentDTO> getDocumentById(@PathVariable String id) {
        try {
            Document doc = documentService.findById(id);
            return new ResponseEntity<>(new DocumentDTO(doc), HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error getting document: {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // READ - Get all documents
    @GetMapping
    public ResponseEntity<List<DocumentDTO>> getAllDocuments() {
        try {
            List<Document> docs = documentService.findAll();
            List<DocumentDTO> dtos = docs.stream()
                    .map(DocumentDTO::new)
                    .toList();
            return new ResponseEntity<>(dtos, HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error getting all documents", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // UPDATE - Update existing document
    @PutMapping("/{id}")
    public ResponseEntity<DocumentDTO> updateDocument(@PathVariable String id, @RequestBody DocumentDTO documentDTO) {
        try {
            Document updatedDoc = documentService.updateDocument(id, documentDTO);
            if (updatedDoc != null) {
                return new ResponseEntity<>(new DocumentDTO(updatedDoc), HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            logger.error("Error updating document: {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // DELETE - Delete document
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String id) {
        try {
            documentService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IOException e) {
            logger.error("Error deleting document: {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // SEARCH - Search documents
    @RequestMapping(value = "/search", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<SearchResponseDTO> searchDocuments(
            @RequestParam(value = "q", required = false) String query,
            @RequestBody(required = false) SearchRequestDTO searchRequest) {
        try {
            SearchRequestDTO finalRequest = searchRequest != null ? searchRequest : new SearchRequestDTO();
            if (query != null) {
                finalRequest.setQuery(query);
            }
            
            List<Document> results = documentService.searchDocuments(finalRequest);
            SearchResponseDTO response = new SearchResponseDTO();
            response.setDocuments(results.stream()
                    .map(DocumentDTO::new)
                    .toList());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error searching documents", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // BULK IMPORT - Import multiple documents
    @PostMapping("/bulk")
    public ResponseEntity<?> bulkImportDocuments(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "dataset", defaultValue = "CISI") String dataset,
            @RequestParam(value = "tokenizerType", required = false, defaultValue = "standard") String tokenizerType,
            @RequestParam(value = "useStemming", required = false, defaultValue = "false") boolean useStemming,
            @RequestParam(value = "rankingAlgorithm", required = false, defaultValue = "tf") String rankingAlgorithm,
            @RequestParam(value = "lengthNormalization", required = false, defaultValue = "false") boolean lengthNormalization) {
        try {
            if (file.isEmpty()) {
                return new ResponseEntity<>("No file provided", HttpStatus.BAD_REQUEST);
            }

            try {
                documentService.configureTokenizer(tokenizerType);
            } catch (IOException ignored) {
                // tokenizer wiring is optional for bulk ingest
            }
            documentService.configureStemming(useStemming);
            documentService.configureRankingAlgorithm(rankingAlgorithm);
            documentService.configureLengthNormalization(lengthNormalization);

            List<Document> processedDocs = documentService.processAndIndexFile(
                    file,
                    dataset,
                    useStemming,
                    rankingAlgorithm,
                    lengthNormalization);

            return ResponseEntity.ok(processedDocs);
        } catch (Exception e) {
            logger.error("Error processing file upload", e);
            Map<String, String> errorResponse = Map.of("message", "Error processing file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // FILE UPLOAD - Upload a file for information retrieval
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>(Map.of("message", "No file uploaded"), HttpStatus.BAD_REQUEST);
        }
        try {
            List<Document> saved = documentService.processUploadedFile(file);
            return new ResponseEntity<>(
                    Map.of(
                            "message", "File uploaded and indexed successfully",
                            "documentCount", saved.size()),
                    HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error uploading file", e);
            return new ResponseEntity<>(
                    Map.of("message", "Error processing file: " + e.getMessage()),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}