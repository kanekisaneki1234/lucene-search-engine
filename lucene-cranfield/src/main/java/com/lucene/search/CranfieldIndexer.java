package com.lucene.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CranfieldIndexer {
    private String indexPath;
    private String cranfieldPath;
    private Analyzer analyzer;

    public CranfieldIndexer(String indexPath, String cranfieldPath, Analyzer analyzer) {
        this.indexPath = indexPath;
        this.cranfieldPath = cranfieldPath;
        this.analyzer = analyzer;
    }

    public void indexCranfieldCollection() throws IOException {
        Directory directory = FSDirectory.open(Paths.get(indexPath));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        try (IndexWriter writer = new IndexWriter(directory, config)) {
            List<CranfieldDocument> documents = parseCranfieldCollection();
            
            for (CranfieldDocument doc : documents) {
                Document luceneDoc = new Document();
                luceneDoc.add(new StringField("docId", doc.getId(), Field.Store.YES)); 
                luceneDoc.add(new TextField("title", doc.getTitle(), Field.Store.YES));
                luceneDoc.add(new TextField("author", doc.getAuthor(), Field.Store.YES));
                luceneDoc.add(new TextField("content", doc.getContent(), Field.Store.YES));
                
                writer.addDocument(luceneDoc);
            }
            
            System.out.println("Indexed " + documents.size() + " documents");
        }
    }

    /**
     * Parses the Cranfield Collection SGML format
     */
    private List<CranfieldDocument> parseCranfieldCollection() throws IOException {
        List<CranfieldDocument> documents = new ArrayList<>();
        File file = new File(cranfieldPath + "/cran.all.1400");
        
        if (!file.exists()) {
            throw new IOException("Cranfield collection file not found: " + file.getAbsolutePath());
        }

        StringBuilder currentContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                currentContent.append(line).append("\n");
            }
        }

        String content = currentContent.toString();
        String[] entries = content.split("\\.I ");

        for (int i = 1; i < entries.length; i++) {
            CranfieldDocument doc = parseCranfieldEntry(entries[i]);
            if (doc != null) {
                documents.add(doc);
            }
        }

        return documents;
    }

    private CranfieldDocument parseCranfieldEntry(String entry) {
        String id = "";
        StringBuilder title = new StringBuilder();
        StringBuilder author = new StringBuilder();
        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new StringReader(entry))) {
            id = reader.readLine().trim();
            
            String line;
            char currentTag = ' ';

            while ((line = reader.readLine()) != null) {
                if (line.startsWith(".T")) {
                    currentTag = 'T';
                } else if (line.startsWith(".A")) {
                    currentTag = 'A';
                } else if (line.startsWith(".W")) {
                    currentTag = 'W';
                } else if (line.startsWith(".B") || line.startsWith(".X")) {
                    currentTag = ' ';
                } else {
                    switch (currentTag) {
                        case 'T':
                            title.append(line).append(" ");
                            break;
                        case 'A':
                            author.append(line).append(" ");
                            break;
                        case 'W':
                            content.append(line).append(" ");
                            break;
                    }
                }
            }
            
            return new CranfieldDocument(
                id, 
                title.toString().trim(), 
                author.toString().trim(), 
                content.toString().trim()
            );

        } catch (Exception e) {
            System.err.println("Error parsing entry starting with ID: " + id);
            e.printStackTrace();
            return null;
        }
    }
}