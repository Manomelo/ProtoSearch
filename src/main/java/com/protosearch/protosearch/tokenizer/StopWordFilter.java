package com.protosearch.protosearch.tokenizer;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class StopWordFilter {
    private static final Set<String> STOP_WORDS = Set.of(
            // articles & determiners
            "a", "an", "the",
            // conjunctions
            "and", "or", "but", "nor", "so", "yet", "for",
            // prepositions
            "in", "on", "at", "to", "of", "up", "by", "as",
            "into", "from", "with", "about", "over", "after",
            "before", "between", "through", "during",
            // pronouns
            "i", "me", "my", "we", "us", "our", "you", "your",
            "he", "him", "his", "she", "her", "it", "its",
            "they", "them", "their",
            // common verbs
            "is", "are", "was", "were", "be", "been", "being",
            "have", "has", "had", "do", "does", "did",
            "will", "would", "could", "should", "may", "might",
            "shall", "can", "need", "dare",
            // other fillers
            "not", "no", "if", "then", "than", "that", "this",
            "these", "those", "which", "who", "whom", "what",
            "when", "where", "why", "how", "all", "both", "each",
            "more", "most", "other", "such", "also", "just", "any"
    );

    public List<String> filter(List<String> tokens){
        return tokens.stream()
                .filter(token -> !STOP_WORDS.contains(token))
                .toList();
    }

}
