package com.moneibakang.informationretrievalbackend.dto;
/*
 * @Author: Monei Bakang
 * @Date: 16 March 2025
 * @Time: 07:21 hours
 */

import com.moneibakang.informationretrievalbackend.model.Document;
import lombok.Data;

@Data
public class DocumentDTO {
    private String id;
    private String title;
    private String content;
    private String author;
    private String dataset;
    private String collection;
    private long timestamp;
    private long indexingTime;
    private int tokenCount;
    private boolean stemmed;

    // Default constructor
    public DocumentDTO() {
    }

    // Conversion constructor from Document model
    public DocumentDTO(Document document) {
        this.id = document.getId();
        this.title = document.getTitle();
        this.content = document.getContent();
        this.collection = document.getCollection();
        this.author = document.getAuthor();
        this.timestamp = document.getTimestamp();
    }

    // Convert DTO to Document model
    public Document toDocument() {
        Document doc = new Document();
        doc.setId(this.id);
        doc.setTitle(this.title);
        doc.setContent(this.content);
        doc.setCollection(this.collection);
        doc.setAuthor(this.author);
        doc.setTimestamp(this.timestamp);
        return doc;
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

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}