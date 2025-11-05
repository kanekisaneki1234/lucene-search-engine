package com.lucene.search;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CranfieldQueryParser {

    public static List<CranfieldQuery> parseQueries(String queryFile) throws IOException {
        List<CranfieldQuery> queries = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(queryFile))) {
            String line;
            String currentId = "";
            StringBuilder currentQuery = new StringBuilder();
            boolean inQuery = false;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith(".I")) {
                    if (inQuery && !currentId.isEmpty()) {
                        String queryText = currentQuery.toString().trim()
                                .replaceAll("\\s+", " ")
                                .replaceAll("[?*]", " ");
                        queries.add(new CranfieldQuery(currentId, queryText));
                    }
                    currentId = line.substring(2).trim();
                    currentQuery = new StringBuilder();
                    inQuery = false;
                } else if (line.startsWith(".W")) {
                    inQuery = true;
                } else if (inQuery) {
                    currentQuery.append(line).append(" ");
                }
            }
            if (inQuery && !currentId.isEmpty()) {
                String queryText = currentQuery.toString().trim()
                        .replaceAll("\\s+", " ")
                        .replaceAll("[?*]", " ");
                queries.add(new CranfieldQuery(currentId, queryText));
            }
        }
        
        return queries;
    }
}