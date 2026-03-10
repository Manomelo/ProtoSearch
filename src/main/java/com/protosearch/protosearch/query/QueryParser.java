package com.protosearch.protosearch.query;

import com.protosearch.protosearch.tokenizer.TokenizationPipeline;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QueryParser {

    private final TokenizationPipeline tokenizationPipeline;
    public List<String> parse(String rawQuery){
        if(rawQuery == null || rawQuery.isBlank()){
            return List.of();
        }

        return tokenizationPipeline.process(rawQuery);
    }
}
