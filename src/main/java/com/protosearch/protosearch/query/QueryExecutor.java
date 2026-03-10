package com.protosearch.protosearch.query;

import com.protosearch.protosearch.model.IndexEntry;
import com.protosearch.protosearch.model.Page;
import com.protosearch.protosearch.repositories.IndexEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class QueryExecutor {

    private final IndexEntryRepository indexEntryRepository;
    private final QueryParser queryParser;
    private final SnippetExtractor snippetExtractor;


    @Value("${search.results-per-page:10}")
    private int resultsPerPage;

    public SearchResponse search(String rawQuery, int page){
        long startTime = System.currentTimeMillis();

        List<String> queryTerms = queryParser.parse(rawQuery);

        if(queryTerms.isEmpty()){
            return new SearchResponse(rawQuery, 0, page, resultsPerPage,
                    List.of(), System.currentTimeMillis() - startTime);
        }

        log.info("Searching for terms: " + queryTerms);

        Map<Long, Double> pageScores = new HashMap<>();

        Map<Long, IndexEntry> pageEntries = new HashMap<>();

        for(String term: queryTerms){

            List<IndexEntry> entries = indexEntryRepository
                    .findByTermOrderByTfIdfScoreDesc(term);

            for (IndexEntry entry: entries){
                Long pageId = entry.getPage().getId();
                pageScores.merge(pageId, entry.getTfIdfScore(), Double::sum );
                pageEntries.putIfAbsent(pageId, entry);
            }
        }

        List<Map.Entry<Long, Double>> ranked = pageScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double> comparingByValue().reversed())
                .toList();

        long totalResults = ranked.size();

        int fromIndex = page * resultsPerPage;
        int toIndex = Math.min(fromIndex + resultsPerPage, ranked.size());

        if(fromIndex > ranked.size()){
            return new SearchResponse(rawQuery, totalResults, page,
                    resultsPerPage, List.of(), System.currentTimeMillis() - startTime);
        }

        List<SearchResult> results = ranked.subList(fromIndex, toIndex).stream()
                .map(entry -> {
                    Long pageId = entry.getKey();
                    double score = entry.getValue();
                    IndexEntry indexEntry = pageEntries.get(pageId);
                    Page p = indexEntry.getPage();

                    String snippet = snippetExtractor.extract(p.getPlainText(), queryTerms);

                    return new SearchResult(
                            p.getTitle() != null ? p.getTitle() : p.getUrl(),
                            p.getUrl(),
                            score,
                            snippet
                    );
                })
                .toList();

        long searchTimeMs = System.currentTimeMillis() - startTime;
        log.info("Search for '{}' found {} results in {}ms", rawQuery, totalResults, searchTimeMs);

        return new SearchResponse(rawQuery, totalResults, page, resultsPerPage, results, searchTimeMs);
    }
}
