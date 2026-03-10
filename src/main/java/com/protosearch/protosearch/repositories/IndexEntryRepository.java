package com.protosearch.protosearch.repositories;

import com.protosearch.protosearch.model.IndexEntry;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositório JPA para acesso às entradas do índice invertido.
 */
@Repository
public interface IndexEntryRepository extends JpaRepository<IndexEntry, Long> {

    @Query("select count (distinct e.pages) from IndexEntry e where e.term = :term")
    int countDistinctPagesByTerm(@Param("term") String term);

    List<IndexEntry> findByTermOrderByTfIdfScoreDesc(String term);
}
