package com.lucene.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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

    /**
     * Indexes the Cranfield Collection
     * @throws IOException
     */
    public void indexCranfieldCollection() throws IOException {
        Directory directory = FSDirectory.open(Paths.get(indexPath));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        IndexWriter writer = new IndexWriter(directory, config);
        
        try {
            List<CranfieldDocument> documents = parseCranfieldCollection();
            
            for (CranfieldDocument doc : documents) {
                Document luceneDoc = new Document();
                luceneDoc.add(new StringField("id", doc.getId(), Field.Store.YES));
                luceneDoc.add(new TextField("title", doc.getTitle(), Field.Store.YES));
                luceneDoc.add(new TextField("author", doc.getAuthor(), Field.Store.YES));
                luceneDoc.add(new TextField("content", doc.getContent(), Field.Store.YES));
                
                writer.addDocument(luceneDoc);
            }
            
            System.out.println("Indexed " + documents.size() + " documents");
        } finally {
            writer.close();
        }
    }

    /**
     * Parses the Cranfield Collection SGML format
     * @return List of CranfieldDocument objects
     * @throws IOException
     */
    private List<CranfieldDocument> parseCranfieldCollection() throws IOException {
        List<CranfieldDocument> documents = new ArrayList<>();
        File file = new File(cranfieldPath + "/cran.all.1400");
        
        if (!file.exists()) {
            throw new IOException("Cranfield collection file not found: " + file.getAbsolutePath());
        }

        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder currentContent = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            currentContent.append(line).append("\n");
        }
        reader.close();

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

    /**
     * Parses individual Cranfield entry
     */
    private CranfieldDocument parseCranfieldEntry(String entry) {
        try {
            String id = "";
            String title = "";
            String author = "";
            String content = "";

            String[] lines = entry.split("\n");
            if (lines.length == 0) return null;

            id = lines[0].trim();
            
            StringBuilder currentField = new StringBuilder();
            String currentTag = "";

            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                
                if (line.startsWith(".T")) {
                    currentTag = "title";
                } else if (line.startsWith(".A")) {
                    currentTag = "author";
                } else if (line.startsWith(".W")) {
                    currentTag = "content";
                } else if (line.startsWith(".B") || line.startsWith(".X")) {
                    currentTag = "";
                } else {
                    if (!currentTag.isEmpty()) {
                        currentField.append(line).append(" ");
                    }
                }

                if (currentTag.equals("title") && (line.startsWith(".A") || i == lines.length - 1)) {
                    title = currentField.toString().trim();
                    currentField = new StringBuilder();
                } else if (currentTag.equals("author") && (line.startsWith(".W") || i == lines.length - 1)) {
                    author = currentField.toString().trim();
                    currentField = new StringBuilder();
                } else if (currentTag.equals("content") && (line.startsWith(".B") || line.startsWith(".X") || i == lines.length - 1)) {
                    content = currentField.toString().trim();
                }
            }

            return new CranfieldDocument(id, title, author, content);
        } catch (Exception e) {
            return null;
        }
    }
}