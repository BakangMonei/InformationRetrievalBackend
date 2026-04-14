package com.moneibakang.informationretrievalbackend.service;

import com.moneibakang.informationretrievalbackend.model.Document;
import com.moneibakang.informationretrievalbackend.util.CISIParser;
import com.moneibakang.informationretrievalbackend.util.PubMedCorpusReader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FileProcessingService {

    private final CISIParser cisiParser;
    private final PubMedCorpusReader pubMedCorpusReader;

    public FileProcessingService(CISIParser cisiParser, PubMedCorpusReader pubMedCorpusReader) {
        this.cisiParser = cisiParser;
        this.pubMedCorpusReader = pubMedCorpusReader;
    }

    public List<Document> processCisiFile(MultipartFile file) throws IOException {
        return cisiParser.parseDocuments(file.getInputStream());
    }

    public List<Document> processPubMedXmlFile(MultipartFile file) throws IOException {
        return pubMedCorpusReader.readCorpus(
                file.getInputStream(),
                file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
    }

    public List<String> processQueryFile(MultipartFile file) throws IOException {
        List<String> queries = new ArrayList<>();
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);

        String[] querySections = content.split("\\.I ");

        for (String section : querySections) {
            if (section.trim().isEmpty()) {
                continue;
            }

            Pattern queryPattern = Pattern.compile("\\.W\\s*(.*?)(?=\\.I|$)", Pattern.DOTALL);
            Matcher queryMatcher = queryPattern.matcher(section);
            if (queryMatcher.find()) {
                queries.add(queryMatcher.group(1).trim());
            }
        }

        return queries;
    }

    /**
     * Parses CISI-style query files and preserves their query ids.
     * If ids are missing/unparseable, it falls back to 1..N ordering.
     */
    public java.util.Map<String, String> processQueryFileWithIds(MultipartFile file) throws IOException {
        java.util.Map<String, String> out = new java.util.LinkedHashMap<>();
        String content = new String(file.getBytes(), StandardCharsets.UTF_8);
        String[] querySections = content.split("\\.I ");
        int fallbackId = 1;
        for (String section : querySections) {
            String s = section.trim();
            if (s.isEmpty()) {
                continue;
            }
            String id = null;
            int firstLineEnd = s.indexOf('\n');
            String firstLine = firstLineEnd >= 0 ? s.substring(0, firstLineEnd).trim() : s;
            if (!firstLine.isBlank()) {
                String digits = firstLine.replaceAll("[^0-9]", "").trim();
                if (!digits.isBlank()) {
                    id = digits;
                }
            }
            if (id == null) {
                id = String.valueOf(fallbackId++);
            }

            Pattern queryPattern = Pattern.compile("\\.W\\s*(.*?)(?=\\.I|$)", Pattern.DOTALL);
            Matcher queryMatcher = queryPattern.matcher(section);
            if (queryMatcher.find()) {
                String text = queryMatcher.group(1).trim();
                if (!text.isBlank()) {
                    out.put(id, text);
                }
            }
        }
        return out;
    }

    public List<RelevanceJudgment> processRelevanceFile(MultipartFile file) throws IOException {
        List<RelevanceJudgment> judgments = new ArrayList<>();
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            StringBuilder lineBuf = new StringBuilder();
            int ch;
            while ((ch = reader.read()) != -1) {
                if (ch == '\n') {
                    addRelevanceLine(judgments, lineBuf.toString());
                    lineBuf.setLength(0);
                } else if (ch != '\r') {
                    lineBuf.append((char) ch);
                }
            }
            addRelevanceLine(judgments, lineBuf.toString());
        }

        return judgments;
    }

    private static void addRelevanceLine(List<RelevanceJudgment> judgments, String line) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length >= 3) {
            RelevanceJudgment judgment = new RelevanceJudgment();
            judgment.setQueryId(parts[0]);
            judgment.setDocumentId(parts[1]);
            judgment.setRelevance(Integer.parseInt(parts[2]));
            judgments.add(judgment);
        }
    }

    public static class RelevanceJudgment {
        private String queryId;
        private String documentId;
        private int relevance;

        public String getQueryId() {
            return queryId;
        }

        public void setQueryId(String queryId) {
            this.queryId = queryId;
        }

        public String getDocumentId() {
            return documentId;
        }

        public void setDocumentId(String documentId) {
            this.documentId = documentId;
        }

        public int getRelevance() {
            return relevance;
        }

        public void setRelevance(int relevance) {
            this.relevance = relevance;
        }
    }
}
