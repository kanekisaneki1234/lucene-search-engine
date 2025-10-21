package com.lucene.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import java.util.List;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        try {
            String indexPath = "./lucene-index";
            String cranfieldPath = "./cranfield-data";
            
            Analyzer analyzer = new StandardAnalyzer();
            
            // Step 1: Create index
            System.out.println("=== Creating Index ===");
            CranfieldIndexer indexer = new CranfieldIndexer(indexPath, cranfieldPath, analyzer);
            indexer.indexCranfieldCollection();
            
            // Step 2: Interactive search
            System.out.println("\n=== Search Engine Ready ===");
            CranfieldSearcher searcher = new CranfieldSearcher(indexPath, analyzer);
            
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("\nEnter search query (or 'exit' to quit): ");
                String query = scanner.nextLine();
                
                if (query.equalsIgnoreCase("exit")) break;
                
                List<CranfieldSearcher.SearchResult> results = searcher.search(query, 10);
                System.out.println("\nFound " + results.size() + " results:");
                for (CranfieldSearcher.SearchResult result : results) {
                    System.out.println(result);
                }
            }
            scanner.close();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}