package com.moneibakang.informationretrievalbackend.config;

/*
 * @Author: Monei Bakang
 * @Date: 16 March 2025
 * @Time: 07:21 hours
 */

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class LuceneConfig {

    @Bean
    public Directory indexDirectory() throws IOException {
        // Create a directory where the index will be stored
        // In production, you might want to use a configurable location
        Path indexPath = Paths.get(System.getProperty("java.io.tmpdir"), "lucene-index");
        return FSDirectory.open(indexPath);
    }
}