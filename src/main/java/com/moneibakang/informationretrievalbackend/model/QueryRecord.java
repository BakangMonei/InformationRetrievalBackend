package com.moneibakang.informationretrievalbackend.model;

public class QueryRecord {
    private String id;
    private String queryText;
    private String model;
    private long executedAt;
    private long latencyMs;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public long getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(long executedAt) {
        this.executedAt = executedAt;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(long latencyMs) {
        this.latencyMs = latencyMs;
    }
}
