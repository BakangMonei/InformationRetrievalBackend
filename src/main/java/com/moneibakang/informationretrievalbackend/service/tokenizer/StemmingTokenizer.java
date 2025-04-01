package com.moneibakang.informationretrievalbackend.service.tokenizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class StemmingTokenizer implements Tokenizer {
    private final Analyzer analyzer;

    public StemmingTokenizer() {
        this.analyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                StandardTokenizer tokenizer = new StandardTokenizer();
                TokenStream stream = new PorterStemFilter(tokenizer);
                return new TokenStreamComponents(tokenizer, stream);
            }
        };
    }

    @Override
    public List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        try (TokenStream stream = analyzer.tokenStream("", new StringReader(text))) {
            CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
            stream.reset();
            
            while (stream.incrementToken()) {
                String term = termAtt.toString().toLowerCase();
                if (!term.isEmpty() && term.length() > 1) {
                    tokens.add(term);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error tokenizing text", e);
        }
        return tokens;
    }

    @Override
    public String getName() {
        return "StemmingTokenizer";
    }

    @Override
    public boolean isStemmingEnabled() {
        return true;
    }

    @Override
    public void setStemmingEnabled(boolean enabled) {

    }
} 