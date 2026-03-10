package com.protosearch.protosearch.tokenizer;


import org.springframework.stereotype.Component;

@Component
public class TextCleaner {

    public String clean(String text){
        return text
                .toLowerCase()
                .replaceAll("<[^>]+>", "")          // Remover tags html
                .replaceAll("https?://\\S+", "")     // Remover URLs
                .replaceAll("[^a-z0-9\\s]", " ")     // Remover pontuacao / simbolos
                .replaceAll("\\s+", " ")            // Remover whitespaces
                .trim();
    }
}
