package com.protosearch.protosearch.query;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SnippetExtractor {

    private static final int SNIPPET_LENGTH = 200;

    public String extract(String plainText, List<String> queryTerms){
        if(plainText == null || plainText.isBlank()) return "";

        String lowerText = plainText.toLowerCase();

        int bestPosition = -1;

        for(String term: queryTerms){
            int pos = lowerText.indexOf(term);
            if(pos != -1 && (bestPosition == -1 || pos < bestPosition)){
                bestPosition = pos;
            }
        }

        if(bestPosition == -1){
            return truncate(plainText, SNIPPET_LENGTH);
        }

        int start = Math.max(0, bestPosition - 50);
        int end = Math.min(plainText.length(), start + SNIPPET_LENGTH);
        String snippet = plainText.substring(start, end);

        if(start > 0) snippet = "..." + snippet;
        if(end < plainText.length()) snippet = snippet + "...";

        return snippet;
    }

    private String truncate(String text, int maxLength){
        if(text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
}
