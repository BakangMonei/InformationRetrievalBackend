package com.moneibakang.informationretrievalbackend.service;

import com.moneibakang.informationretrievalbackend.model.Document;
import com.moneibakang.informationretrievalbackend.model.EvaluationMetrics;
import com.moneibakang.informationretrievalbackend.model.QueryRecord;
import com.moneibakang.informationretrievalbackend.model.ResultRecord;
import com.moneibakang.informationretrievalbackend.repository.LuceneDocumentRepository;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class IRPlatformService {
    private static final Logger log = LoggerFactory.getLogger(IRPlatformService.class);
    private final LuceneDocumentRepository repository;

    private volatile long lastIndexingTimeMs = 0;
    private volatile long lastIndexedTokens = 0;

    private final Map<String, QueryRecord> queryStore = new ConcurrentHashMap<>();
    private final Map<String, ResultRecord> resultStore = new ConcurrentHashMap<>();
    private volatile EvaluationMetrics lastMetrics = new EvaluationMetrics();
    private volatile List<double[]> lastPrCurve = new ArrayList<>();

    public IRPlatformService(LuceneDocumentRepository repository) {
        this.repository = repository;
    }

    public Document createDocument(Document document) throws IOException {
        return repository.save(document);
    }

    public List<Document> listDocuments(int page, int size, String category, Integer year) throws IOException {
        List<Document> all = repository.findAll();
        List<Document> filtered = all.stream()
                .filter(d -> category == null || category.isBlank() || category.equalsIgnoreCase(d.getCollection()))
                .filter(d -> year == null || toYear(d.getTimestamp()) == year)
                .sorted(Comparator.comparingLong(Document::getTimestamp).reversed())
                .toList();
        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        return filtered.subList(from, to);
    }

    public Document getDocument(String id) throws IOException {
        return repository.findById(id).orElse(null);
    }

    public Document updateDocument(String id, Document payload) throws IOException {
        payload.setId(id);
        repository.deleteById(id);
        return repository.save(payload);
    }

    public void deleteDocument(String id) throws IOException {
        repository.deleteById(id);
    }

    public Map<String, Object> rebuildIndex() throws IOException {
        long start = System.currentTimeMillis();
        List<Document> all = repository.findAll();
        repository.deleteAll();
        repository.saveAll(all);
        lastIndexedTokens = all.stream().mapToLong(this::tokenCount).sum();
        lastIndexingTimeMs = System.currentTimeMillis() - start;
        return getIndexStatus();
    }

    public Map<String, Object> getIndexStatus() throws IOException {
        Map<String, Object> stats = repository.getIndexStatistics();
        long sizeBytes = directorySize(Path.of("index"));
        stats.put("sizeBytes", sizeBytes);
        stats.put("lastIndexingTimeMs", lastIndexingTimeMs);
        stats.put("lastIndexedTokens", lastIndexedTokens);
        return stats;
    }

    public Map<String, Object> search(String query,
                                      String model,
                                      boolean stemming,
                                      boolean expansion,
                                      String category,
                                      Integer year,
                                      String keywords,
                                      String operator,
                                      int page,
                                      int size) throws IOException {
        long start = System.currentTimeMillis();
        String queryText = expansion ? expandQuery(query, 5) : query;

        try (DirectoryReader reader = DirectoryReader.open(repository.getDirectory())) {
            IndexSearcher searcher = new IndexSearcher(reader);
            setSimilarity(searcher, model);
            Query luceneQuery = buildQuery(queryText, category, year, keywords, operator, stemming);
            TopDocs topDocs = searcher.search(luceneQuery, Math.max(100, (page + 1) * size));

            int from = Math.min(page * size, topDocs.scoreDocs.length);
            int to = Math.min(from + size, topDocs.scoreDocs.length);
            List<Map<String, Object>> hits = new ArrayList<>();
            List<String> docIds = new ArrayList<>();

            for (int i = from; i < to; i++) {
                ScoreDoc sd = topDocs.scoreDocs[i];
                org.apache.lucene.document.Document ld = searcher.storedFields().document(sd.doc);
                docIds.add(ld.get("id"));
                Map<String, Object> hit = new HashMap<>();
                hit.put("id", ld.get("id"));
                hit.put("title", ld.get("title"));
                hit.put("content", ld.get("content"));
                hit.put("author", ld.get("author"));
                hit.put("dataset", ld.get("dataset"));
                hit.put("category", ld.get("collection"));
                hit.put("score", sd.score);
                hits.add(hit);
            }

            long latency = System.currentTimeMillis() - start;
            String queryId = UUID.randomUUID().toString();
            QueryRecord qr = new QueryRecord();
            qr.setId(queryId);
            qr.setQueryText(queryText);
            qr.setModel(model);
            qr.setExecutedAt(System.currentTimeMillis());
            qr.setLatencyMs(latency);
            queryStore.put(queryId, qr);

            ResultRecord rr = new ResultRecord();
            rr.setId(UUID.randomUUID().toString());
            rr.setQueryId(queryId);
            rr.setDocumentIds(docIds);
            rr.setCreatedAt(System.currentTimeMillis());
            resultStore.put(rr.getId(), rr);

            log.info("search query='{}' model={} hits={} latencyMs={}", queryText, model, topDocs.totalHits.value, latency);

            Map<String, Object> out = new HashMap<>();
            out.put("queryId", queryId);
            out.put("totalHits", topDocs.totalHits.value);
            out.put("page", page);
            out.put("size", size);
            out.put("latencyMs", latency);
            out.put("results", hits);
            return out;
        } catch (Exception e) {
            throw new IOException("Search failed: " + e.getMessage(), e);
        }
    }

    public String expandQuery(String query, int topTerms) throws IOException {
        Map<String, Object> initial = search(query, "bm25", false, false, null, null, null, "AND", 0, 5);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> results = (List<Map<String, Object>>) initial.get("results");
        Map<String, Integer> tf = new HashMap<>();
        for (Map<String, Object> r : results) {
            String content = String.valueOf(r.getOrDefault("content", ""));
            for (String t : tokenize(content, false)) {
                if (t.length() > 2) {
                    tf.merge(t, 1, Integer::sum);
                }
            }
        }
        String expanded = tf.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(topTerms)
                .map(Map.Entry::getKey)
                .collect(Collectors.joining(" "));
        return (query + " " + expanded).trim();
    }

    public EvaluationMetrics runEvaluation(List<String> retrievedDocIds, List<String> relevantDocIds) {
        Set<String> rel = new HashSet<>(relevantDocIds == null ? List.of() : relevantDocIds);
        if (retrievedDocIds == null) {
            retrievedDocIds = List.of();
        }

        int tp = 0;
        for (String id : retrievedDocIds) {
            if (rel.contains(id)) {
                tp++;
            }
        }
        double precision = retrievedDocIds.isEmpty() ? 0.0 : (double) tp / retrievedDocIds.size();
        double recall = rel.isEmpty() ? 0.0 : (double) tp / rel.size();
        double f1 = (precision + recall) == 0 ? 0.0 : 2 * precision * recall / (precision + recall);

        double apNumerator = 0.0;
        int relSeen = 0;
        for (int i = 0; i < retrievedDocIds.size(); i++) {
            if (rel.contains(retrievedDocIds.get(i))) {
                relSeen++;
                apNumerator += ((double) relSeen / (i + 1));
            }
        }
        double map = rel.isEmpty() ? 0.0 : apNumerator / rel.size();

        EvaluationMetrics metrics = new EvaluationMetrics();
        metrics.setPrecision(precision);
        metrics.setRecall(recall);
        metrics.setF1Score(f1);
        metrics.setMap(map);
        lastMetrics = metrics;
        lastPrCurve = buildPrCurve(retrievedDocIds, rel);
        return metrics;
    }

    public EvaluationMetrics getLastMetrics() {
        return lastMetrics;
    }

    public List<double[]> getLastPrCurve() {
        return lastPrCurve;
    }

    public QueryRecord createQueryRecord(QueryRecord query) {
        if (query.getId() == null || query.getId().isBlank()) {
            query.setId(UUID.randomUUID().toString());
        }
        query.setExecutedAt(System.currentTimeMillis());
        queryStore.put(query.getId(), query);
        return query;
    }

    public List<QueryRecord> listQueries() {
        return queryStore.values().stream()
                .sorted(Comparator.comparingLong(QueryRecord::getExecutedAt).reversed())
                .toList();
    }

    public QueryRecord getQuery(String id) {
        return queryStore.get(id);
    }

    public QueryRecord updateQuery(String id, QueryRecord payload) {
        payload.setId(id);
        queryStore.put(id, payload);
        return payload;
    }

    public void deleteQuery(String id) {
        queryStore.remove(id);
    }

    public ResultRecord createResultRecord(ResultRecord result) {
        if (result.getId() == null || result.getId().isBlank()) {
            result.setId(UUID.randomUUID().toString());
        }
        result.setCreatedAt(System.currentTimeMillis());
        resultStore.put(result.getId(), result);
        return result;
    }

    public List<ResultRecord> listResults() {
        return resultStore.values().stream()
                .sorted(Comparator.comparingLong(ResultRecord::getCreatedAt).reversed())
                .toList();
    }

    public ResultRecord getResult(String id) {
        return resultStore.get(id);
    }

    public ResultRecord updateResult(String id, ResultRecord payload) {
        payload.setId(id);
        resultStore.put(id, payload);
        return payload;
    }

    public void deleteResult(String id) {
        resultStore.remove(id);
    }

    public Map<String, Object> termDistributionStats() throws IOException {
        List<Document> all = repository.findAll();
        Map<String, Integer> freq = new HashMap<>();
        for (Document d : all) {
            for (String t : tokenize(d.getContent(), false)) {
                freq.merge(t, 1, Integer::sum);
            }
        }
        List<Map.Entry<String, Integer>> top = freq.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(50)
                .toList();

        Map<String, Object> out = new HashMap<>();
        out.put("vocabularySize", freq.size());
        out.put("topTerms", top);
        out.put("zipfApproximation", zipfSlope(top));
        return out;
    }

    private Query buildQuery(String text, String category, Integer year, String keywords, String operator, boolean stemming) throws Exception {
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        QueryParser parser = new QueryParser("content", new org.apache.lucene.analysis.standard.StandardAnalyzer());
        builder.add(parser.parse(QueryParser.escape(text)), BooleanClause.Occur.MUST);

        if (category != null && !category.isBlank()) {
            builder.add(new TermQuery(new Term("collection", category)), BooleanClause.Occur.FILTER);
        }
        if (year != null) {
            builder.add(new TermQuery(new Term("year", String.valueOf(year))), BooleanClause.Occur.SHOULD);
        }
        if (keywords != null && !keywords.isBlank()) {
            BooleanClause.Occur occur = "OR".equalsIgnoreCase(operator) ? BooleanClause.Occur.SHOULD : BooleanClause.Occur.MUST;
            for (String kw : tokenize(keywords, stemming)) {
                builder.add(new TermQuery(new Term("content", kw)), occur);
            }
        }
        return builder.build();
    }

    private void setSimilarity(IndexSearcher searcher, String model) {
        if ("bm25".equalsIgnoreCase(model)) {
            searcher.setSimilarity(new BM25Similarity());
            return;
        }
        if ("tf".equalsIgnoreCase(model)) {
            searcher.setSimilarity(new ClassicSimilarity() {
                @Override
                public float idf(long docFreq, long docCount) {
                    return 1.0f;
                }
            });
            return;
        }
        searcher.setSimilarity(new ClassicSimilarity());
    }

    private List<String> tokenize(String text, boolean stemming) {
        if (text == null) {
            return List.of();
        }
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("\\W+"))
                .filter(s -> !s.isBlank())
                .map(s -> stemming ? naiveStem(s) : s)
                .toList();
    }

    private String naiveStem(String token) {
        if (token.endsWith("ing") && token.length() > 5) {
            return token.substring(0, token.length() - 3);
        }
        if (token.endsWith("ed") && token.length() > 4) {
            return token.substring(0, token.length() - 2);
        }
        if (token.endsWith("s") && token.length() > 3) {
            return token.substring(0, token.length() - 1);
        }
        return token;
    }

    private int toYear(long epochMs) {
        return java.time.Instant.ofEpochMilli(epochMs).atZone(java.time.ZoneId.systemDefault()).getYear();
    }

    private long tokenCount(Document d) {
        return tokenize(d.getContent(), false).size();
    }

    private long directorySize(Path path) {
        try {
            if (!Files.exists(path)) {
                return 0L;
            }
            try (var s = Files.walk(path)) {
                return s.filter(Files::isRegularFile).mapToLong(p -> p.toFile().length()).sum();
            }
        } catch (IOException e) {
            return 0L;
        }
    }

    private List<double[]> buildPrCurve(List<String> retrieved, Set<String> relevant) {
        List<double[]> curve = new ArrayList<>();
        int tp = 0;
        for (int i = 0; i < retrieved.size(); i++) {
            if (relevant.contains(retrieved.get(i))) {
                tp++;
            }
            double p = (double) tp / (i + 1);
            double r = relevant.isEmpty() ? 0.0 : (double) tp / relevant.size();
            curve.add(new double[]{p, r});
        }
        return curve;
    }

    private double zipfSlope(List<Map.Entry<String, Integer>> rankedTerms) {
        if (rankedTerms.size() < 2) {
            return 0.0;
        }
        double x1 = Math.log(1);
        double y1 = Math.log(rankedTerms.get(0).getValue());
        double x2 = Math.log(rankedTerms.size());
        double y2 = Math.log(Math.max(1, rankedTerms.get(rankedTerms.size() - 1).getValue()));
        return (y2 - y1) / (x2 - x1);
    }
}
