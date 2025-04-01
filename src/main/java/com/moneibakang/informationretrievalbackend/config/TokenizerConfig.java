package com.moneibakang.informationretrievalbackend.config;

import com.moneibakang.informationretrievalbackend.service.tokenizer.SimpleTokenizer;
import com.moneibakang.informationretrievalbackend.service.tokenizer.StemmingTokenizer;
import com.moneibakang.informationretrievalbackend.service.tokenizer.Tokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class TokenizerConfig {
    
    @Bean(name = "simpleTokenizer")
    @Primary
    public Tokenizer simpleTokenizer() {
        return new SimpleTokenizer();
    }
    
    @Bean(name = "stemmingTokenizer")
    public Tokenizer stemmingTokenizer() {
        return new StemmingTokenizer();
    }
} 