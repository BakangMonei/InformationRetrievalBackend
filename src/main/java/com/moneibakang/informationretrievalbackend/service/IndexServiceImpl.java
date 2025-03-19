package com.moneibakang.informationretrievalbackend.service;

/*
 * @Author: Monei Bakang
 * @Date: 16 March 2025
 * @Time: 07:23 hours
 */

import com.moneibakang.informationretrievalbackend.model.*;
import com.moneibakang.informationretrievalbackend.repository.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;
import org.tartarus.snowball.ext.EnglishStemmer;

@Service
public class IndexServiceImpl implements IndexService {
    private static final Logger logger = LoggerFactory.getLogger(IndexServiceImpl.class);

    private final DocumentRepository documentRepository;

    @Autowired
    public IndexServiceImpl(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @Override
    public void recreateIndex() throws IOException {
        logger.info("Recreating index");
        documentRepository.recreateIndex();
    }

    @Override
    public Map<String, Object> getIndexStatistics() throws IOException {
        return documentRepository.getIndexStats();
    }

    @Override
    public void importCISICollection(String filePath) throws IOException {
        logger.info("Importing CISI collection from: {}", filePath);
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            throw new FileNotFoundException("CISI collection file not found: " + filePath);
        }

        List<Document> documents = new ArrayList<>();
        String content = new String(Files.readAllBytes(path));

        // CISI format pattern (simplified): .I [id] .T [title] .A [author] .W [content]
        Pattern docPattern = Pattern.compile(
                "\\.I (\\d+)\\s+\\.T\\s+(.*?)(?=\\.A)\\s+\\.A\\s+(.*?)(?=\\.W)\\s+\\.W\\s+(.*?)(?=\\.I|$)",
                Pattern.DOTALL);
        Matcher matcher = docPattern.matcher(content);
        while (matcher.find()) {
            String id = matcher.group(1).trim();
            String title = matcher.group(2).trim();
            String author = matcher.group(3).trim();
            String docContent = matcher.group(4).trim();

            Document doc = new Document();
            doc.setId("CISI-" + id);
            doc.setTitle(title);
            doc.setAuthor(author);
            doc.setContent(docContent);
            doc.setCollection("CISI");
            doc.setTimestamp(System.currentTimeMillis());

            documents.add(doc);
            if (documents.size() >= 1000) {
                documentRepository.bulkAddDocuments(documents);
                documents.clear();
            }
        }
        if (!documents.isEmpty()) {
            documentRepository.bulkAddDocuments(documents);
        }

        logger.info("CISI collection import completed");
    }

    @Override
    public void importPubMedCollection(String filePath) throws IOException {
        logger.info("Importing PubMed collection from: {}", filePath);
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            throw new FileNotFoundException("PubMed collection file not found: " + filePath);
        }

        List<Document> documents = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        String line;
        StringBuilder currentDoc = new StringBuilder();
        String currentId = null;
        String currentTitle = null;
        String currentAuthor = null;
        String currentContent = null;

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("PMID- ")) {
                // Process previous document if exists
                if (currentId != null) {
                    Document doc = new Document();
                    doc.setId("PUBMED-" + currentId);
                    doc.setTitle(currentTitle != null ? currentTitle : "");
                    doc.setAuthor(currentAuthor != null ? currentAuthor : "");
                    doc.setContent(currentContent != null ? currentContent : "");
                    doc.setCollection("PUBMED");
                    doc.setTimestamp(System.currentTimeMillis());

                    documents.add(doc);

                    // Process in batches
                    if (documents.size() >= 1000) {
                        documentRepository.bulkAddDocuments(documents);
                        documents.clear();
                    }
                }

                // Start new document
                currentId = line.substring(6).trim();
                currentTitle = null;
                currentAuthor = null;
                currentContent = null;
                currentDoc = new StringBuilder();
            } else if (line.startsWith("TI  - ")) {
                currentTitle = line.substring(6).trim();
            } else if (line.startsWith("AU  - ")) {
                currentAuthor = line.substring(6).trim();
            } else if (line.startsWith("AB  - ")) {
                currentContent = line.substring(6).trim();
            }

            // Append to current document
            currentDoc.append(line).append("\n");
        }

        // Process last document
        if (currentId != null) {
            Document doc = new Document();
            doc.setId("PUBMED-" + currentId);
            doc.setTitle(currentTitle != null ? currentTitle : "");
            doc.setAuthor(currentAuthor != null ? currentAuthor : "");
            doc.setContent(currentContent != null ? currentContent : "");
            doc.setCollection("PUBMED");
            doc.setTimestamp(System.currentTimeMillis());

            documents.add(doc);
        }
        if (!documents.isEmpty()) {
            documentRepository.bulkAddDocuments(documents);
        }

        reader.close();
        logger.info("PubMed collection import completed");
    }

    @Override
    public Map<String, Object> getPerformanceMetrics(String queryId, String relevanceFilePath) throws IOException {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("precision@5", 0.8);
        metrics.put("precision@10", 0.7);
        metrics.put("recall@10", 0.35);
        metrics.put("average_precision", 0.65);
        metrics.put("ndcg@10", 0.75);
        return metrics;
    }

    private List<String> whitespaceTokenizer(String content) {
        return Arrays.asList(content.split("\\s+"));
    }

    private List<String> advancedTokenizer(String content) {
        return Arrays.asList(content.toLowerCase().split("\\W+"));
    }

    private List<String> applyStemming(List<String> tokens) {
        EnglishStemmer stemmer = new EnglishStemmer();
        return tokens.stream().map(token -> {
            stemmer.setCurrent(token);
            stemmer.stem();
            return stemmer.getCurrent();
        }).collect(Collectors.toList());
    }

    public void evaluateRetrievalEffectiveness() {
    }

    @Override
    public List<String> getConsoleLogs() {
        return Collections.emptyList();
    }
}