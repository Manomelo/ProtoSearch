package com.protosearch.protosearch.crawler;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CrawlerFrontier {
    private final RedisTemplate<String, String> redisTemplate;

    private static final String QUEUE_KEY = "crawl:queue";

    private static final String VISITED_KEY = "crawl:visited";

    public void enqueue(String url){
        Boolean isNew = redisTemplate.opsForSet().add(VISITED_KEY, url) == 1;
        if(isNew){
            redisTemplate.opsForList().rightPush(QUEUE_KEY, url);
        }

    }

    public void reset(){
        redisTemplate.delete("crawl:visited");
        redisTemplate.delete("crawl:queue");
    }

    public Optional<String>dequeue(){
        String url = redisTemplate.opsForList().leftPop(QUEUE_KEY);
        return Optional.ofNullable(url);
    }

    public boolean hasVisited(String url){
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(VISITED_KEY, url) );
    }

    public long queueSize(){
        return redisTemplate.opsForList().size(QUEUE_KEY);
    }
}
