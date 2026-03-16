package com.protosearch.protosearch.services;

import com.protosearch.protosearch.crawler.CrawlerFrontier;
import com.protosearch.protosearch.crawler.FetchResult;
import com.protosearch.protosearch.crawler.PagesFetcher;
import com.protosearch.protosearch.crawler.RobotsChecker;
import com.protosearch.protosearch.enums.CrawlStatus;
import com.protosearch.protosearch.model.Page;
import com.protosearch.protosearch.repositories.PageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class CrawlerService {

    private final CrawlerFrontier frontier;

    private final PagesFetcher fetcher;

    private final RobotsChecker robotsChecker;

    private final PageRepository pageRepository;

    @Value("${crawler.max-depth:3}")
    private int maxDepth;

    @Value("${crawler.max-pages:50}")
    private int maxPages;

    @Async("crawlerExecutor")
    public void startCrawl(List<String> seedUrls){
        seedUrls.forEach(frontier::enqueue);
        crawl();
    }

    private void crawl() {
        int pagesCrawled = 0;

        while (pagesCrawled < maxPages){
            Optional<String> nextUrl = frontier.dequeue();

            if(nextUrl.isEmpty()){
                log.info("Crawl completo. Fila encerrada apos {} paginas", pagesCrawled);
            }

            String url = nextUrl.get();

            if(!robotsChecker.isAllowed(url)){
                log.debug("Bloqueado por robots.txt: {}", url);
            }

            FetchResult result = fetcher.fetch(url);

            if(result.isSuccess()){
                savePage(result);

                result.getLinks().forEach(frontier::enqueue);
                pagesCrawled++;
                log.info("Crawled ({}/{}): {}", pagesCrawled, maxPages, url);
            } else {
                saveFailedPage(result);
                log.warn("Failed to crawl {}: {}", url, result.getErrorMessage());
            }
        }
    }

    public void saveFailedPage(FetchResult result){
        Page page = new Page();
        page.setUrl(result.getUrl());
        page.setStatus(CrawlStatus.FAILED);
        page.setCrawledAt(LocalDateTime.now());
        pageRepository.save(page);
    }

    public void savePage(FetchResult result){
        Page page = new Page();
        page.setUrl(result.getUrl());
        page.setRawHTML(result.getRawHtml());
        page.setPlainText(result.getPlainText());
        page.setTitle(result.getTitle());
        page.setHttpStatus(result.getStatusCode());
        page.setStatus(CrawlStatus.CRAWLED);
        page.setCrawledAt(LocalDateTime.now());
        pageRepository.save(page);
    }
}
