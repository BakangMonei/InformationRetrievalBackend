package com.moneibakang.informationretrievalbackend.model;

public class EvaluationMetrics {
    private double precision;
    private double recall;
    private double f1Score;
    private double map;

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public double getRecall() {
        return recall;
    }

    public void setRecall(double recall) {
        this.recall = recall;
    }

    public double getF1Score() {
        return f1Score;
    }

    public void setF1Score(double f1Score) {
        this.f1Score = f1Score;
    }

    public double getMap() {
        return map;
    }

    public void setMap(double map) {
        this.map = map;
    }
}
