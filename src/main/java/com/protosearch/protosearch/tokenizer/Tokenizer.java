package com.protosearch.protosearch.tokenizer;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class Tokenizer {

    public List<String> tokenize(String cleanedText){
        if(cleanedText == null || cleanedText.isBlank()){
            return List.of();
        }

        return Arrays.stream(cleanedText.split("\\s+"))
                .filter(token -> !token.isBlank())
                .filter(token -> token.length() > 1)
                .filter(token -> !token.matches("\\d+"))
                .toList();
    }
}
