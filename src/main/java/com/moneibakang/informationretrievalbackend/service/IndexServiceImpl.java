package com.moneibakang.informationretrievalbackend.service;

/*
 * @Author: Monei Bakang
 * @Date: 16 March 2026
 * @Time: 07:23 hours
 */

import com.moneibakang.informationretrievalbackend.model.*;
import com.moneibakang.informationretrievalbackend.repository.*;
import com.moneibakang.informationretrievalbackend.util.CISIParser;
import com.moneibakang.informationretrievalbackend.util.DatasetPathResolver;
import com.moneibakang.informationretrievalbackend.util.PubMedCorpusReader;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Service
public class IndexServiceImpl implements IndexService {
    private static final Logger logger = LoggerFactory.getLogger(IndexServiceImpl.class);

    private static final int BATCH = 1000;

    private final DocumentRepository documentRepository;
    private final CISIParser cisiParser;
    private final DatasetPathResolver pathResolver;
    private final PubMedCorpusReader pubMedCorpusReader;

    @Autowired
    public IndexServiceImpl(
            DocumentRepository documentRepository,
            CISIParser cisiParser,
            DatasetPathResolver pathResolver,
            PubMedCorpusReader pubMedCorpusReader) {
        this.documentRepository = documentRepository;
        this.cisiParser = cisiParser;
        this.pathResolver = pathResolver;
        this.pubMedCorpusReader = pubMedCorpusReader;
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
        Path path = pathResolver.resolveCisiPath(
                filePath == null || filePath.isEmpty() ? null : filePath);
        logger.info("Importing CISI collection from: {}", path.toAbsolutePath());

        List<Document> documents = cisiParser.parseDocuments(path.toString());
        if (documents.isEmpty()) {
            throw new IOException(
                    "No CISI documents parsed from " + path.toAbsolutePath()
                            + ". Check the file is valid CISI (.I / .T / .W) and not empty.");
        }
        flushInBatches(documents);
        logger.info("CISI collection import completed ({} documents)", documents.size());
    }

    @Override
    public void importPubMedCollection(String filePath) throws IOException {
        Path path = pathResolver.resolvePubmedPath(
                filePath == null || filePath.isEmpty() ? null : filePath);
        logger.info("Importing PubMed collection from: {}", path.toAbsolutePath());

        List<Document> documents = pubMedCorpusReader.readCorpus(path);
        if (documents.isEmpty()) {
            throw new IOException(
                    "No PubMed documents parsed from " + path.toAbsolutePath()
                            + ". Use a non-empty PubMed XML (e.g. efetch) or MEDLINE flatfile (PMID- / TI  - lines). "
                            + "Optional query param: filePath=/absolute/path/to/your/file.xml");
        }
        flushInBatches(documents);
        logger.info("PubMed collection import completed ({} documents)", documents.size());
    }

    private void flushInBatches(List<Document> documents) throws IOException {
        List<Document> batch = new ArrayList<>(BATCH);
        for (Document d : documents) {
            batch.add(d);
            if (batch.size() >= BATCH) {
                documentRepository.bulkAddDocuments(batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            documentRepository.bulkAddDocuments(batch);
        }
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

    public void evaluateRetrievalEffectiveness() {
    }

    @Override
    public List<String> getConsoleLogs() {
        return Collections.emptyList();
    }
}
