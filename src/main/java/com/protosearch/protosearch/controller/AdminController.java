package com.protosearch.protosearch.controller;


import com.protosearch.protosearch.crawler.CrawlerFrontier;
import com.protosearch.protosearch.crawler.CrawlerProprieties;
import com.protosearch.protosearch.repositories.IndexEntryRepository;
import com.protosearch.protosearch.repositories.PageRepository;
import com.protosearch.protosearch.services.CrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    private final JobLauncher jobLauncher;
    private final Job indexJob;
    private final CrawlerService crawlerService;
    private final CrawlerProprieties crawlerProprieties;
    private final IndexEntryRepository indexEntryRepository;
    private final PageRepository pageRepository;
    private final CrawlerFrontier crawlerFrontier;

    public AdminController(
            @Qualifier("asyncJobLauncher") JobLauncher jobLauncher,
            Job indexJob,
            CrawlerService crawlerService,
            CrawlerProprieties crawlerProprieties,
            IndexEntryRepository indexEntryRepository,
            PageRepository pageRepository,
            CrawlerFrontier crawlerFrontier) {
        this.jobLauncher = jobLauncher;
        this.indexJob = indexJob;
        this.crawlerService = crawlerService;
        this.crawlerProprieties = crawlerProprieties;
        this.indexEntryRepository = indexEntryRepository;
        this.pageRepository = pageRepository;
        this.crawlerFrontier = crawlerFrontier;
    }

    @PostMapping("/crawl")
    public ResponseEntity<String> startCrawl() {
        try{
            List<String> seeds = crawlerProprieties.getSeedUrls();
            log.info("Start crawl with seeds: {}", seeds);
            if(seeds == null || seeds.isEmpty()){
                return ResponseEntity.badRequest().body("No seed Urls configured!");
            }
            crawlerService.startCrawl(seeds);
            return ResponseEntity.ok("Crawl started with seeds: " + seeds);
        } catch (Exception e){
            log.error("Failed to start crawl: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }


    @PostMapping("/reset")
    public ResponseEntity<String> resetDatabase(){

        indexEntryRepository.deleteAll();
        pageRepository.deleteAll();
        crawlerFrontier.reset();

        return ResponseEntity.ok("Databases deleted");
    }

    @PostMapping("/index")
    public ResponseEntity<String> triggerIndexing(){
        JobParameters params = new JobParametersBuilder()
                .addLong("started at: ", System.currentTimeMillis())
                .toJobParameters();

        try {
            jobLauncher.run(indexJob, params);

            return ResponseEntity.ok("Indexing Job Started");
        } catch (JobExecutionAlreadyRunningException | JobParametersInvalidException | JobRestartException |
                 JobInstanceAlreadyCompleteException e){
            return ResponseEntity.internalServerError().build();
        }
    }
}
