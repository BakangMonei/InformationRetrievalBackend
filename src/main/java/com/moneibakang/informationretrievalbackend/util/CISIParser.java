package com.moneibakang.informationretrievalbackend.util;

import com.moneibakang.informationretrievalbackend.model.Document;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class CISIParser {

    private enum Section {
        NONE, TITLE, AUTHOR, CONTENT, SKIP_UNTIL_NEXT_DOC
    }

    public List<Document> parseDocuments(String filePath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Path.of(filePath), StandardCharsets.UTF_8)) {
            return parseDocuments(reader);
        }
    }

    public List<Document> parseDocuments(InputStream inputStream) throws IOException {
        return parseDocuments(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)));
    }

    public List<Document> parseDocuments(Reader reader) throws IOException {
        if (reader instanceof BufferedReader br) {
            return parseDocuments(br);
        }
        return parseDocuments(new BufferedReader(reader));
    }

    public List<Document> parseDocuments(BufferedReader reader) throws IOException {
        List<Document> documents = new ArrayList<>();
        Document current = null;
        StringBuilder title = new StringBuilder();
        StringBuilder author = new StringBuilder();
        StringBuilder body = new StringBuilder();
        Section section = Section.NONE;

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith(".I")) {
                if (current != null) {
                    finalizeCisiDoc(current, title, author, body);
                    documents.add(current);
                }
                String idPart = line.length() > 2 ? line.substring(2).trim() : "";
                String rawId = idPart.split("\\s+")[0];
                if (rawId.isEmpty()) {
                    current = null;
                    continue;
                }
                current = new Document();
                current.setId("CISI-" + rawId);
                current.setDataset("CISI");
                current.setCollection("CISI");
                title.setLength(0);
                author.setLength(0);
                body.setLength(0);
                section = Section.NONE;
            } else if (current == null) {
                continue;
            } else if (line.startsWith(".T")) {
                section = Section.TITLE;
                appendTagLine(title, line, 2);
            } else if (line.startsWith(".A")) {
                section = Section.AUTHOR;
                appendTagLine(author, line, 2);
            } else if (line.startsWith(".W")) {
                section = Section.CONTENT;
                appendTagLine(body, line, 2);
            } else if (line.startsWith(".X") || line.startsWith(".B")) {
                section = Section.SKIP_UNTIL_NEXT_DOC;
            } else if (section == Section.SKIP_UNTIL_NEXT_DOC) {
                // ignore citation / bibliography lines until the next .I
            } else {
                switch (section) {
                    case TITLE -> title.append(line).append(' ');
                    case AUTHOR -> author.append(line).append(' ');
                    case CONTENT -> body.append(line).append(' ');
                    default -> {
                    }
                }
            }
        }
        if (current != null) {
            finalizeCisiDoc(current, title, author, body);
            documents.add(current);
        }
        return documents;
    }

    private static void appendTagLine(StringBuilder target, String line, int tagLen) {
        if (line.length() > tagLen) {
            String rest = line.substring(tagLen).trim();
            if (!rest.isEmpty()) {
                target.append(rest).append(' ');
            }
        }
    }

    private static void finalizeCisiDoc(Document doc, StringBuilder title, StringBuilder author, StringBuilder body) {
        String t = title.toString().trim();
        String c = body.toString().trim();
        doc.setTitle(t.isEmpty() ? "(no title)" : t);
        doc.setAuthor(author.toString().trim());
        doc.setContent(c.isEmpty() ? "(no content)" : c);
        doc.setTimestamp(System.currentTimeMillis());
    }

    public List<String> parseQueries(String filePath) throws IOException {
        List<String> queries = new ArrayList<>();
        StringBuilder queryContent = new StringBuilder();

        try (BufferedReader reader = Files.newBufferedReader(Path.of(filePath), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(".I")) {
                    if (queryContent.length() > 0) {
                        queries.add(queryContent.toString().trim());
                        queryContent = new StringBuilder();
                    }
                } else if (line.startsWith(".W")) {
                    String rest = line.length() > 2 ? line.substring(2).trim() : "";
                    if (!rest.isEmpty()) {
                        queryContent.append(rest).append(' ');
                    }
                } else if (!line.isEmpty()) {
                    queryContent.append(line).append(' ');
                }
            }
            if (queryContent.length() > 0) {
                queries.add(queryContent.toString().trim());
            }
        }

        return queries;
    }

    public Map<String, Set<String>> parseRelevanceJudgments(String filePath) throws IOException {
        Map<String, Set<String>> relevanceMap = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(Path.of(filePath), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 2) {
                    String queryId = parts[0];
                    String docId = parts[1];

                    relevanceMap.computeIfAbsent(queryId, k -> new HashSet<>()).add(docId);
                }
            }
        }

        return relevanceMap;
    }
}
