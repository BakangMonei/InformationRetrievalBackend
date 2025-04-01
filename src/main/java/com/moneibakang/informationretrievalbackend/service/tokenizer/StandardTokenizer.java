package com.moneibakang.informationretrievalbackend.service.tokenizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StandardTokenizer implements Tokenizer {
    private boolean stemmingEnabled = false;
    private final Analyzer analyzer;

    public StandardTokenizer() {
        this.analyzer = new StandardAnalyzer();
    }

    @Override
    public List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        try {
            TokenStream tokenStream = analyzer.tokenStream("", text);
            tokenStream.reset();
            CharTermAttribute termAttribute = tokenStream.addAttribute(CharTermAttribute.class);
            
            while (tokenStream.incrementToken()) {
                String token = termAttribute.toString();
                if (stemmingEnabled) {
                    token = stem(token);
                }
                tokens.add(token);
            }
            
            tokenStream.end();
            tokenStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Error tokenizing text", e);
        }
        return tokens;
    }

    @Override
    public String getName() {
        return "standard";
    }

    @Override
    public boolean isStemmingEnabled() {
        return stemmingEnabled;
    }

    @Override
    public void setStemmingEnabled(boolean enabled) {
        this.stemmingEnabled = enabled;
    }

    private String stem(String token) {
        // Simple stemming by removing common suffixes
        if (token.endsWith("ing")) {
            return token.substring(0, token.length() - 3);
        } else if (token.endsWith("ed")) {
            return token.substring(0, token.length() - 2);
        } else if (token.endsWith("s") || token.endsWith("es")) {
            return token.substring(0, token.length() - 1);
        } else if (token.endsWith("er")) {
            return token.substring(0, token.length() - 2);
        } else if (token.endsWith("est")) {
            return token.substring(0, token.length() - 3);
        }
        return token;
    }
} 