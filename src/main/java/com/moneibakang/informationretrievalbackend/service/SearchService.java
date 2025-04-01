package com.moneibakang.informationretrievalbackend.service;

import com.moneibakang.informationretrievalbackend.model.Document;
import com.moneibakang.informationretrievalbackend.service.tokenizer.Tokenizer;
import com.moneibakang.informationretrievalbackend.dto.SearchRequestDTO;
import com.moneibakang.informationretrievalbackend.dto.SearchResponseDTO;
import com.moneibakang.informationretrievalbackend.dto.DocumentDTO;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchService {
    private final Directory directory;
    private final Analyzer analyzer;
    private final Tokenizer tokenizer;

    public SearchService(Tokenizer tokenizer) throws IOException {
        this.tokenizer = tokenizer;
        Path indexPath = Paths.get("index_" + tokenizer.getName().toLowerCase());
        this.directory = FSDirectory.open(indexPath);
        this.analyzer = new StandardAnalyzer();
    }

    public SearchResponseDTO search(SearchRequestDTO request) throws IOException {
        // Set stemming if requested
        tokenizer.setStemmingEnabled(request.isUseStemming());
        
        // Perform search
        List<Document> documents = search(request.getQuery(), request.getRankingAlgorithm());
        
        // Convert to DTOs
        List<DocumentDTO> documentDTOs = documents.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // Create response
        SearchResponseDTO response = new SearchResponseDTO();
        response.setDocuments(documentDTOs);
        response.setTotalHits(documents.size());
        return response;
    }

    public List<Document> search(String query, String rankingAlgorithm) throws IOException {
        List<String> queryTokens = tokenizer.tokenize(query);
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);

        Query luceneQuery;
        if ("tfidf".equals(rankingAlgorithm)) {
            luceneQuery = createTFIDFQuery(queryTokens);
        } else {
            luceneQuery = createTFQuery(queryTokens);
        }

        TopDocs results = searcher.search(luceneQuery, 100);
        return convertToDocuments(results, searcher);
    }

    private Query createTFQuery(List<String> queryTokens) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        for (String token : queryTokens) {
            builder.add(new TermQuery(new Term("tokens", token)), BooleanClause.Occur.SHOULD);
        }
        return builder.build();
    }

    private Query createTFIDFQuery(List<String> queryTokens) {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        for (String token : queryTokens) {
            builder.add(new TermQuery(new Term("tokens", token)), BooleanClause.Occur.SHOULD);
        }
        return builder.build();
    }

    private List<Document> convertToDocuments(TopDocs results, IndexSearcher searcher) throws IOException {
        List<Document> documents = new ArrayList<>();
        for (ScoreDoc scoreDoc : results.scoreDocs) {
            org.apache.lucene.document.Document luceneDoc = searcher.storedFields().document(scoreDoc.doc);
            Document doc = new Document();
            doc.setId(luceneDoc.get("id"));
            doc.setTitle(luceneDoc.get("title"));
            doc.setContent(luceneDoc.get("content"));
            doc.setAuthor(luceneDoc.get("author"));
            doc.setDataset(luceneDoc.get("dataset"));
            documents.add(doc);
        }
        return documents;
    }

    private DocumentDTO convertToDTO(Document doc) {
        DocumentDTO dto = new DocumentDTO();
        dto.setId(doc.getId());
        dto.setTitle(doc.getTitle());
        dto.setContent(doc.getContent());
        dto.setAuthor(doc.getAuthor());
        dto.setDataset(doc.getDataset());
        return dto;
    }

    public void close() throws IOException {
        directory.close();
        analyzer.close();
    }
} 