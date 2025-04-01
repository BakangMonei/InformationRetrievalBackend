package com.moneibakang.informationretrievalbackend.service.tokenizer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CustomTokenizer implements Tokenizer {
    private boolean stemmingEnabled = false;

    @Override
    public List<String> tokenize(String text) {
        // Convert to lowercase and split by non-alphanumeric characters
        List<String> tokens = Arrays.stream(text.toLowerCase()
                .split("[^a-z0-9]+"))
                .filter(token -> !token.isEmpty())
                .collect(Collectors.toList());

        if (stemmingEnabled) {
            tokens = tokens.stream()
                    .map(this::stem)
                    .collect(Collectors.toList());
        }

        return tokens;
    }

    @Override
    public String getName() {
        return "custom";
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