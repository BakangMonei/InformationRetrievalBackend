package com.moneibakang.informationretrievalbackend.util;

import com.moneibakang.informationretrievalbackend.model.Document;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Component
public class CISIParser {
    
    public List<Document> parseDocuments(String filePath) throws IOException {
        List<Document> documents = new ArrayList<>();
        Document currentDoc = null;
        StringBuilder content = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(".I")) {
                    if (currentDoc != null) {
                        currentDoc.setContent(content.toString().trim());
                        documents.add(currentDoc);
                        content = new StringBuilder();
                    }
                    String id = line.substring(3).trim();
                    currentDoc = new Document();
                    currentDoc.setId(id);
                    currentDoc.setDataset("CISI");
                } else if (line.startsWith(".T")) {
                    // Skip the .T line
                    continue;
                } else if (line.startsWith(".A")) {
                    currentDoc.setAuthor(line.substring(3).trim());
                } else if (line.startsWith(".W")) {
                    // Skip the .W line
                    continue;
                } else if (line.startsWith(".X")) {
                    // Skip the .X line and subsequent lines
                    break;
                } else if (currentDoc != null) {
                    content.append(line).append(" ");
                }
            }
            
            // Add the last document
            if (currentDoc != null) {
                currentDoc.setContent(content.toString().trim());
                documents.add(currentDoc);
            }
        }
        
        return documents;
    }
    
    public List<String> parseQueries(String filePath) throws IOException {
        List<String> queries = new ArrayList<>();
        StringBuilder queryContent = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(".I")) {
                    if (queryContent.length() > 0) {
                        queries.add(queryContent.toString().trim());
                        queryContent = new StringBuilder();
                    }
                } else if (line.startsWith(".W")) {
                    // Skip the .W line
                    continue;
                } else {
                    queryContent.append(line).append(" ");
                }
            }
            
            // Add the last query
            if (queryContent.length() > 0) {
                queries.add(queryContent.toString().trim());
            }
        }
        
        return queries;
    }
    
    public Map<String, Set<String>> parseRelevanceJudgments(String filePath) throws IOException {
        Map<String, Set<String>> relevanceMap = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 2) {
                    String queryId = parts[0];
                    String docId = parts[1];
                    
                    relevanceMap.computeIfAbsent(queryId, k -> new HashSet<>()).add(docId);
                }
            }
        }
        
        return relevanceMap;
    }
} 