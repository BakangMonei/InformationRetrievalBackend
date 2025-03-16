package com.moneibakang.informationretrievalbackend.model;
/*
 * @Author: Monei Bakang
 * @Date: 16 March 2025
 * @Time: 07:21 hours
 */

import java.io.*;

public class Document implements Serializable {
    private String id, title, content, collection, author;
    private long timestamp;

    // Default constructor
    public Document() {
    }

    // Parameterized constructor
    public Document(String id, String title, String content, String collection, String author) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.collection = collection;
        this.author = author;
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

    @Override
    public String toString() {
        return "Document{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", collection='" + collection + '\'' +
                ", author='" + author + '\'' +
                '}';
    }
}