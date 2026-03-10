package com.protosearch.protosearch.crawler;

import lombok.Value;

import java.util.List;

@Value
public class FetchResult {

    String url;

    boolean success;

    int statusCode;

    String rawHtml;

    String plainText;

    String title;

    List<String> links;

    String errorMessage;

    public static FetchResult success(String url, int status, String html,
                                      String text, String title, List<String> links) {
        return new FetchResult(url, true, status, html, text, title, links, null);
    }

    public static FetchResult failure(String url, String error){
        return new FetchResult(url, false, 0, null, null, null, List.of(), error);
    }


}
