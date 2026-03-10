package com.protosearch.protosearch.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa uma entrada no índice invertido, que associa termos a páginas e suas informações relacionadas.
 */
@Entity
@Table(name = "index_entries",
       indexes = @Index(name = "idx_term", columnList = "term"))
@Data
@NoArgsConstructor
@Setter
@Getter
public class IndexEntry {

    /**
     * Identificador unico da entrada no indice
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Termo indexado.
     * Nao pode ser nulo
     */
    @Column(nullable = false)
    private String term;

    /**
     * Pagina associada ao termo
     * Relacionamento many to one com a entidade Page
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id")
    private Page page;

    /**
     * Pontuação TF-IDF (Term Frequency - Inverse Document Frequency) do termo na página.
     */
    private double tfIdfScore;

    /**
     * Frequencia do termo na pagina
     */
    private int termFrequency;

    /**
     * Numero de documentos que contem o termo
     */
    private int documentFrequency;

    /**
     * Posicoes do termo no conteudo da pagina
     * Armazenado como texto para maior flexibilidade
     */
    @Column(columnDefinition = "TEXT")
    private String positions;
}
