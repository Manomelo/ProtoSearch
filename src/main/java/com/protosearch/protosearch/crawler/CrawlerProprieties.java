package com.protosearch.protosearch.crawler;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "crawler")
@Data
public class CrawlerProprieties {

    private List<String> seedUrls;

    private int maxDepth;

    private int maxPages;

    private long politenessDelayMs;
}
