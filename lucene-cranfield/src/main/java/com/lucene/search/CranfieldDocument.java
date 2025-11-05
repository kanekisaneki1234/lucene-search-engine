package com.lucene.search;

public class CranfieldDocument {
    private final String id;
    private final String title;
    private final String author;
    private final String content;

    public CranfieldDocument(String id, String title, String author, String content) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.content = content;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getContent() { return content; }
}