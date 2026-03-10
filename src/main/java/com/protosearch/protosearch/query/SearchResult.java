package com.protosearch.protosearch.query;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchResult {

    private String title;
    private String url;
    private double score;
    private String snippet;
}
