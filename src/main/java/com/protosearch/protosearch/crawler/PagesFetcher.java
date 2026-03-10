package com.protosearch.protosearch.crawler;

import lombok.RequiredArgsConstructor;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.print.Doc;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class PagesFetcher {

    private final Map<String, Long> lastRequestTime = new ConcurrentHashMap<>();

    @Value("${crawler.politeness-delay-ms:500}")
    private long politenessDelayMs;

    public FetchResult fetch(String url){

        try{
            enforcePolitenessDelay(url);

            Connection.Response response = Jsoup.connect(url)
                    .userAgent("ProtoEngineBot/1.0")
                    .timeout(10_000)
                    .followRedirects(true)
                    .execute();

            Document document = response.parse();

            String plainText = document.body() != null ? document.body().text() : "";

            String title = document.title();

            List<String> links = document.select("a[href]").stream()
                    .map(el -> el.absUrl("href"))
                    .filter(link -> link.startsWith("http"))
                    .distinct()
                    .toList();

            return FetchResult.success(url, response.statusCode(), document.html(), plainText, title, links);
        } catch (InterruptedException | IOException e){
            return FetchResult.failure(url, e.getMessage());
        }
    }
    private void enforcePolitenessDelay(String url) throws InterruptedException {
        // Obtém o domínio da URL
        String domain = URI.create(url).getHost();
        // Recupera o tempo da última requisição para o domínio
        long lastRequest = lastRequestTime.getOrDefault(domain, 0L);
        // Calcula o tempo decorrido desde a última requisição
        long elapsed = System.currentTimeMillis() - lastRequest;

        // Se o tempo decorrido for menor que o politeness delay, aguarda o tempo restante
        if (elapsed < politenessDelayMs) {
            Thread.sleep(politenessDelayMs - elapsed);
        }

        // Atualiza o tempo da última requisição para o domínio
        lastRequestTime.put(domain, System.currentTimeMillis());
    }
}

