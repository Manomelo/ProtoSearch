package com.protosearch.protosearch.enums;

/**
 * Representa o estado de uma {@link com.protosearch.protosearch.model.Page}
 * ao longo do ciclo de vida de crawling e indexação.
 */
public enum CrawlStatus {
    /** Página enfileirada, ainda não rastreada. */
    PENDING,
    /** Página rastreada com sucesso, conteúdo disponível para indexação. */
    CRAWLED,
    /** Falha ao tentar rastrear a página (timeout, erro HTTP, etc.). */
    FAILED,
    /** Página ignorada (ex: bloqueada pelo robots.txt). */
    SKIPPED,
    /** Página rastreada e indexada com sucesso. */
    INDEXED
}