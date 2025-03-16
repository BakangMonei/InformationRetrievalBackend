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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*") // Allow requests from React frontend
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
    @PostMapping("/search")
    public ResponseEntity<SearchResponseDTO> searchDocuments(@RequestBody SearchRequestDTO searchRequest) {
        try {
            SearchResponseDTO searchResponse = documentService.searchDocuments(searchRequest);
            return new ResponseEntity<>(searchResponse, HttpStatus.OK);
        } catch (IOException e) {
            logger.error("Error searching documents", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // BULK IMPORT - Import multiple documents
    @PostMapping("/bulk")
    public ResponseEntity<List<String>> bulkImportDocuments(@RequestBody List<DocumentDTO> documents) {
        try {
            List<String> ids = documentService.bulkImportDocuments(documents);
            return new ResponseEntity<>(ids, HttpStatus.CREATED);
        } catch (IOException e) {
            logger.error("Error bulk importing documents", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}