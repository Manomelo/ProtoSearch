package com.protosearch.protosearch.crawler;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RobotsChecker {

    private final Map<String, List<String>> robotsCache = new ConcurrentHashMap<>();

    public boolean isAllowed(String url){
        try{
            URL parsed = new URL(url);
            String domain = parsed.getProtocol() + "://" + parsed.getHost();
            String path = parsed.getPath();

            List<String> disallowedPaths = robotsCache.computeIfAbsent(
                    domain, this::fetchDisallowedPaths
            );



            return disallowedPaths.stream().noneMatch(path::startsWith);
        } catch (MalformedURLException e){
            return true;
        }
    }

    private List<String> fetchDisallowedPaths(String domain) {
        List<String> disallowed = new ArrayList<>();
        try{
            Document robotsTxt = Jsoup
                    .connect(domain + "/robots.txt")
                    .timeout(5000)
                    .get();

            boolean applicableSection = false;

            for(String line: robotsTxt.wholeText().split("\n"))
            {
                line = line.trim();
                if (line.equalsIgnoreCase("User-agent: *")){
                    applicableSection = true;
                } else if (line.startsWith("User-agent:")){
                    applicableSection = false;
                } else if (applicableSection && line.startsWith("Disallow")) {
                    String path = line.substring("Disallow:".length()).trim();
                    if(!path.isEmpty()) disallowed.add(path);

                }
            }
        } catch (IOException ignored){
            // Permite tudo
        }
        return disallowed;
    }
}
