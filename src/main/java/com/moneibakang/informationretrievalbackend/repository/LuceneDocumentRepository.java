package com.moneibakang.informationretrievalbackend.repository;

/*
 * @Author: Monei Bakang
 * @Date: 16 March 2025
 * @Time: 07:21 hours
 */

import com.moneibakang.informationretrievalbackend.dto.*;
import com.moneibakang.informationretrievalbackend.exception.*;
import com.moneibakang.informationretrievalbackend.model.*;
import com.moneibakang.informationretrievalbackend.util.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Repository
public class LuceneDocumentRepository implements DocumentRepository {
    private static final Logger logger = LoggerFactory.getLogger(LuceneDocumentRepository.class);

    private final Directory indexDirectory;
    private final AnalyzerFactory analyzerFactory;
    private Analyzer currentAnalyzer;
    private boolean useStemming = false;
    private String rankingAlgorithm = "tf-idf";
    private boolean applyLengthNormalization = true;

    @Autowired
    public LuceneDocumentRepository(Directory indexDirectory, AnalyzerFactory analyzerFactory) {
        this.indexDirectory = indexDirectory;
        this.analyzerFactory = analyzerFactory;
        this.currentAnalyzer = analyzerFactory.getAnalyzer("standard", false);
    }

    @Override
    public String addDocument(Document document) throws IOException {
        if (document.getId() == null || document.getId().isEmpty()) {
            document.setId(UUID.randomUUID().toString());
        }

        try (IndexWriter writer = createIndexWriter()) {
            writer.addDocument(convertToLuceneDocument(document));
            writer.commit();
            return document.getId();
        } catch (IOException e) {
            logger.error("Error adding document to index", e);
            throw new IndexException("Failed to add document to index", e);
        }
    }

