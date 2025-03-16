package com.moneibakang.informationretrievalbackend.util;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class CustomTokenizer extends Tokenizer {
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final StringBuilder buffer = new StringBuilder();
    private int tokenStart = 0, tokenEnd = 0;
    
    @Override
    public boolean incrementToken() throws IOException {
        clearAttributes();
        buffer.setLength(0);
        
        int chr;
        boolean hasToken = false;
        
        while ((chr = input.read()) != -1) {
            char ch = (char) chr;
            
            // Custom tokenization rules
            if (Character.isLetterOrDigit(ch) || ch == '-' || ch == '_') {
                buffer.append(Character.toLowerCase(ch));
                if (!hasToken) {
                    tokenStart = tokenEnd;
                    hasToken = true;
                }
            } else if (hasToken) {
                break;
            }
            tokenEnd++;
        }
        
        if (!hasToken && chr == -1) {
            return false;
        }
        
        termAtt.setEmpty().append(buffer);
        return true;
    }
    
    @Override
    public void reset() throws IOException {
        super.reset();
        tokenStart = tokenEnd = 0;
    }
} 