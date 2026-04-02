package com.moneibakang.informationretrievalbackend.service;

/*
 * @Author: Monei Bakang
 * @Date: 16 March 2026
 * @Time: 07:23 hours
 */

import java.io.*;
import java.util.*;

public interface IndexService {
    void recreateIndex() throws IOException;

    Map<String, Object> getIndexStatistics() throws IOException;

    void importCISICollection(String filePath) throws IOException;

    void importPubMedCollection(String filePath) throws IOException;

    Map<String, Object> getPerformanceMetrics(String queryId, String relevanceFilePath) throws IOException;

    List<String> getConsoleLogs(
            // Path: src/main/java/com/moneibakang/informationretrievalbackend/service/IndexService.java
    );
}