package com.moneibakang.informationretrievalbackend.service.impl;

import com.moneibakang.informationretrievalbackend.dto.DocumentDTO;
import com.moneibakang.informationretrievalbackend.dto.SearchRequestDTO;
import com.moneibakang.informationretrievalbackend.model.Document;
import com.moneibakang.informationretrievalbackend.repository.LuceneDocumentRepository;
import com.moneibakang.informationretrievalbackend.service.DocumentService;
import com.moneibakang.informationretrievalbackend.service.FileProcessingService;
import com.moneibakang.informationretrievalbackend.util.CISIParser;
import com.moneibakang.informationretrievalbackend.util.PubMedCorpusReader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class DocumentServiceImpl implements DocumentService {
    private final LuceneDocumentRepository documentRepository;
    private final FileProcessingService fileProcessingService;
    private final CISIParser cisiParser;
    private final PubMedCorpusReader pubMedCorpusReader;
    private String currentRankingAlgorithm = "tf";
    private boolean lengthNormalizationEnabled = false;
    private String currentTokenizerType = "standard";
    private boolean stemmingEnabled = false;

    public DocumentServiceImpl(
            LuceneDocumentRepository documentRepository,
            FileProcessingService fileProcessingService,
            CISIParser cisiParser,
            PubMedCorpusReader pubMedCorpusReader) {
        this.documentRepository = documentRepository;
        this.fileProcessingService = fileProcessingService;
        this.cisiParser = cisiParser;
        this.pubMedCorpusReader = pubMedCorpusReader;
    }

    @Override
    public Document save(Document document) throws IOException {
        return documentRepository.save(document);
    }

    @Override
    public List<Document> saveAll(List<Document> documents) throws IOException {
        return documentRepository.saveAll(documents);
    }

    @Override
    public Document findById(String id) throws IOException {
        return documentRepository.findById(id)
                .orElseThrow(() -> new IOException("Document not found with id: " + id));
    }

    @Override
    public List<Document> findAll() throws IOException {
        return documentRepository.findAll();
    }

    @Override
    public void deleteById(String id) throws IOException {
        documentRepository.deleteById(id);
    }

    @Override
    public void deleteAll() throws IOException {
        documentRepository.deleteAll();
    }

    @Override
    public List<Document> searchDocuments(SearchRequestDTO searchRequest) throws IOException {
        return documentRepository.search(searchRequest.getQuery(), currentRankingAlgorithm);
    }

    @Override
    public Map<String, Object> getIndexStatistics() throws IOException {
        return documentRepository.getIndexStatistics();
    }

    @Override
    public List<Document> processAndIndexFile(MultipartFile file, String dataset, boolean useStemming,
            String rankingAlgorithm, boolean lengthNormalization) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("No file provided");
        }
        List<Document> documents;
        if (dataset != null && dataset.toUpperCase(Locale.ROOT).contains("PUBMED")) {
            documents = fileProcessingService.processPubMedXmlFile(file);
        } else {
            documents = fileProcessingService.processCisiFile(file);
        }
        return saveAll(documents);
    }

    @Override
    public String getCurrentRankingAlgorithm() {
        return currentRankingAlgorithm;
    }

    @Override
    public void configureRankingAlgorithm(String algorithm) {
        this.currentRankingAlgorithm = algorithm;
    }

    @Override
    public boolean isLengthNormalizationEnabled() {
        return lengthNormalizationEnabled;
    }

    @Override
    public String getCurrentTokenizerType() {
        return currentTokenizerType;
    }

    @Override
    public void configureTokenizer(String type) throws IOException {
        this.currentTokenizerType = type;
        // TODO: Implement tokenizer configuration in repository
    }

    @Override
    public boolean isStemmingEnabled() {
        return stemmingEnabled;
    }

    @Override
    public void configureStemming(boolean enabled) throws IOException {
        this.stemmingEnabled = enabled;
        // TODO: Implement stemming configuration in repository
    }

    @Override
    public void configureLengthNormalization(boolean enabled) throws IOException {
        this.lengthNormalizationEnabled = enabled;
        // TODO: Implement length normalization configuration in repository
    }

    @Override
    public List<Document> bulkImportDocuments(List<DocumentDTO> documents) throws IOException {
        List<Document> docs = documents.stream()
                .map(this::convertToDocument)
                .toList();
        return saveAll(docs);
    }

    @Override
    public Document updateDocument(String id, DocumentDTO documentDTO) throws IOException {
        Document existingDoc = findById(id);
        if (existingDoc != null) {
            Document updatedDoc = convertToDocument(documentDTO);
            updatedDoc.setId(id);
            return save(updatedDoc);
        }
        return null;
    }

    @Override
    public List<Document> processUploadedFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("No file provided");
        }
        byte[] bytes = file.getBytes();
        String originalName = file.getOriginalFilename();
        String name = originalName == null ? "" : originalName.toLowerCase(Locale.ROOT);
        if (name.contains("cisi") || name.endsWith(".all")) {
            return saveAll(cisiParser.parseDocuments(new ByteArrayInputStream(bytes)));
        }
        return saveAll(pubMedCorpusReader.readCorpus(
                new ByteArrayInputStream(bytes), originalName == null ? "" : originalName));
    }

    private Document convertToDocument(DocumentDTO dto) {
        Document doc = new Document();
        doc.setId(dto.getId());
        doc.setTitle(dto.getTitle());
        doc.setContent(dto.getContent());
        doc.setAuthor(dto.getAuthor());
        doc.setDataset(dto.getDataset());
        doc.setCollection(dto.getCollection());
        doc.setTimestamp(dto.getTimestamp());
        doc.setIndexingTime(dto.getIndexingTime());
        doc.setTokenCount(dto.getTokenCount());
        doc.setStemmed(dto.isStemmed());
        return doc;
    }
} 