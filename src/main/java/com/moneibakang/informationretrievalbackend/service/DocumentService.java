package com.moneibakang.informationretrievalbackend.service;

/*
 * @Author: Monei Bakang
 * @Date: 16 March 2025
 * @Time: 07:23 hours
 */


import com.moneibakang.informationretrievalbackend.dto.*;

import java.io.*;
import java.util.*;

public interface DocumentService {
    // CRUD operations
    DocumentDTO createDocument(DocumentDTO documentDTO) throws IOException;

    DocumentDTO getDocumentById(String id) throws IOException;

    List<DocumentDTO> getAllDocuments() throws IOException;

    DocumentDTO updateDocument(String id, DocumentDTO documentDTO) throws IOException;

    void deleteDocument(String id) throws IOException;

    // Search operations
    SearchResponseDTO searchDocuments(SearchRequestDTO searchRequest) throws IOException;

    // Configuration operations
    void configureTokenizer(String type) throws IOException;

    void configureStemming(boolean enabled) throws IOException;

    void configureRankingAlgorithm(String algorithm) throws IOException;

    void configureLengthNormalization(boolean enabled) throws IOException;

    // Bulk operations
    List<String> bulkImportDocuments(List<DocumentDTO> documents) throws IOException;

    // Stats operations
    Map<String, Object> getIndexStatistics() throws IOException;
}