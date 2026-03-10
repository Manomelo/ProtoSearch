package com.protosearch.protosearch.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

/**
 * Representa uma página web armazenada no sistema, contendo informações como URL, conteúdo, status HTTP e metadados.
 */
@Entity
@Table(name = "pages")
@Data
@NoArgsConstructor
public class Page {

    /**
     * Identificador único da página.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * URL da página.
     * Deve ser única, pode ser nula, e tem um comprimento máximo de 2048 caracteres.
     */
    @Column(unique = true, nullable = true, length = 2048)
    private String url;

    /**
     * Conteúdo HTML bruto da página.
     * Armazenado como texto.
     */
    @Column(columnDefinition = "TEXT")
    private String rawHTML;

    /**
     * Texto simples extraído do conteúdo da página.
     * Armazenado como texto.
     */
    @Column(columnDefinition = "TEXT")
    private String plainText;

    /**
     * Título da página.
     */
    private String title;

    /**
     * Código de status HTTP retornado ao acessar a página.
     */
    private int httpStatus;

    /**
     * Profundidade da página no processo de rastreamento.
     */
    private int depth;

    /**
     * Status do rastreamento da página.
     * Representado como uma enumeração.
     */
    @Enumerated(EnumType.STRING)
    private CrawlStatus status;

    /**
     * Data e hora em que a página foi rastreada.
     */
    private LocalDateTime crawledAt;
}