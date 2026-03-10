package com.protosearch.protosearch.controller;

import com.protosearch.protosearch.query.QueryExecutor;
import com.protosearch.protosearch.query.SearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final QueryExecutor queryExecutor;

    @GetMapping
    public ResponseEntity<SearchResponse> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page
    ){
        if(q == null || q.isBlank()){
            return ResponseEntity.badRequest().build();
        }

        SearchResponse response = queryExecutor.search(q, page);
        return ResponseEntity.ok(response);
    }

}
