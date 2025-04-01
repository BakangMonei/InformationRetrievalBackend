package com.moneibakang.informationretrievalbackend.service.tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SimpleTokenizer implements Tokenizer {
    private boolean stemmingEnabled = false;
    private static final Pattern WORD_PATTERN = Pattern.compile("\\b[a-zA-Z]+\\b");

    @Override
    public List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        var matcher = WORD_PATTERN.matcher(text.toLowerCase());
        
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        
        return tokens;
    }

    @Override
    public String getName() {
        return "simple";
    }

    @Override
    public boolean isStemmingEnabled() {
        return stemmingEnabled;
    }

    @Override
    public void setStemmingEnabled(boolean enabled) {
        this.stemmingEnabled = enabled;
    }
} 