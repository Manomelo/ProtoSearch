package com.protosearch.protosearch.tokenizer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TokenizationPipeline {
    private final TextCleaner cleaner;
    private final Tokenizer tokenizer;
    private final StopWordFilter stopWordFilter;
    private final PorterStemmer stemmer;


    public List<String> process(String rawText){
        String cleaned = cleaner.clean(rawText);
        List<String> tokens = tokenizer.tokenize(cleaned);
        List<String> filtered = stopWordFilter.filter(tokens);
        return stemmer.stemAll(filtered);
    }
}
