package com.moneibakang.informationretrievalbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import com.moneibakang.informationretrievalbackend.service.tokenizer.Tokenizer;
import com.moneibakang.informationretrievalbackend.service.tokenizer.StandardTokenizer;
import com.moneibakang.informationretrievalbackend.service.tokenizer.CustomTokenizer;
import com.moneibakang.informationretrievalbackend.service.ranking.RankingAlgorithm;
import com.moneibakang.informationretrievalbackend.service.ranking.TFIDFRanking;
import com.moneibakang.informationretrievalbackend.service.ranking.TFRanking;

@Configuration
public class IRConfig {

    @Bean
    public Tokenizer standardTokenizer() {
        return new StandardTokenizer();
    }

    @Bean
    public Tokenizer customTokenizer() {
        return new CustomTokenizer();
    }

    @Bean
    @Primary
    public RankingAlgorithm tfidfRanking(StandardTokenizer tokenizer) {
        return new TFIDFRanking(tokenizer);
    }

    @Bean
    public RankingAlgorithm tfRanking(StandardTokenizer tokenizer) {
        return new TFRanking(tokenizer);
    }
} 