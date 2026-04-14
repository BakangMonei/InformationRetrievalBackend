package com.moneibakang.informationretrievalbackend.service;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory evaluation inputs (queries + qrels) so the backend can run end-to-end
 * without requiring a database for coursework.
 */
@Service
public class EvaluationDataStore {

    private final Map<String, Map<String, String>> queriesByDataset = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Set<String>>> relevantByDataset = new ConcurrentHashMap<>();

    public void putQueries(String dataset, Map<String, String> queries) {
        String ds = norm(dataset);
        queriesByDataset.put(ds, new LinkedHashMap<>(queries));
    }

    public void putRelevance(String dataset, java.util.List<FileProcessingService.RelevanceJudgment> judgments) {
        String ds = norm(dataset);
        Map<String, Set<String>> qrels = new HashMap<>();
        for (FileProcessingService.RelevanceJudgment j : judgments) {
            if (j.getQueryId() == null || j.getQueryId().isBlank()) {
                continue;
            }
            if (j.getDocumentId() == null || j.getDocumentId().isBlank()) {
                continue;
            }
            if (j.getRelevance() <= 0) {
                continue;
            }
            qrels.computeIfAbsent(j.getQueryId().trim(), k -> ConcurrentHashMap.newKeySet())
                    .add(j.getDocumentId().trim());
        }
        relevantByDataset.put(ds, qrels);
    }

    public Map<String, String> getQueries(String dataset) {
        return Collections.unmodifiableMap(queriesByDataset.getOrDefault(norm(dataset), Map.of()));
    }

    public Map<String, Set<String>> getRelevance(String dataset) {
        Map<String, Set<String>> qrels = relevantByDataset.get(norm(dataset));
        if (qrels == null) {
            return Map.of();
        }
        return qrels.entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        e -> Set.copyOf(e.getValue())
                ));
    }

    public Map<String, Object> status() {
        Map<String, Object> out = new HashMap<>();
        for (String ds : queriesByDataset.keySet()) {
            out.put(ds + "_queries", queriesByDataset.get(ds).size());
        }
        for (String ds : relevantByDataset.keySet()) {
            out.put(ds + "_qrels", relevantByDataset.get(ds).values().stream().mapToInt(Set::size).sum());
        }
        return out;
    }

    public void reset() {
        queriesByDataset.clear();
        relevantByDataset.clear();
    }

    private static String norm(String dataset) {
        if (dataset == null || dataset.isBlank()) {
            return "CISI";
        }
        return dataset.trim().toUpperCase();
    }
}

