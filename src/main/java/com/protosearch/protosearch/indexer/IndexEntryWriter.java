package com.protosearch.protosearch.indexer;

import com.protosearch.protosearch.enums.CrawlStatus;
import com.protosearch.protosearch.model.IndexEntry;
import com.protosearch.protosearch.model.Page;
import com.protosearch.protosearch.repositories.IndexEntryRepository;
import com.protosearch.protosearch.repositories.PageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class IndexEntryWriter implements ItemWriter<List<IndexEntry>> {

    private final IndexEntryRepository indexEntryRepository ;
    private final PageRepository pageRepository;

    @Override
    public void write(Chunk<? extends List<IndexEntry>> chunk){
        for (List<IndexEntry> entries : chunk){
            if(entries.isEmpty()) continue;

            indexEntryRepository.saveAll(entries);

            Page page = entries.get(0).getPage();

            page.setStatus(CrawlStatus.INDEXED);
            pageRepository.save(page);

            log.info("Indexed page: {} ({} terms)", page.getUrl(), entries.size());
        }
    }
}
