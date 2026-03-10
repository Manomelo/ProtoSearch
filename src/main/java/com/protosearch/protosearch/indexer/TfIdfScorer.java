package com.protosearch.protosearch.indexer;

import com.protosearch.protosearch.enums.CrawlStatus;
import com.protosearch.protosearch.model.Page;
import com.protosearch.protosearch.repositories.PageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TfIdfScorer {
    private final PageRepository pageRepository;

    public double score(int termFrequency, int totalTermsInDoc, int documentFrequency){
        if(totalTermsInDoc == 0) return 0.0;

        double tf = (double) termFrequency / totalTermsInDoc;

        long totalDocuments = pageRepository.countByStatus(CrawlStatus.CRAWLED);

        double idf = Math.log((double) totalDocuments / (1 + documentFrequency));

        return idf * tf;
    }
}
