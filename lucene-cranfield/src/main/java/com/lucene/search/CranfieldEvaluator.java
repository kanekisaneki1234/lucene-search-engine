package com.lucene.search;

import org.apache.lucene.analysis.Analyzer;
// import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.Similarity;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.Scanner;

public class CranfieldEvaluator {

    private static final String INDEX_DIR = "lucene-index";
    private static final String CRAN_DATA_DIR = "cranfield-data";
    private static final String QUERY_FILE = CRAN_DATA_DIR + "/cran.qry";
    private static final String OUTPUT_FILE = "lucene_results.txt";

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            Analyzer analyzer = new StandardAnalyzer();
            System.out.println("Using Analyzer: " + analyzer.getClass().getSimpleName());

            System.out.println("Re-indexing Cranfield collection...");
            CranfieldIndexer indexer = new CranfieldIndexer(INDEX_DIR, CRAN_DATA_DIR, analyzer);
            indexer.indexCranfieldCollection();
            System.out.println("Indexing complete.");

            System.out.println("\nChoose Scoring Model:");
            System.out.println("1. BM25Similarity (default)");
            System.out.println("2. ClassicSimilarity (Vector Space Model / TF-IDF)");
            System.out.println("3. LMDirichletSimilarity");
            int choice = 1;
            try {
                choice = scanner.nextInt();
            } catch (Exception e) {
                System.out.println("Invalid input, using default (BM25).");
            }
            scanner.nextLine();

            Similarity similarity;
            switch (choice) {
                case 2: similarity = new ClassicSimilarity(); break;
                case 3: similarity = new LMDirichletSimilarity(); break;
                default: similarity = new BM25Similarity();
            }

            CranfieldSearcher searcher = new CranfieldSearcher(INDEX_DIR, analyzer);
            searcher.setSimilarity(similarity);

            List<CranfieldQuery> queries = CranfieldQueryParser.parseQueries(QUERY_FILE);
            System.out.println("Loaded " + queries.size() + " queries.");

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE))) {
                int queryCount = 0;
                
                for (CranfieldQuery q : queries) {
                    
                    List<CranfieldSearcher.SearchResult> results = searcher.search(q.text, 1400); 

                    if (queryCount % 25 == 0) {
                        System.out.println("Processing query " + q.id + "...");
                    }

                    int rank = 1;
                    for (CranfieldSearcher.SearchResult result : results) {
                        String line = String.format("%s Q0 %s %d %.6f Lucene\n",
                                q.id.trim(),
                                result.docId.trim(), 
                                rank++,
                                result.score
                        );
                        writer.write(line);
                    }
                    queryCount++;
                }
            }

            System.out.println("✅ Evaluation results written to: " + OUTPUT_FILE);
            System.out.println("Processed " + queries.size() + " queries successfully.");
            System.out.println("\nNow run: trec_eval -m all cranfield-data/cran.rel " + OUTPUT_FILE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}