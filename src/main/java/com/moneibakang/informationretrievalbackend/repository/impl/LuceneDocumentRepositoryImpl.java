package com.moneibakang.informationretrievalbackend.repository.impl;

import com.moneibakang.informationretrievalbackend.config.IndexAnalysisSettings;
import com.moneibakang.informationretrievalbackend.model.Document;
import com.moneibakang.informationretrievalbackend.repository.LuceneDocumentRepository;
import com.moneibakang.informationretrievalbackend.util.AnalyzerFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class LuceneDocumentRepositoryImpl implements LuceneDocumentRepository {
    private final Directory directory;
    private final AnalyzerFactory analyzerFactory;
    private final IndexAnalysisSettings indexAnalysisSettings;

    public LuceneDocumentRepositoryImpl(
            AnalyzerFactory analyzerFactory,
            IndexAnalysisSettings indexAnalysisSettings) throws IOException {
        Path indexPath = Paths.get("index");
        this.directory = FSDirectory.open(indexPath);
        this.analyzerFactory = analyzerFactory;
        this.indexAnalysisSettings = indexAnalysisSettings;
    }

    @Override
    public Document save(Document document) throws IOException {
        if (document.getId() == null || document.getId().isEmpty()) {
            document.setId(UUID.randomUUID().toString());
        }

        try (IndexWriter writer = getIndexWriter()) {
            writer.updateDocument(new Term("id", document.getId()), convertToLuceneDocument(document));
            writer.commit();
        }
        return document;
    }

    @Override
    public List<Document> saveAll(List<Document> documents) throws IOException {
        List<Document> savedDocs = new ArrayList<>();
        try (IndexWriter writer = getIndexWriter()) {
            for (Document doc : documents) {
                if (doc.getId() == null || doc.getId().isEmpty()) {
                    doc.setId(UUID.randomUUID().toString());
                }
                writer.updateDocument(new Term("id", doc.getId()), convertToLuceneDocument(doc));
                savedDocs.add(doc);
            }
            writer.commit();
        }
        return savedDocs;
    }

    @Override
    public Optional<Document> findById(String id) throws IOException {
        try (IndexReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            Term term = new Term("id", id);
            Query query = new TermQuery(term);
            TopDocs hits = searcher.search(query, 1);

            if (hits.totalHits.value > 0) {
                org.apache.lucene.document.Document luceneDoc = searcher.storedFields().document(hits.scoreDocs[0].doc);
                return Optional.of(convertToDocument(luceneDoc));
            }
            return Optional.empty();
        }
    }

    @Override
    public List<Document> findAll() throws IOException {
        try (IndexReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            Query query = new MatchAllDocsQuery();
            TopDocs hits = searcher.search(query, Integer.MAX_VALUE);

            List<Document> documents = new ArrayList<>();
            for (ScoreDoc scoreDoc : hits.scoreDocs) {
                org.apache.lucene.document.Document luceneDoc = searcher.storedFields().document(scoreDoc.doc);
                documents.add(convertToDocument(luceneDoc));
            }
            return documents;
        }
    }

    @Override
    public void deleteById(String id) throws IOException {
        try (IndexWriter writer = getIndexWriter()) {
            writer.deleteDocuments(new Term("id", id));
            writer.commit();
        }
    }

    @Override
    public void deleteAll() throws IOException {
        try (IndexWriter writer = getIndexWriter()) {
            writer.deleteAll();
            writer.commit();
        }
    }

    @Override
    public List<Document> search(String query, String rankingAlgorithm) throws IOException {
        try (Analyzer analyzer = analyzerFactory.getAnalyzer(
                indexAnalysisSettings.getTokenizerType(),
                indexAnalysisSettings.isStemming())) {
            try (IndexReader reader = DirectoryReader.open(directory)) {
                IndexSearcher searcher = new IndexSearcher(reader);
                applySimilarity(searcher, rankingAlgorithm);

                try {
                    QueryParser parser = new QueryParser("content", analyzer);
                    Query luceneQuery = parser.parse(query);
                    TopDocs hits = searcher.search(luceneQuery, 100);

                    List<Document> documents = new ArrayList<>();
                    for (ScoreDoc scoreDoc : hits.scoreDocs) {
                        org.apache.lucene.document.Document luceneDoc =
                                searcher.storedFields().document(scoreDoc.doc);
                        documents.add(convertToDocument(luceneDoc));
                    }
                    return documents;
                } catch (ParseException e) {
                    throw new IOException("Error parsing query: " + e.getMessage(), e);
                }
            }
        }
    }

    private static void applySimilarity(IndexSearcher searcher, String rankingAlgorithm) {
        if (rankingAlgorithm == null) {
            searcher.setSimilarity(new BM25Similarity());
            return;
        }
        String r = rankingAlgorithm.toLowerCase(Locale.ROOT);
        if (r.contains("bm25")) {
            searcher.setSimilarity(new BM25Similarity());
        } else if ("tf".equals(r) || r.contains("frequency")) {
            searcher.setSimilarity(new ClassicSimilarity() {
                @Override
                public float idf(long docFreq, long docCount) {
                    return 1.0f;
                }
            });
        } else {
            searcher.setSimilarity(new ClassicSimilarity());
        }
    }

    @Override
    public Map<String, Object> getIndexStatistics() throws IOException {
        Map<String, Object> stats = new HashMap<>();
        try (IndexReader reader = DirectoryReader.open(directory)) {
            stats.put("numDocs", reader.numDocs());
            stats.put("numDeletedDocs", reader.numDeletedDocs());
            stats.put("maxDoc", reader.maxDoc());
            stats.put("hasDeletions", reader.hasDeletions());
        }
        stats.put("indexTokenizer", indexAnalysisSettings.getTokenizerType());
        stats.put("indexStemming", indexAnalysisSettings.isStemming());
        return stats;
    }

    @Override
    public org.apache.lucene.document.Document convertToLuceneDocument(Document document) {
        org.apache.lucene.document.Document luceneDoc = new org.apache.lucene.document.Document();
        luceneDoc.add(new StringField("id", safe(document.getId()), Field.Store.YES));
        luceneDoc.add(new TextField("title", safe(document.getTitle()), Field.Store.YES));
        luceneDoc.add(new TextField("content", safe(document.getContent()), Field.Store.YES));
        luceneDoc.add(new StringField("author", safe(document.getAuthor()), Field.Store.YES));
        luceneDoc.add(new StringField("dataset", safe(document.getDataset()), Field.Store.YES));
        luceneDoc.add(new StringField("collection", safe(document.getCollection()), Field.Store.YES));
        luceneDoc.add(new StringField("timestamp", String.valueOf(document.getTimestamp()), Field.Store.YES));
        int year = java.time.Instant.ofEpochMilli(document.getTimestamp())
                .atZone(java.time.ZoneId.systemDefault())
                .getYear();
        luceneDoc.add(new StringField("year", String.valueOf(year), Field.Store.YES));
        return luceneDoc;
    }

    @Override
    public Document convertToDocument(org.apache.lucene.document.Document luceneDoc) {
        Document document = new Document();
        document.setId(luceneDoc.get("id"));
        document.setTitle(luceneDoc.get("title"));
        document.setContent(luceneDoc.get("content"));
        document.setAuthor(luceneDoc.get("author"));
        document.setDataset(luceneDoc.get("dataset"));
        document.setCollection(luceneDoc.get("collection"));

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

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    @Override
    public Directory getDirectory() {
        return directory;
    }

    @Override
    public IndexWriter getIndexWriter() throws IOException {
        Analyzer analyzer = analyzerFactory.getAnalyzer(
                indexAnalysisSettings.getTokenizerType(),
                indexAnalysisSettings.isStemming());
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        return new IndexWriter(directory, config);
    }

    @Override
    public IndexSearcher getIndexSearcher() throws IOException {
        return new IndexSearcher(DirectoryReader.open(directory));
    }
}
