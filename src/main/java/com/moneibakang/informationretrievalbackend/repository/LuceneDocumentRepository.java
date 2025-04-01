package com.moneibakang.informationretrievalbackend.repository;

import com.moneibakang.informationretrievalbackend.model.Document;
import org.apache.lucene.store.Directory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.*;

@Repository
public interface LuceneDocumentRepository {
    // Basic CRUD operations
    Document save(Document document) throws IOException;
    List<Document> saveAll(List<Document> documents) throws IOException;
    Optional<Document> findById(String id) throws IOException;
    List<Document> findAll() throws IOException;
    void deleteById(String id) throws IOException;
    void deleteAll() throws IOException;
    
    // Search operations
    List<Document> search(String query, String rankingAlgorithm) throws IOException;
    Map<String, Object> getIndexStatistics() throws IOException;
    
    // Document conversion methods
    org.apache.lucene.document.Document convertToLuceneDocument(Document document);
    Document convertToDocument(org.apache.lucene.document.Document luceneDoc);
    
    // Lucene internal operations
    Directory getDirectory();
    IndexWriter getIndexWriter() throws IOException;
    IndexSearcher getIndexSearcher() throws IOException;
} 