    @Override
    public Optional<Document> findById(String id) throws IOException {
        try (IndexReader reader = DirectoryReader.open(indexDirectory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            Term term = new Term("id", id);
            Query query = new TermQuery(term);
            TopDocs hits = searcher.search(query, 1);

            if (hits.totalHits.value > 0) {
                org.apache.lucene.document.Document luceneDoc = searcher.doc(hits.scoreDocs[0].doc);
                return Optional.of(convertFromLuceneDocument(luceneDoc));
            }
            return Optional.empty();
        } catch (IOException e) {
            logger.error("Error finding document by ID: " + id, e);
            throw new IndexException("Failed to find document by ID", e);
        }
    }

    @Override
    public List<Document> findAll() throws IOException {
        try (IndexReader reader = DirectoryReader.open(indexDirectory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            Query query = new MatchAllDocsQuery();
            TopDocs hits = searcher.search(query, Integer.MAX_VALUE);

            List<Document> documents = new ArrayList<>();
            for (ScoreDoc scoreDoc : hits.scoreDocs) {
                org.apache.lucene.document.Document luceneDoc = searcher.doc(scoreDoc.doc);
                documents.add(convertFromLuceneDocument(luceneDoc));
            }
            return documents;
        } catch (IOException e) {
            logger.error("Error finding all documents", e);
            throw new IndexException("Failed to retrieve all documents", e);
        }
    }

    @Override
    public boolean updateDocument(Document document) throws IOException {
        try (IndexWriter writer = createIndexWriter()) {
            writer.updateDocument(new Term("id", document.getId()), convertToLuceneDocument(document));
            writer.commit();
            return true;
        } catch (IOException e) {
            logger.error("Error updating document: " + document.getId(), e);
            throw new IndexException("Failed to update document", e);
        }
    }

    @Override
    public boolean deleteDocument(String id) throws IOException {
        try (IndexWriter writer = createIndexWriter()) {
            writer.deleteDocuments(new Term("id", id));
            writer.commit();
            return true;
        } catch (IOException e) {
            logger.error("Error deleting document: " + id, e);
            throw new IndexException("Failed to delete document", e);
        }
    }

    @Override
    public SearchResponseDTO search(SearchRequestDTO searchRequest) throws IOException {
        long startTime = System.nanoTime();

        try (IndexReader reader = DirectoryReader.open(indexDirectory)) {
            IndexSearcher searcher = new IndexSearcher(reader);

            // Set similarity based on ranking algorithm
            searcher.setSimilarity(getSimilarity(searchRequest.getRankingAlgorithm(),
                    searchRequest.isApplyLengthNormalization()));

            // Create query parser with appropriate analyzer
            Analyzer queryAnalyzer = analyzerFactory.getAnalyzer(
                    searchRequest.getTokenizerType(),
                    searchRequest.isUseStemming()
            );

            QueryParser parser = new QueryParser("content", queryAnalyzer);
            Query query;
            try {
                query = parser.parse(searchRequest.getQuery());

                // Add collection filter if specified
                if (searchRequest.getCollection() != null && !searchRequest.getCollection().isEmpty()) {
                    TermQuery collectionQuery = new TermQuery(new Term("collection", searchRequest.getCollection()));
                    BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
                    booleanQuery.add(query, BooleanClause.Occur.MUST);
                    booleanQuery.add(collectionQuery, BooleanClause.Occur.MUST);
                    query = booleanQuery.build();
                }
            } catch (Exception e) {
                throw new IndexException("Invalid query syntax", e);
            }

            // Calculate pagination parameters
            int pageSize = searchRequest.getResultsPerPage();
            int offset = searchRequest.getPage() * pageSize;

            // Execute search
            TopDocs hits = searcher.search(query, offset + pageSize);

            // Process results
            List<DocumentDTO> resultDocs = new ArrayList<>();
            int endIndex = Math.min(offset + pageSize, hits.scoreDocs.length);

            for (int i = offset; i < endIndex; i++) {
                ScoreDoc scoreDoc = hits.scoreDocs[i];
                org.apache.lucene.document.Document luceneDoc = searcher.doc(scoreDoc.doc);
                Document document = convertFromLuceneDocument(luceneDoc);
                resultDocs.add(new DocumentDTO(document, scoreDoc.score));
            }

            // Calculate query time
            long endTime = System.nanoTime();
            double queryTimeMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

            // Create response object
            SearchResponseDTO response = new SearchResponseDTO(resultDocs, (int) hits.totalHits.value, queryTimeMs);
            response.setPage(searchRequest.getPage());
            response.setTotalPages((int) Math.ceil((double) hits.totalHits.value / pageSize));

            // Add metrics
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("rankingAlgorithm", searchRequest.getRankingAlgorithm());
            metrics.put("tokenizerType", searchRequest.getTokenizerType());
            metrics.put("stemming", searchRequest.isUseStemming());
            metrics.put("lengthNormalization", searchRequest.isApplyLengthNormalization());
            response.setMetrics(metrics);

            return response;
        } catch (IOException e) {
            logger.error("Error searching documents", e);
            throw new IndexException("Failed to search documents", e);
        }
    }

    @Override
    public void recreateIndex() throws IOException {
        try (IndexWriter writer = new IndexWriter(indexDirectory,
                new IndexWriterConfig(currentAnalyzer).setOpenMode(IndexWriterConfig.OpenMode.CREATE))) {
            // This will delete the existing index and create a new one
            writer.commit();
        } catch (IOException e) {
            logger.error("Error recreating index", e);
            throw new IndexException("Failed to recreate index", e);
        }
    }

    @Override
    public Map<String, Object> getIndexStats() throws IOException {
        Map<String, Object> stats = new HashMap<>();
        try (IndexReader reader = DirectoryReader.open(indexDirectory)) {
            stats.put("numDocs", reader.numDocs());
            stats.put("numDeletedDocs", reader.numDeletedDocs());
            stats.put("maxDoc", reader.maxDoc());
            stats.put("hasDeletions", reader.hasDeletions());
            stats.put("tokenizerType", analyzerFactory.getCurrentTokenizerType());
            stats.put("useStemming", useStemming);
            stats.put("rankingAlgorithm", rankingAlgorithm);
            stats.put("applyLengthNormalization", applyLengthNormalization);

            // Get term statistics for document fields
            Map<String, Long> termCounts = new HashMap<>();
            for (LeafReaderContext context : reader.leaves()) {
                LeafReader leafReader = context.reader();
                Terms terms = leafReader.terms("content");
                if (terms != null) {
                    termCounts.put("uniqueTerms", termCounts.getOrDefault("uniqueTerms", 0L) + terms.size());
                    termCounts.put("totalTerms", termCounts.getOrDefault("totalTerms", 0L) + terms.getSumTotalTermFreq());
                }
            }
            stats.putAll(termCounts);
        } catch (IOException e) {
            logger.error("Error getting index stats", e);
            throw new IndexException("Failed to get index statistics", e);
        }
        return stats;
    }

    @Override
    public void setTokenizer(String tokenizerType) {
        this.currentAnalyzer = analyzerFactory.getAnalyzer(tokenizerType, this.useStemming);
    }

    @Override
    public void setStemming(boolean enabled) {
        this.useStemming = enabled;
        this.currentAnalyzer = analyzerFactory.getAnalyzer(
                analyzerFactory.getCurrentTokenizerType(),
                enabled
        );
    }

    @Override
    public void setRankingAlgorithm(String algorithm) {
        this.rankingAlgorithm = algorithm;
    }

    @Override
    public void setLengthNormalization(boolean enabled) {
        this.applyLengthNormalization = enabled;
    }

    @Override
    public List<String> bulkAddDocuments(List<Document> documents) throws IOException {
        List<String> ids = new ArrayList<>();
        try (IndexWriter writer = createIndexWriter()) {
            for (Document document : documents) {
                if (document.getId() == null || document.getId().isEmpty()) {
                    document.setId(UUID.randomUUID().toString());
                }
                writer.addDocument(convertToLuceneDocument(document));
                ids.add(document.getId());
            }
            writer.commit();
        } catch (IOException e) {
            logger.error("Error bulk adding documents", e);
            throw new IndexException("Failed to bulk add documents", e);
        }
        return ids;
    }

    // Helper methods
    private IndexWriter createIndexWriter() throws IOException {
        IndexWriterConfig config = new IndexWriterConfig(currentAnalyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        return new IndexWriter(indexDirectory, config);
    }

    private org.apache.lucene.document.Document convertToLuceneDocument(Document document) {
        org.apache.lucene.document.Document luceneDoc = new org.apache.lucene.document.Document();

        luceneDoc.add(new StringField("id", document.getId(), Field.Store.YES));
        luceneDoc.add(new TextField("title", document.getTitle(), Field.Store.YES));
        luceneDoc.add(new TextField("content", document.getContent(), Field.Store.YES));
        luceneDoc.add(new StringField("collection", document.getCollection(), Field.Store.YES));
        luceneDoc.add(new StringField("author", document.getAuthor() != null ? document.getAuthor() : "", Field.Store.YES));
        luceneDoc.add(new StringField("timestamp", String.valueOf(document.getTimestamp()), Field.Store.YES));

        return luceneDoc;
    }

    private Document convertFromLuceneDocument(org.apache.lucene.document.Document luceneDoc) {
        Document document = new Document();
        document.setId(luceneDoc.get("id"));
        document.setTitle(luceneDoc.get("title"));
        document.setContent(luceneDoc.get("content"));
        document.setCollection(luceneDoc.get("collection"));
        document.setAuthor(luceneDoc.get("author"));

        String timestampStr = luceneDoc.get("timestamp");
        if (timestampStr != null && !timestampStr.isEmpty()) {
            try {
                document.setTimestamp(Long.parseLong(timestampStr));
            } catch (NumberFormatException e) {
                document.setTimestamp(System.currentTimeMillis());
            }
        }

        return document;
    }

    private Similarity getSimilarity(String algorithm, boolean applyLengthNormalization) {
        if ("tf".equals(algorithm)) {
            // Custom TF-only similarity
            return new ClassicSimilarity() {
                @Override
                public float idf(long docFreq, long docCount) {
                    return 1.0f; // Neutralize IDF component
                }
            };
        } else if ("tf-idf".equals(algorithm)) {
            return new ClassicSimilarity(); // Classic TF-IDF
        } else {
            return new BM25Similarity(); // Default to BM25
        }
        // Note: Length normalization is built into these similarities
        // ClassicSimilarity (former TFIDFSimilarity) applies normalization by default
    }
}
