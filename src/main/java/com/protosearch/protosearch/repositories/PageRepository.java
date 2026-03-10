package com.protosearch.protosearch.repositories;

import com.protosearch.protosearch.enums.CrawlStatus;
import com.protosearch.protosearch.model.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositório JPA para acesso e persistência de {@link Page}s rastreadas.
 */
@Repository
public interface PageRepository extends JpaRepository<Page, Long> {

    long countByStatus(CrawlStatus status);

    boolean existsByUrl(String url);
}
