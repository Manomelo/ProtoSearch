# Search Engine

Motor de busca construído com Spring Boot que rastreia páginas web, indexa seu conteúdo com TF-IDF e expõe uma API REST para consultas.

## Tecnologias

- **Java 21** + **Spring Boot 3.2**
- **Spring Batch** — pipeline de indexação em chunks
- **Spring Data JPA** + **PostgreSQL** — persistência de páginas e índice invertido
- **Spring Data Redis** + **Jedis** — fila e controle de URLs visitadas pelo crawler
- **Jsoup** — parsing de HTML e extração de links
- **Lombok** — redução de boilerplate

---

## Pré-requisitos

- Java 21+
- Maven 3.8+
- PostgreSQL rodando em `localhost:5432`
- Redis rodando em `localhost:6379`

---

## Configuração

Edite `src/main/resources/application.properties`:

```properties
# Banco de dados
spring.datasource.url=jdbc:postgresql://localhost:5432/searchengine
spring.datasource.username=postgres
spring.datasource.password=password

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# URLs de partida do crawler
crawler.seed-urls[0]=https://docs.oracle.com/en/java/
crawler.seed-urls[1]=https://www.wikipedia.org

# Limites do crawler
crawler.max-depth=3
crawler.max-pages=1500
crawler.politeness-delay-ms=500

# Busca
search.results-per-page=10
```

| Propriedade | Descrição | Padrão |
|---|---|---|
| `crawler.seed-urls` | Lista de URLs de partida | — |
| `crawler.max-depth` | Profundidade máxima de links a seguir | `3` |
| `crawler.max-pages` | Limite total de páginas rastreadas | `10000` |
| `crawler.politeness-delay-ms` | Espera mínima entre requisições ao mesmo domínio (ms) | `500` |
| `search.results-per-page` | Resultados por página na busca | `10` |

---

## Executando

```bash
# Compilar e rodar
./mvnw spring-boot:run

# Ou gerar o JAR e executar
./mvnw package -DskipTests
java -jar target/ProtoSearch-0.0.1-SNAPSHOT.jar
```

A aplicação sobe em `http://localhost:8080`.

---

## API

### Endpoints administrativos

#### `POST /admin/crawl`

Inicia o rastreamento a partir das seed URLs configuradas em `application.properties`.

```bash
curl -X POST http://localhost:8080/admin/crawl
```

**Resposta:** `200 OK` — `Crawl Started`

---

#### `POST /admin/index`

Dispara o job Spring Batch que indexa todas as páginas com status `CRAWLED`.
Processa em chunks de 50 páginas, calcula TF-IDF por termo e persiste o índice invertido.

```bash
curl -X POST http://localhost:8080/admin/index
```

**Resposta:** `200 OK` — `Indexing job started.`

---

#### `POST /admin/reset`

Apaga todos os dados do banco (páginas e índice) e limpa a fila do Redis.
Útil para reiniciar o processo do zero.

```bash
curl -X POST http://localhost:8080/admin/reset
```

**Resposta:** `200 OK` — `Databases deleted`

---

## Fluxo de funcionamento

```
Seed URLs
    │
    ▼
┌─────────────────────────────────────────────────┐
│                   CRAWLER                        │
│  CrawlFrontier (Redis) ──► PageFetcher (Jsoup)   │
│         │                        │               │
│  fila de URLs              RobotsChecker         │
│         │                        │               │
│         └──────────── salva Page (CRAWLED)       │
└─────────────────────────────────────────────────┘
    │
    ▼  POST /admin/index
┌─────────────────────────────────────────────────┐
│              INDEXADOR (Spring Batch)            │
│                                                  │
│  PageItemReader ──► PageIndexProcessor           │
│  (Pages CRAWLED)       │                         │
│                        │ TokenizationPipeline    │
│                        │  ├─ TextCleaner         │
│                        │  ├─ Tokenizer           │
│                        │  ├─ StopWordFilter      │
│                        │  └─ PorterStemmer       │
│                        │                         │
│                        │ TfIdfScorer             │
│                        ▼                         │
│               IndexEntryWriter                   │
│          (salva IndexEntry, marca INDEXED)       │
└─────────────────────────────────────────────────┘
    │
    ▼
┌─────────────────────────────────────────────────┐
│                   BUSCA                          │
│  QueryParser ──► QueryExecutor                   │
│  (normaliza query)    │                          │
│                       │ soma TF-IDF por página   │
│                       │ ordena e pagina          │
│                       ▼                          │
│              SnippetExtractor                    │
│          (extrai trecho do conteúdo)             │
│                       │                          │
│                       ▼                          │
│              SearchResponse (JSON)               │
└─────────────────────────────────────────────────┘
```

---

## Pipeline de tokenização

Aplicado tanto na indexação quanto nas queries de busca para garantir consistência:

1. **TextCleaner** — converte para minúsculas, remove tags HTML, URLs, pontuação e espaços extras
2. **Tokenizer** — divide por espaços, filtra tokens com ≤ 1 caractere e tokens puramente numéricos
3. **StopWordFilter** — remove palavras sem valor semântico (artigos, preposições, pronomes, etc.)
4. **PorterStemmer** — reduz cada token à sua raiz (ex: `running` → `run`)

---

## Estrutura do projeto

```
src/main/java/projeto/ProtoSearch/searchengine/
├── controller/
│   └── AdminController.java        # Endpoints administrativos
├── crawler/
│   ├── CrawlFrontier.java          # Fila e controle de visitados (Redis)
│   ├── CrawlService.java           # Orquestração do crawling
│   ├── CrawlerProprieties.java     # Configurações do crawler
│   ├── FetchResult.java            # Resultado de uma requisição HTTP
│   ├── PageFetcher.java            # Requisições HTTP com Jsoup
│   └── RobotsChecker.java          # Respeito ao robots.txt
├── indexer/
│   ├── IndexEntryRepository.java   # Repositório do índice invertido
│   ├── IndexEntryWriter.java       # Writer do Spring Batch
│   ├── IndexJobConfig.java         # Configuração do Job de indexação
│   ├── PageIndexProcessor.java     # Processor: Page → List<IndexEntry>
│   ├── PorterStemmer.java          # Algoritmo de stemming
│   ├── StopWordFilter.java         # Remoção de stopwords
│   ├── TextCleaner.java            # Limpeza e normalização de texto
│   ├── TfIdScorer.java             # Cálculo de TF-IDF
│   ├── TokenizationPipeline.java   # Orquestração das etapas de tokenização
│   └── Tokenizer.java              # Divisão de texto em tokens
├── models/
│   ├── IndexEntry.java             # Entidade: entrada do índice invertido
│   ├── Page.java                   # Entidade: página rastreada
│   ├── PageRepository.java         # Repositório de páginas
│   ├── SearchResponse.java         # DTO de resposta da busca
│   └── SearchResult.java           # DTO de um resultado individual
├── enums/
│   └── CrawlStatus.java            # Estados de uma página (PENDING → INDEXED)
├── query/
│   ├── QueryExecutor.java          # Execução da busca com ranqueamento TF-IDF
│   ├── QueryParser.java            # Normalização da query do usuário
│   └── SnippetExtractor.java       # Extração de trechos relevantes do conteúdo
└── SearchEngineApplication.java    # Ponto de entrada
```
