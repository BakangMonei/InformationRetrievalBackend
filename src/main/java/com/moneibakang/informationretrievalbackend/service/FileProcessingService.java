package com.moneibakang.informationretrievalbackend.service;

import com.moneibakang.informationretrievalbackend.model.Document;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FileProcessingService {
    
    public List<Document> processCisiFile(MultipartFile file) throws IOException {
        List<Document> documents = new ArrayList<>();
        String content = new String(file.getBytes());
        
        // Split by .I to get individual documents
        String[] docSections = content.split("\\.I ");
        
        for (String section : docSections) {
            if (section.trim().isEmpty()) continue;
            
            Document doc = new Document();
            
            // Extract ID
            Pattern idPattern = Pattern.compile("(\\d+)");
            Matcher idMatcher = idPattern.matcher(section);
            if (idMatcher.find()) {
                doc.setId(idMatcher.group(1));
            }
            
            // Extract title
            Pattern titlePattern = Pattern.compile("\\.T\\s*(.*?)(?=\\.A|$)");
            Matcher titleMatcher = titlePattern.matcher(section);
            if (titleMatcher.find()) {
                doc.setTitle(titleMatcher.group(1).trim());
            }
            
            // Extract author
            Pattern authorPattern = Pattern.compile("\\.A\\s*(.*?)(?=\\.W|$)");
            Matcher authorMatcher = authorPattern.matcher(section);
            if (authorMatcher.find()) {
                doc.setAuthor(authorMatcher.group(1).trim());
            }
            
            // Extract content
            Pattern contentPattern = Pattern.compile("\\.W\\s*(.*?)(?=\\.X|$)");
            Matcher contentMatcher = contentPattern.matcher(section);
            if (contentMatcher.find()) {
                doc.setContent(contentMatcher.group(1).trim());
            }
            
            documents.add(doc);
        }
        
        return documents;
    }
    
    public List<Document> processPubMedXmlFile(MultipartFile file) throws IOException, ParserConfigurationException, SAXException {
        // TODO: Implement PubMed XML parsing
        // This will parse the PubMed XML format and convert it to Document objects
        return new ArrayList<>();
    }
    
    public List<String> processQueryFile(MultipartFile file) throws IOException {
        List<String> queries = new ArrayList<>();
        String content = new String(file.getBytes());
        
        // Split by .I to get individual queries
        String[] querySections = content.split("\\.I ");
        
        for (String section : querySections) {
            if (section.trim().isEmpty()) continue;
            
            // Extract query text
            Pattern queryPattern = Pattern.compile("\\.W\\s*(.*?)(?=\\.I|$)");
            Matcher queryMatcher = queryPattern.matcher(section);
            if (queryMatcher.find()) {
                queries.add(queryMatcher.group(1).trim());
            }
        }
        
        return queries;
    }
    
    public List<RelevanceJudgment> processRelevanceFile(MultipartFile file) throws IOException {
        List<RelevanceJudgment> judgments = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
        String line;
        
        while ((line = reader.readLine()) != null) {
            String[] parts = line.trim().split("\\s+");
            if (parts.length >= 3) {
                RelevanceJudgment judgment = new RelevanceJudgment();
                judgment.setQueryId(parts[0]);
                judgment.setDocumentId(parts[1]);
                judgment.setRelevance(Integer.parseInt(parts[2]));
                judgments.add(judgment);
            }
        }
        
        return judgments;
    }
    
    public static class RelevanceJudgment {
        private String queryId;
        private String documentId;
        private int relevance;
        
        // Getters and setters
        public String getQueryId() { return queryId; }
        public void setQueryId(String queryId) { this.queryId = queryId; }
        public String getDocumentId() { return documentId; }
        public void setDocumentId(String documentId) { this.documentId = documentId; }
        public int getRelevance() { return relevance; }
        public void setRelevance(int relevance) { this.relevance = relevance; }
    }
} 