package com.moneibakang.informationretrievalbackend.config;

/*
 * @Author: Monei Bakang
 * @Date: 16 March 2026
 * @Time: 07:21 hours
 */

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.nio.file.*;

@Configuration
public class LuceneConfig {

    @Bean
    public Directory indexDirectory() throws IOException {
        Path indexPath = Paths.get(System.getProperty("java.io.tmpdir"), "lucene-index");
        return FSDirectory.open(indexPath);
    }
}