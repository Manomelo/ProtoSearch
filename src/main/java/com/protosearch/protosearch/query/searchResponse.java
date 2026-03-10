package com.protosearch.protosearch.query;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class searchResponse {
    private String query;
    private long totalResults;
    private int page;
    private int pageSize;
    private List<SearchResult> results;
    private long searchTimeMs;
}
