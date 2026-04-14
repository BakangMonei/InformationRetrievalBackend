package com.moneibakang.informationretrievalbackend.controller;

import com.moneibakang.informationretrievalbackend.model.Document;
import com.moneibakang.informationretrievalbackend.service.FileProcessingService;
import com.moneibakang.informationretrievalbackend.service.DocumentService;
import com.moneibakang.informationretrievalbackend.dto.DocumentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "*")
public class FileUploadController {

    @Autowired
    private FileProcessingService fileProcessingService;

    @Autowired
    private DocumentService documentService;

    @PostMapping("/cisi")
    public ResponseEntity<?> uploadCisiFile(@RequestParam("file") MultipartFile file) {
        try {
            List<Document> documents = fileProcessingService.processCisiFile(file);
            List<DocumentDTO> documentDTOs = documents.stream()
                .map(d -> convertToDTO(d, "CISI"))
                .collect(Collectors.toList());
            documentService.bulkImportDocuments(documentDTOs);
            return ResponseEntity.ok(Map.of(
                "message", "Successfully processed CISI file",
                "documentCount", documentDTOs.size(),
                "documentIds", documentDTOs.stream().map(DocumentDTO::getId).collect(Collectors.toList())
            ));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to process CISI file",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/pubmed")
    public ResponseEntity<?> uploadPubMedFile(@RequestParam("file") MultipartFile file) {
        try {
            List<Document> documents = fileProcessingService.processPubMedXmlFile(file);
            List<DocumentDTO> documentDTOs = documents.stream()
                .map(d -> convertToDTO(d, "PUBMED"))
                .collect(Collectors.toList());
            documentService.bulkImportDocuments(documentDTOs);
            return ResponseEntity.ok(Map.of(
                "message", "Successfully processed PubMed file",
                "documentCount", documentDTOs.size(),
                "documentIds", documentDTOs.stream().map(DocumentDTO::getId).collect(Collectors.toList())
            ));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to process PubMed file",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/queries")
    public ResponseEntity<?> uploadQueryFile(@RequestParam("file") MultipartFile file) {
        try {
            List<String> queries = fileProcessingService.processQueryFile(file);
            return ResponseEntity.ok(Map.of(
                "message", "Successfully processed query file",
                "queryCount", queries.size(),
                "queries", queries
            ));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to process query file",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/relevance")
    public ResponseEntity<?> uploadRelevanceFile(@RequestParam("file") MultipartFile file) {
        try {
            List<FileProcessingService.RelevanceJudgment> judgments = 
                fileProcessingService.processRelevanceFile(file);
            return ResponseEntity.ok(Map.of(
                "message", "Successfully processed relevance file",
                "judgmentCount", judgments.size(),
                "judgments", judgments
            ));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to process relevance file",
                "message", e.getMessage()
            ));
        }
    }

    private DocumentDTO convertToDTO(Document doc, String collection) {
        DocumentDTO dto = new DocumentDTO();
        dto.setId(doc.getId());
        dto.setTitle(doc.getTitle());
        dto.setAuthor(doc.getAuthor());
        dto.setContent(doc.getContent());
        dto.setCollection(collection);
        dto.setDataset(collection);
        dto.setTimestamp(doc.getTimestamp() > 0 ? doc.getTimestamp() : System.currentTimeMillis());
        return dto;
    }
}