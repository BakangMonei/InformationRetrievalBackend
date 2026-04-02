package com.moneibakang.informationretrievalbackend.util;

/*
 * @Author: Monei Bakang
 * @Date: 16 March 2026
 * @Time: 07:24 hours
 */


import com.moneibakang.informationretrievalbackend.model.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.DirectoryReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.*;

/**
 * Utility class for Lucene operations
 */
public class LuceneUtils {

    private static final String INDEX_PATH = "lucene-index";
    private static final String ID_FIELD = "id";
    private static final String TITLE_FIELD = "title";
    private static final String AUTHOR_FIELD = "author";
    private static final String CONTENT_FIELD = "content";

    /**
     * Calculate term frequency for a specific term in the corpus
     *
     * @param directory The Lucene index directory
     * @param field     The field to search in
     * @param term      The term to search for
     * @return The term frequency
     * @throws IOException If there's an error reading the index
     */
    public static long getTermFrequency(Directory directory, String field, String term) throws IOException {
        try (IndexReader reader = DirectoryReader.open(directory)) {
            Term searchTerm = new Term(field, term);
            return reader.totalTermFreq(searchTerm);
        }
    }

    /**
     * Calculate document frequency for a specific term in the corpus
     *
     * @param directory The Lucene index directory
     * @param field     The field to search in
     * @param term      The term to search for
     * @return The document frequency
     * @throws IOException If there's an error reading the index
     */
    public static long getDocumentFrequency(Directory directory, String field, String term) throws IOException {
        try (IndexReader reader = DirectoryReader.open(directory)) {
            Term searchTerm = new Term(field, term);
            return reader.docFreq(searchTerm);
        }
    }

    /**
     * Get the top N most frequent terms in the index
     *
     * @param directory The Lucene index directory
     * @param field     The field to analyze
     * @param n         The number of top terms to return
     * @return A map of terms and their frequencies
     * @throws IOException If there's an error reading the index
     */
    public static Map<String, Long> getTopTerms(Directory directory, String field, int n) throws IOException {
        Map<String, Long> termFrequencies = new HashMap<>();

        try (IndexReader reader = DirectoryReader.open(directory)) {
            for (int i = 0; i < reader.maxDoc(); i++) {
                org.apache.lucene.document.Document doc = reader.storedFields().document(i);
                String content = doc.get(field);
                if (content != null) {
                    String[] terms = content.split("\\s+");
                    for (String term : terms) {
                        term = term.toLowerCase();
                        termFrequencies.put(term, termFrequencies.getOrDefault(term, 0L) + 1);
                    }
                }
            }
        }

        return termFrequencies.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(n)
                .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);
    }



    public static List<Document> convertTopDocsToDocuments(TopDocs topDocs, IndexSearcher searcher) throws IOException {
        List<Document> documents = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            org.apache.lucene.document.Document luceneDoc = searcher.storedFields().document(scoreDoc.doc);
            documents.add(convertToDocument(luceneDoc));
        }
        return documents;
    }

    public static Document getDocument(IndexReader reader, int docId) throws IOException {
        org.apache.lucene.document.Document luceneDoc = reader.storedFields().document(docId);
        return convertToDocument(luceneDoc);
    }

    public static void createIndex(List<Document> documents) throws IOException {
        Path indexPath = Paths.get(INDEX_PATH);
        Directory dir = FSDirectory.open(indexPath);
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(dir, config);

        for (Document doc : documents) {
            org.apache.lucene.document.Document luceneDoc = new org.apache.lucene.document.Document();
            luceneDoc.add(new StringField(ID_FIELD, doc.getId(), Field.Store.YES));
            luceneDoc.add(new TextField(TITLE_FIELD, doc.getTitle(), Field.Store.YES));
            luceneDoc.add(new TextField(AUTHOR_FIELD, doc.getAuthor(), Field.Store.YES));
            luceneDoc.add(new TextField(CONTENT_FIELD, doc.getContent(), Field.Store.YES));
            writer.addDocument(luceneDoc);
        }

        writer.close();
        dir.close();
        analyzer.close();
    }

    public static List<Document> search(String query, String rankingAlgorithm) throws IOException {
        Path indexPath = Paths.get(INDEX_PATH);
        Directory dir = FSDirectory.open(indexPath);
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);

        // Create query
        BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
        String[] terms = query.toLowerCase().split("\\s+");
        for (String term : terms) {
            queryBuilder.add(new TermQuery(new Term(CONTENT_FIELD, term)), BooleanClause.Occur.SHOULD);
        }
        Query luceneQuery = queryBuilder.build();

        // Search
        TopDocs results = searcher.search(luceneQuery, 100);
        List<Document> documents = new ArrayList<>();

        for (ScoreDoc scoreDoc : results.scoreDocs) {
            org.apache.lucene.document.Document luceneDoc = reader.storedFields().document(scoreDoc.doc);
            documents.add(convertToDocument(luceneDoc));
        }

        reader.close();
        dir.close();
        return documents;
    }

    private static Document convertToDocument(org.apache.lucene.document.Document luceneDoc) {
        Document doc = new Document();
        doc.setId(luceneDoc.get(ID_FIELD));
        doc.setTitle(luceneDoc.get(TITLE_FIELD));
        doc.setAuthor(luceneDoc.get(AUTHOR_FIELD));
        doc.setContent(luceneDoc.get(CONTENT_FIELD));
        return doc;
    }
}