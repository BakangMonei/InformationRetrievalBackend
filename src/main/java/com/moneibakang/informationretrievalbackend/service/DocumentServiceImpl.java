package com.moneibakang.informationretrievalbackend.service;

/*
 * @Author: Monei Bakang
 * @Date: 16 March 2025
 * @Time: 07:23 hours
 */

import com.moneibakang.informationretrievalbackend.dto.*;
import com.moneibakang.informationretrievalbackend.exception.*;
import com.moneibakang.informationretrievalbackend.model.*;
import com.moneibakang.informationretrievalbackend.repository.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocumentServiceImpl implements DocumentService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentServiceImpl.class);

    private final DocumentRepository documentRepository;

    // Add these fields to track current configuration
    private String currentTokenizerType = "standard";
    private boolean stemmingEnabled = false;
    private String currentRankingAlgorithm = "tf-idf";
    private boolean lengthNormalizationEnabled = true;

    @Autowired
    public DocumentServiceImpl(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public DocumentDTO createDocument(DocumentDTO documentDTO) throws IOException {
        logger.info("Creating new document");
        String docId = documentRepository.addDocument(documentDTO.toDocument());
        documentDTO.setId(docId);
        return documentDTO;
    }

    @Override
    public DocumentDTO getDocumentById(String id) throws IOException {
        logger.info("Getting document by ID: {}", id);
        Optional<Document> documentOpt = documentRepository.findById(id);
        return documentOpt.map(DocumentDTO::new)
                .orElseThrow(() -> new DocumentNotFoundException("Document not found with ID: " + id));
    }

    @Override
    public List<DocumentDTO> getAllDocuments() throws IOException {
        logger.info("Getting all documents");
        return documentRepository.findAll().stream()
                .map(DocumentDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public DocumentDTO updateDocument(String id, DocumentDTO documentDTO) throws IOException {
        logger.info("Updating document with ID: {}", id);

        // Verify document exists
        Optional<Document> existingDoc = documentRepository.findById(id);
        if (existingDoc.isEmpty()) {
            throw new DocumentNotFoundException("Document not found with ID: " + id);
        }

        // Update document
        Document document = documentDTO.toDocument();
        document.setId(id);
        boolean updated = documentRepository.updateDocument(document);

        if (!updated) {
            throw new DocumentNotFoundException("Failed to update document with ID: " + id);
        }

        return documentDTO;
    }

    @Override
    public void deleteDocument(String id) throws IOException {
        logger.info("Deleting document with ID: {}", id);

        // Verify document exists
        Optional<Document> existingDoc = documentRepository.findById(id);
        if (existingDoc.isEmpty()) {
            throw new DocumentNotFoundException("Document not found with ID: " + id);
        }

        boolean deleted = documentRepository.deleteDocument(id);
        if (!deleted) {
            throw new DocumentNotFoundException("Failed to delete document with ID: " + id);
        }
    }

    @Override
    public SearchResponseDTO searchDocuments(SearchRequestDTO searchRequest) throws IOException {
        logger.info("Searching documents with query: {}", searchRequest.getQuery());
        return documentRepository.search(searchRequest);
    }

    @Override
    public String getCurrentTokenizerType() {
        return currentTokenizerType;
    }

    @Override
    public boolean isStemmingEnabled() {
        return stemmingEnabled;
    }

    @Override
    public String getCurrentRankingAlgorithm() {
        return currentRankingAlgorithm;
    }

    @Override
    public boolean isLengthNormalizationEnabled() {
        return lengthNormalizationEnabled;
    }

    @Override
    public void processUploadedFile(MultipartFile file) throws IOException {
        logger.info("Uploaded file: {}", file.getOriginalFilename());
    }

    @Override
    public void configureTokenizer(String type) throws IOException {
        logger.info("Configuring tokenizer: {}", type);
        this.currentTokenizerType = type;
        documentRepository.setTokenizer(type);
    }

    @Override
    public void configureStemming(boolean enabled) throws IOException {
        logger.info("Configuring stemming: {}", enabled);
        this.stemmingEnabled = enabled;
        documentRepository.setStemming(enabled);
    }

    @Override
    public void configureRankingAlgorithm(String algorithm) throws IOException {
        logger.info("Configuring ranking algorithm: {}", algorithm);
        this.currentRankingAlgorithm = algorithm;
        documentRepository.setRankingAlgorithm(algorithm);
    }

    @Override
    public void configureLengthNormalization(boolean enabled) throws IOException {
        logger.info("Configuring length normalization: {}", enabled);
        this.lengthNormalizationEnabled = enabled;
        documentRepository.setLengthNormalization(enabled);
    }

    @Override
    public List<String> bulkImportDocuments(List<DocumentDTO> documentDTOs) throws IOException {
        logger.info("Bulk importing {} documents", documentDTOs.size());
        List<Document> documents = documentDTOs.stream()
                .map(DocumentDTO::toDocument)
                .collect(Collectors.toList());
        return documentRepository.bulkAddDocuments(documents);
    }

    @Override
    public Map<String, Object> getIndexStatistics() throws IOException {
        logger.info("Getting index statistics");
        return documentRepository.getIndexStats();
    }
}