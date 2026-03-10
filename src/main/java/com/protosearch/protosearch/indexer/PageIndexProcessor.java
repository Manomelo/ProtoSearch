package com.protosearch.protosearch.indexer;

import com.protosearch.protosearch.model.IndexEntry;
import com.protosearch.protosearch.model.Page;
import com.protosearch.protosearch.repositories.IndexEntryRepository;
import com.protosearch.protosearch.tokenizer.TokenizationPipeline;
import jakarta.persistence.Index;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PageIndexProcessor implements ItemProcessor<Page, List<IndexEntry>> {
    private final TokenizationPipeline tokenizationPipeline;
    private final TfIdfScorer scorer;
    private final IndexEntryRepository indexEntryRepository;

    @Override
    public List<IndexEntry> process(Page page){
        List<String> terms = tokenizationPipeline.process(page.getPlainText());

        if (terms.isEmpty()) return Collections.emptyList();

        int totalTerms = terms.size();

        Map<String, List<Integer>> termPositions = new LinkedHashMap<>();
        for(int i = 0; i < terms.size(); i++){
            termPositions
                    .computeIfAbsent(terms.get(i), k -> new ArrayList<>())
                    .add(i);
        }

        List<IndexEntry> entries = new ArrayList<>();

        for(Map.Entry<String, List<Integer>> entry : termPositions.entrySet()){
            String term = entry.getKey();
            List<Integer> positions = entry.getValue();
            int termFrequency = positions.size();


            int documentFrequency = indexEntryRepository.countDistinctPagesByTerm(term);

            double tfIdf = scorer.score(termFrequency, totalTerms, documentFrequency);

            IndexEntry indexEntry = new IndexEntry();
            indexEntry.setPage(page);
            indexEntry.setTerm(term);
            indexEntry.setTermFrequency(termFrequency);
            indexEntry.setDocumentFrequency(documentFrequency);
            indexEntry.setTfIdfScore(tfIdf);

            indexEntry.setPositions(positions.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(",")));

            entries.add(indexEntry);
        }

        return entries;
    }
}
