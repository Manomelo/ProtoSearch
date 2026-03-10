package com.protosearch.protosearch.indexer;

import com.protosearch.protosearch.model.IndexEntry;
import com.protosearch.protosearch.model.Page;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class IndexJobConfig {
    private final EntityManagerFactory entityManagerFactory;
    private final PageIndexProcessor processor;
    private final IndexEntryWriter writer;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public JpaPagingItemReader<Page> pageItemReader(){
        return new JpaPagingItemReaderBuilder<Page>()
                .name("pageItemReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(50)
                .queryString("select * from page p where p.status = 'CRAWLED' ORDER BY p.id")
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
