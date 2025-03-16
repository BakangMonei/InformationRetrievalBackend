package com.moneibakang.informationretrievalbackend.repository;

/*
 * @Author: Monei Bakang
 * @Date: 16 March 2025
 * @Time: 07:21 hours
 */


import com.moneibakang.informationretrievalbackend.dto.*;
import com.moneibakang.informationretrievalbackend.model.*;

import java.io.*;
import java.util.*;

public interface DocumentRepository {
    // CRUD operations
    String addDocument(Document document) throws IOException;

    Optional<Document> findById(String id) throws IOException;

    List<Document> findAll() throws IOException;

    boolean updateDocument(Document document) throws IOException;

    boolean deleteDocument(String id) throws IOException;

    // Search operations
    SearchResponseDTO search(SearchRequestDTO searchRequest) throws IOException;

    // Index management
    void recreateIndex() throws IOException;

    Map<String, Object> getIndexStats() throws IOException;

    // Configuration
    void setTokenizer(String tokenizerType);

    void setStemming(boolean enabled);

    void setRankingAlgorithm(String algorithm);

    void setLengthNormalization(boolean enabled);

    // Bulk operations
    List<String> bulkAddDocuments(List<Document> documents) throws IOException;
}