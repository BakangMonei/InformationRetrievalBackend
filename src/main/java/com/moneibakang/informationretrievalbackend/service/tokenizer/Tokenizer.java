package com.moneibakang.informationretrievalbackend.service.tokenizer;

import java.util.List;

public interface Tokenizer {
    List<String> tokenize(String text);
    String getName();
    boolean isStemmingEnabled();
    void setStemmingEnabled(boolean enabled);
} 