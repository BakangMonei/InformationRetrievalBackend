package com.moneibakang.informationretrievalbackend.service;

/*
 * @Author: Monei Bakang
 * @Date: 16 March 2025
 * @Time: 07:23 hours
 */

import com.moneibakang.informationretrievalbackend.dto.*;
import com.moneibakang.informationretrievalbackend.model.Document;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

public interface DocumentService {
    // Basic CRUD operations
    Document save(Document document) throws IOException;
    List<Document> saveAll(List<Document> documents) throws IOException;
    Document findById(String id) throws IOException;
    List<Document> findAll() throws IOException;
    void deleteById(String id) throws IOException;
    void deleteAll() throws IOException;
    
    // Search and indexing operations
    List<Document> searchDocuments(SearchRequestDTO searchRequest) throws IOException;
    Map<String, Object> getIndexStatistics() throws IOException;
    List<Document> processAndIndexFile(MultipartFile file, String dataset, boolean useStemming, String rankingAlgorithm, boolean lengthNormalization) throws IOException;
    
    // Configuration operations
    String getCurrentRankingAlgorithm();
    void configureRankingAlgorithm(String algorithm);
    boolean isLengthNormalizationEnabled();
    String getCurrentTokenizerType();
    void configureTokenizer(String type) throws IOException;
    boolean isStemmingEnabled();
    void configureStemming(boolean enabled) throws IOException;
    void configureLengthNormalization(boolean enabled) throws IOException;
    
    // Document processing operations
    List<Document> bulkImportDocuments(List<DocumentDTO> documents) throws IOException;
    Document updateDocument(String id, DocumentDTO documentDTO) throws IOException;
    List<Document> processUploadedFile(MultipartFile file) throws IOException;
}