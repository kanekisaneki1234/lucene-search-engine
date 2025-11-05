package com.lucene.search;

import org.apache.lucene.analysis.Analyzer;
// import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CranfieldSearcher {

    private final IndexSearcher searcher;
    private final Analyzer analyzer;

    public CranfieldSearcher(String indexDir, Analyzer analyzer) throws IOException {
        this.analyzer = analyzer;
        this.searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(Paths.get(indexDir))));
    }

    public void setSimilarity(Similarity similarity) {
        searcher.setSimilarity(similarity);
        System.out.println("Similarity set to: " + similarity.getClass().getSimpleName());
    }

    public List<SearchResult> search(String queryString, int maxHits) {
        List<SearchResult> results = new ArrayList<>();

        try {
            Map<String, Float> boosts = new HashMap<>();
            boosts.put("title", 3.0f);
            boosts.put("content", 1.0f);
            boosts.put("author", 0.5f);

            MultiFieldQueryParser parser = new MultiFieldQueryParser(
                    new String[]{"title", "content", "author"}, analyzer, boosts
            );

            Query query = parser.parse(queryString);

            TopDocs topDocs = searcher.search(query, maxHits);

            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document doc = searcher.storedFields().document(scoreDoc.doc);
                results.add(new SearchResult(
                        doc.get("docId"),
                        doc.get("title"),
                        scoreDoc.score
                ));
            }

        } catch (ParseException | IOException e) {
            System.err.println("Error while searching '" + queryString + "': " + e.getMessage());
        }

        return results;
    }

    public static class SearchResult {
        public final String docId;
        public final String title;
        public final float score;

        public SearchResult(String docId, String title, float score) {
            this.docId = docId;
            this.title = title;
            this.score = score;
        }

        @Override
        public String toString() {
            return String.format("[DocID: %s] %s (Score: %.4f)", docId, title, score);
        }
    }
}