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
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DocumentServiceImpl implements DocumentService {
    private final Logger logger = LoggerFactory.getLogger(DocumentServiceImpl.class);
    private final DocumentRepository documentRepository;
    private String currentTokenizerType = "standard";
    private boolean stemmingEnabled = false;
    private String currentRankingAlgorithm = "tf-idf";
    private boolean lengthNormalizationEnabled = false;

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
        String fileExtension = getFileExtension(file.getOriginalFilename());
        List<Document> documents;
        
        if ("xml".equalsIgnoreCase(fileExtension)) {
            documents = processPubMedXML(file);
        } else {
            documents = processStandardFile(file);
        }

        // Apply tokenization and indexing
        List<String> tokens = tokenizeDocuments(documents, currentTokenizerType);
        if (stemmingEnabled) {
            tokens = applyStemming(tokens);
        }

        // Add documents to repository
        documentRepository.bulkAddDocuments(documents);
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1) : "";
    }

    private List<Document> processPubMedXML(MultipartFile file) throws IOException {
        List<Document> documents = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            StringBuilder currentDoc = new StringBuilder();
            String currentId = null;
            String currentTitle = null;
            String currentAuthor = null;
            String currentContent = null;
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("PMID- ")) {
                    // Process previous document if exists
                    if (currentId != null) {
                        Document doc = createDocument(currentId, currentTitle, currentAuthor, currentContent, "PUBMED");
                        documents.add(doc);
                    }
                    // Start new document
                    currentId = line.substring(6).trim();
                    currentTitle = null;
                    currentAuthor = null;
                    currentContent = null;
                    currentDoc = new StringBuilder();
                }
                // Parse other fields
                else if (line.startsWith("TI  - ")) currentTitle = line.substring(6).trim();
                else if (line.startsWith("AU  - ")) currentAuthor = line.substring(6).trim();
                else if (line.startsWith("AB  - ")) currentContent = line.substring(6).trim();

                currentDoc.append(line).append("\n");
            }

            // Process last document
            if (currentId != null) {
                Document doc = createDocument(currentId, currentTitle, currentAuthor, currentContent, "PUBMED");
                documents.add(doc);
            }
        }
        return documents;
    }

    private List<Document> processStandardFile(MultipartFile file) throws IOException {
        List<Document> documents = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            StringBuilder content = new StringBuilder();
            String line;
            int docId = 1;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() && content.length() > 0) {
                    // Create new document
                    Document doc = createDocument(
                        String.valueOf(docId++),
                        "Document " + docId,
                        null,
                        content.toString().trim(),
                        "STANDARD"
                    );
                    documents.add(doc);
                    content = new StringBuilder();
                } else {
                    content.append(line).append("\n");
                }
            }

            // Process last document if exists
            if (content.length() > 0) {
                Document doc = createDocument(
                    String.valueOf(docId),
                    "Document " + docId,
                    null,
                    content.toString().trim(),
                    "STANDARD"
                );
                documents.add(doc);
            }
        }
        return documents;
    }

    private Document createDocument(String id, String title, String author, String content, String collection) {
        Document doc = new Document();
        doc.setId(id);
        doc.setTitle(title != null ? title : "");
        doc.setAuthor(author != null ? author : "");
        doc.setContent(content != null ? content : "");
        doc.setCollection(collection);
        doc.setTimestamp(System.currentTimeMillis());
        return doc;
    }

    private List<String> tokenizeDocuments(List<Document> documents, String tokenizerType) {
        List<String> allTokens = new ArrayList<>();
        for (Document doc : documents) {
            List<String> tokens;
            if ("standard".equals(tokenizerType)) {
                tokens = whitespaceTokenizer(doc.getContent());
            } else {
                tokens = advancedTokenizer(doc.getContent());
            }
            allTokens.addAll(tokens);
        }
        return allTokens;
    }

    private List<String> whitespaceTokenizer(String content) {
        return Arrays.asList(content.split("\\s+"));
    }

    private List<String> advancedTokenizer(String content) {
        // Remove punctuation, convert to lowercase, and split on whitespace
        return Arrays.asList(content.toLowerCase()
            .replaceAll("[^a-zA-Z0-9\\s]", " ")
            .split("\\s+"));
    }

    private List<String> applyStemming(List<String> tokens) {
        PorterStemmer stemmer = new PorterStemmer();
        return tokens.stream()
            .map(token -> {
                stemmer.setCurrent(token);
                stemmer.stem();
                return stemmer.getCurrent();
            })
            .collect(Collectors.toList());
    }

    private Map<String, Double> calculateScores(List<Document> documents, List<String> tokens,
                                              String rankingAlgorithm, boolean lengthNormalization) {
        Map<String, Double> scores = new HashMap<>();
        
        // Calculate document frequencies
        Map<String, Integer> df = calculateDocumentFrequencies(tokens, documents);
        
        for (Document doc : documents) {
            double score;
            if ("tf-idf".equals(rankingAlgorithm)) {
                score = calculateTfIdfScore(doc, tokens, df, documents.size());
            } else {
                score = calculateTfScore(doc, tokens);
            }
            
            if (lengthNormalization) {
                score /= Math.sqrt(doc.getContent().length());
            }
            
            scores.put(doc.getId(), score);
        }
        
        return scores;
    }

    private double calculateTfIdfScore(Document doc, List<String> tokens, Map<String, Integer> df, int totalDocs) {
        double score = 0.0;
        Map<String, Integer> tf = calculateTermFrequencies(doc.getContent());
        
        for (String token : tokens) {
            if (tf.containsKey(token)) {
                double tfScore = 1 + Math.log(tf.get(token));
                double idf = Math.log((double) totalDocs / df.get(token));
                score += tfScore * idf;
            }
        }
        
        return score;
    }

    private double calculateTfScore(Document doc, List<String> tokens) {
        Map<String, Integer> tf = calculateTermFrequencies(doc.getContent());
        return tokens.stream()
            .mapToDouble(token -> tf.getOrDefault(token, 0))
            .sum();
    }

    private Map<String, Integer> calculateTermFrequencies(String content) {
        Map<String, Integer> tf = new HashMap<>();
        List<String> docTokens = Arrays.asList(content.toLowerCase().split("\\s+"));
        for (String token : docTokens) {
            tf.merge(token, 1, Integer::sum);
        }
        return tf;
    }

    private Map<String, Integer> calculateDocumentFrequencies(List<String> tokens, List<Document> documents) {
        Map<String, Integer> df = new HashMap<>();
        for (String token : tokens) {
            int docCount = (int) documents.stream()
                .filter(doc -> doc.getContent().toLowerCase().contains(token.toLowerCase()))
                .count();
            df.put(token, docCount);
        }
        return df;
    }

    private double calculateAverageDocLength(List<Document> documents) {
        return documents.stream()
            .mapToInt(doc -> doc.getContent().split("\\s+").length)
            .average()
            .orElse(0.0);
    }

    private boolean hasRelevanceJudgments() {
        // Implementation depends on your relevance judgments storage
        return false;
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

    @Override
    public Map<String, Object> processAndIndexFile(
        MultipartFile file,
        String tokenizerType,
        boolean useStemming,
        String rankingAlgorithm,
        boolean lengthNormalization
    ) throws IOException {
        long startTime = System.currentTimeMillis();
        
        // Create metrics map
        Map<String, Object> metrics = new HashMap<>();
        
        // Process file based on type
        String fileExtension = getFileExtension(file.getOriginalFilename());
        List<Document> documents = new ArrayList<>();
        
        if ("xml".equalsIgnoreCase(fileExtension)) {
            documents = processPubMedXML(file);
        } else {
            documents = processStandardFile(file);
        }
        
        // Apply tokenization
        List<String> tokens = tokenizeDocuments(documents, tokenizerType);
        
        // Apply stemming if enabled
        if (useStemming) {
            tokens = applyStemming(tokens);
        }
        
        // Calculate ranking scores
        Map<String, Double> scores = calculateScores(
            documents, 
            tokens, 
            rankingAlgorithm, 
            lengthNormalization
        );
        
        // Store documents and update index
        documentRepository.bulkAddDocuments(documents);
        
        // Calculate metrics
        long endTime = System.currentTimeMillis();
        metrics.put("processingTime", endTime - startTime);
        metrics.put("totalDocuments", documents.size());
        metrics.put("totalTokens", tokens.size());
        metrics.put("uniqueTokens", new HashSet<>(tokens).size());
        metrics.put("averageDocLength", calculateAverageDocLength(documents));
        metrics.put("datasetType", fileExtension.equalsIgnoreCase("xml") ? "PubMed" : "Standard");
        
        // Add effectiveness metrics if available
        if (hasRelevanceJudgments()) {
            metrics.putAll(calculateEffectivenessMetrics(documents, scores));
        }
        
        return metrics;
    }

    private Map<String, Object> calculateEffectivenessMetrics(
        List<Document> documents, 
        Map<String, Double> scores
    ) {
        Map<String, Object> metrics = new HashMap<>();
        // Calculate precision, recall, F1 score
        double precision = calculatePrecision(documents, scores);
        double recall = calculateRecall(documents, scores);
        double f1Score = 2 * (precision * recall) / (precision + recall);
        
        metrics.put("precision", precision);
        metrics.put("recall", recall);
        metrics.put("f1Score", f1Score);
        
        return metrics;
    }

    private double calculatePrecision(List<Document> documents, Map<String, Double> scores) {
        // Placeholder: Actual implementation requires relevance judgments
        // For now, return a default value
        return 0.0;
    }

    private double calculateRecall(List<Document> documents, Map<String, Double> scores) {
        // Placeholder: Actual implementation requires relevance judgments
        // For now, return a default value
        return 0.0;
    }
}