package com.protosearch.protosearch.indexer;

import com.protosearch.protosearch.enums.CrawlStatus;
import com.protosearch.protosearch.model.IndexEntry;
import com.protosearch.protosearch.model.Page;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class IndexJobConfig {
    private final EntityManagerFactory entityManagerFactory;
    private final PageIndexProcessor processor;
    private final IndexEntryWriter writer;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public JpaCursorItemReader<Page> pageItemReader(){
        return new JpaCursorItemReaderBuilder<Page>()
                .name("pageItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select p from Page p where p.status = :status ORDER BY p.id")
                .parameterValues(Map.of("status", CrawlStatus.CRAWLED))
                .build();
    }

    @Bean
    public Step indexStep(){
        return new StepBuilder("indexStep", jobRepository)
                .<Page, List<IndexEntry>>chunk(50, transactionManager)
                .reader(pageItemReader())
                .processor((ItemProcessor<? super Page, ? extends List<IndexEntry>>) processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job indexJob(){
        return new JobBuilder("indexJob", jobRepository)
                .start(indexStep())
                .build();
    }
}