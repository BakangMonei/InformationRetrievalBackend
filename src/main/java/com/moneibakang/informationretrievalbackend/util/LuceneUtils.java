package com.moneibakang.informationretrievalbackend.util;

/*
 * @Author: Monei Bakang
 * @Date: 16 March 2025
 * @Time: 07:24 hours
 */


import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;

import java.io.*;
import java.util.*;

/**
 * Utility class for Lucene operations
 */
public class LuceneUtils {

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
            // This is a simplified implementation - in a real system, you'd use
            // TermVectors or HighFrequencyTerms for better performance
            for (int i = 0; i < reader.maxDoc(); i++) {
                Document doc = reader.document(i);
                String content = doc.get(field);
                if (content != null) {
                    // Simple tokenization by whitespace for demonstration
                    String[] terms = content.split("\\s+");
                    for (String term : terms) {
                        term = term.toLowerCase();
                        termFrequencies.put(term, termFrequencies.getOrDefault(term, 0L) + 1);
                    }
                }
            }
        }

        // Sort by frequency and take top N
        return termFrequencies.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(n)
                .collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), HashMap::putAll);
    }

/**
 * Calculate precision and recall for a search result
 *
 * @param searchResults The search results
 */
}