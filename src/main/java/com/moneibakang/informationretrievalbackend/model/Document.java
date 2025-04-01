package com.moneibakang.informationretrievalbackend.model;
/*
 * @Author: Monei Bakang
 * @Date: 16 March 2025
 * @Time: 07:21 hours
 */

import java.io.*;
import lombok.Data;

@Data
public class Document implements Serializable {
    private String id;
    private String title;
    private String content;
    private String author;
    private String dataset; // CISI or MEDLINE
    private String collection; // Collection name
    private long timestamp;
    private long indexingTime;
    private int tokenCount;
    private boolean stemmed;

    // Default constructor
    public Document() {
        this.timestamp = System.currentTimeMillis();
    }

    // Parameterized constructor
    public Document(String id, String title, String content, String author, String collection) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.collection = collection;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getIndexingTime() {
        return indexingTime;
    }

    public void setIndexingTime(long indexingTime) {
        this.indexingTime = indexingTime;
    }

    @Override
    public String toString() {
        return "Document{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                '}';
    }
}