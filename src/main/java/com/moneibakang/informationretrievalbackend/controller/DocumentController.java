package com.moneibakang.informationretrievalbackend.controller;
/*
 * @Author: Monei Bakang
 * @Date: 16 March 2025
 * @Time: 07:21 hours
 */

import com.moneibakang.informationretrievalbackend.dto.*;
import com.moneibakang.informationretrievalbackend.exception.*;
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
@CrossOrigin(origins = "http://localhost:3000")// Allow requests from React frontend
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
            DocumentDTO createdDoc = documentService.createDocument(documentDTO);
            return new ResponseEntity<>(createdDoc, HttpStatus.CREATED);
        } catch (IOException e) {
            logger.error("Error creating document", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // READ - Get document by ID
    @GetMapping("/{id}")
    public ResponseEntity<DocumentDTO> getDocumentById(@PathVariable String id) {
        try {
            DocumentDTO doc = documentService.getDocumentById(id);
            return new ResponseEntity<>(doc, HttpStatus.OK);
        } catch (DocumentNotFoundException e) {
            logger.warn("Document not found: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            logger.error("Error getting document: {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // READ - Get all documents
    @GetMapping
    public ResponseEntity<List<DocumentDTO>> getAllDocuments() {
        try {
            List<DocumentDTO> docs = documentService.getAllDocuments();
            return new ResponseEntity<>(docs, HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error getting all documents", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // UPDATE - Update existing document
    @PutMapping("/{id}")
    public ResponseEntity<DocumentDTO> updateDocument(@PathVariable String id, @RequestBody DocumentDTO documentDTO) {
        try {
            DocumentDTO updatedDoc = documentService.updateDocument(id, documentDTO);
            return new ResponseEntity<>(updatedDoc, HttpStatus.OK);
        } catch (DocumentNotFoundException e) {
            logger.warn("Document not found for update: {}", id);
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
            documentService.deleteDocument(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (DocumentNotFoundException e) {
            logger.warn("Document not found for deletion: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
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
            // Handle both GET and POST requests
            SearchRequestDTO finalRequest;
            if (searchRequest == null) {
                finalRequest = new SearchRequestDTO();
                finalRequest.setQuery(query);
            } else {
                finalRequest = searchRequest;
            }
            
            SearchResponseDTO searchResponse = documentService.searchDocuments(finalRequest);
            return new ResponseEntity<>(searchResponse, HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error searching documents", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // BULK IMPORT - Import multiple documents
    @PostMapping("/bulk")
    public ResponseEntity<?> bulkImportDocuments(@RequestParam("file") MultipartFile file,
                                               @RequestParam("tokenizerType") String tokenizerType,
                                               @RequestParam("useStemming") boolean useStemming,
                                               @RequestParam("rankingAlgorithm") String rankingAlgorithm,
                                               @RequestParam("lengthNormalization") boolean lengthNormalization) {
        try {
            if (file.isEmpty()) {
                return new ResponseEntity<>("No file provided", HttpStatus.BAD_REQUEST);
            }

            // Process the file and get metrics
            Map<String, Object> processingResults = documentService.processAndIndexFile(
                file, 
                tokenizerType, 
                useStemming, 
                rankingAlgorithm, 
                lengthNormalization
            );

            return ResponseEntity.ok(processingResults);
        } catch (Exception e) {
            logger.error("Error processing file upload", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error processing file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // FILE UPLOAD - Upload a file for information retrieval
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>("No file uploaded", HttpStatus.BAD_REQUEST);
        }
        try {
            // Process the file (you will need to implement this logic)
            documentService.processUploadedFile(file);
            return new ResponseEntity<>("File uploaded successfully", HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error uploading file", e);
            return new ResponseEntity<>("Error processing file: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}