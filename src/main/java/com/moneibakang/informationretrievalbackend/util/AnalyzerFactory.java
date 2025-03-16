package com.moneibakang.informationretrievalbackend.util;

/*
* @Author: Monei Bakang
* @Date: 16 March 2025
* @Time: 07:24 hours
*/

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.springframework.stereotype.Component;

@Component
public class AnalyzerFactory {

    private String currentTokenizerType = "standard";

    /**
     * Get the appropriate Analyzer based on the tokenizer type and stemming setting
     *
     * @param tokenizerType The type of tokenizer to use
     * @param useStemming   Whether to use stemming
     * @return The configured Analyzer
     */
    public Analyzer getAnalyzer(String tokenizerType, boolean useStemming) {
        this.currentTokenizerType = tokenizerType;

        // If stemming is enabled, use EnglishAnalyzer which includes Porter stemming
        if (useStemming) {
            return new EnglishAnalyzer();
        }

        // Otherwise, choose analyzer based on tokenizer type
        switch (tokenizerType.toLowerCase()) {
            case "whitespace":
                return new WhitespaceAnalyzer();
            case "simple":
                return new SimpleAnalyzer();
            case "standard":
            default:
                return new StandardAnalyzer();
        }
    }

    /**
     * Get the current tokenizer type
     *
     * @return The current tokenizer type
     */
    public String getCurrentTokenizerType() {
        return currentTokenizerType;
    }
}