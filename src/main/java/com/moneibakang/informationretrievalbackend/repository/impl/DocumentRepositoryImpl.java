package com.moneibakang.informationretrievalbackend.repository.impl;

import com.moneibakang.informationretrievalbackend.dto.SearchRequestDTO;
import com.moneibakang.informationretrievalbackend.dto.SearchResponseDTO;
import com.moneibakang.informationretrievalbackend.model.Document;
import com.moneibakang.informationretrievalbackend.repository.DocumentRepository;
import com.moneibakang.informationretrievalbackend.repository.LuceneDocumentRepository;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.*;

@Repository
public class DocumentRepositoryImpl implements DocumentRepository {
    private final LuceneDocumentRepository luceneRepository;
    private String currentTokenizerType = "standard";
    private boolean stemmingEnabled = false;
    private String currentRankingAlgorithm = "tf-idf";
    private boolean lengthNormalizationEnabled = false;

    public DocumentRepositoryImpl(LuceneDocumentRepository luceneRepository) {
        this.luceneRepository = luceneRepository;
    }

    @Override
    public String addDocument(Document document) throws IOException {
        Document savedDoc = luceneRepository.save(document);
        return savedDoc.getId();
    }

    @Override
    public Optional<Document> findById(String id) throws IOException {
        return luceneRepository.findById(id);
    }

    @Override
    public List<Document> findAll() throws IOException {
        return luceneRepository.findAll();
    }

    @Override
    public boolean updateDocument(Document document) throws IOException {
        try {
            luceneRepository.save(document);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean deleteDocument(String id) throws IOException {
        try {
            luceneRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public SearchResponseDTO search(SearchRequestDTO searchRequest) throws IOException {
        List<Document> results = luceneRepository.search(searchRequest.getQuery(), currentRankingAlgorithm);
        SearchResponseDTO response = new SearchResponseDTO();
        response.setDocuments(results.stream()
                .map(doc -> new com.moneibakang.informationretrievalbackend.dto.DocumentDTO(doc))
                .toList());
        return response;
    }

    @Override
    public void recreateIndex() throws IOException {
        luceneRepository.deleteAll();
    }

    @Override
    public Map<String, Object> getIndexStats() throws IOException {
        return luceneRepository.getIndexStatistics();
    }

    @Override
    public void setTokenizer(String tokenizerType) {
        this.currentTokenizerType = tokenizerType;
    }

    @Override
    public void setStemming(boolean enabled) {
        this.stemmingEnabled = enabled;
    }

    @Override
    public void setRankingAlgorithm(String algorithm) {
        this.currentRankingAlgorithm = algorithm;
    }

    @Override
    public void setLengthNormalization(boolean enabled) {
        this.lengthNormalizationEnabled = enabled;
    }

    @Override
    public List<String> bulkAddDocuments(List<Document> documents) throws IOException {
        List<Document> savedDocs = luceneRepository.saveAll(documents);
        return savedDocs.stream()
                .map(Document::getId)
                .toList();
    }
} 