package com.moneibakang.informationretrievalbackend.config;

import org.springframework.stereotype.Component;

/**
 * Analysis used when writing the primary Lucene index under {@code index/}.
 * Must match the tokenizer/stemmer you use when searching that index via {@code QueryParser}.
 * After changing values, rebuild the index (re-import or {@code POST /index/build}).
 */
@Component
public class IndexAnalysisSettings {

    private volatile String tokenizerType = "standard";
    private volatile boolean stemming = false;

    public String getTokenizerType() {
        return tokenizerType;
    }

    public void setTokenizerType(String tokenizerType) {
        if (tokenizerType == null || tokenizerType.isBlank()) {
            this.tokenizerType = "standard";
        } else {
            this.tokenizerType = tokenizerType.trim().toLowerCase();
        }
    }

    public boolean isStemming() {
        return stemming;
    }

    public void setStemming(boolean stemming) {
        this.stemming = stemming;
    }
}
