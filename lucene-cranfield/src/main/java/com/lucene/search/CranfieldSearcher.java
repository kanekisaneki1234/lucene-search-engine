package com.lucene.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CranfieldSearcher {
    private String indexPath;
    private Analyzer analyzer;
    private IndexSearcher searcher;

    public CranfieldSearcher(String indexPath, Analyzer analyzer) throws Exception {
        this.indexPath = indexPath;
        this.analyzer = analyzer;
        
        Directory directory = FSDirectory.open(Paths.get(indexPath));
        IndexReader reader = DirectoryReader.open(directory);
        this.searcher = new IndexSearcher(reader);
    }

    /**
     * Search using a simple query string
     * @param queryString Query text
     * @param topK Number of top results to return
     * @return List of search results
     * @throws Exception
     */
    public List<SearchResult> search(String queryString, int topK) throws Exception {
        QueryParser parser = new QueryParser("content", analyzer);
        Query query = parser.parse(queryString);
        
        TopDocs results = searcher.search(query, topK);
        List<SearchResult> searchResults = new ArrayList<>();

        for (ScoreDoc scoreDoc : results.scoreDocs) {
            org.apache.lucene.document.Document doc = searcher.storedFields().document(scoreDoc.doc);
            searchResults.add(new SearchResult(
                doc.get("id"),
                doc.get("title"),
                scoreDoc.score
            ));
        }

        return searchResults;
    }

    public static class SearchResult {
        public String id;
        public String title;
        public float score;

        public SearchResult(String id, String title, float score) {
            this.id = id;
            this.title = title;
            this.score = score;
        }

        @Override
        public String toString() {
            return "ID: " + id + ", Title: " + title + ", Score: " + score;
        }
    }
}