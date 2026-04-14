# Information Retrieval Backend: Step-by-Step Presentation Guide

This guide is a presenter script for explaining how the backend works end-to-end.

## 1) Start With The Big Picture (1-2 minutes)

Say this first:

- The project is a Spring Boot backend for Information Retrieval.
- It uses Apache Lucene to store, index, and search documents.
- It supports full CRUD, search, indexing, analytics, and evaluation.
- There are two API styles:
  - modern root endpoints like `/documents`, `/search`, `/evaluation/*`
  - legacy compatibility endpoints under `/api/*`

Show these files:

- `src/main/java/com/moneibakang/informationretrievalbackend/IrSystemApplication.java`
- `README.md`
- `Endpoints.md`

## 2) Explain The Runtime Layers (2-3 minutes)

Walk through the flow from request to Lucene:

1. **Controller layer** receives HTTP requests.
   - Main entry: `IRPlatformController` (modern endpoints)
   - Compatibility: `DocumentController`, `IndexController`, `ApiSearchController`, `EvaluationController`
2. **Service layer** runs business and IR logic.
   - Core orchestration: `IRPlatformService`
   - Legacy/auxiliary services: `DocumentServiceImpl`, `IndexServiceImpl`, `EvaluationService`
3. **Repository layer** reads/writes Lucene index.
   - `LuceneDocumentRepositoryImpl`
4. **Lucene index** on disk:
   - primary index in `index/`
   - experiment variant indexes in `index_variants/`

## 3) Document Ingestion Flow (Upload/Import) (3-4 minutes)

Use this sequence to explain data onboarding:

1. Client uploads file or sends documents.
2. Controller endpoint receives input:
   - `/documents` (direct CRUD)
   - `/api/documents/upload` or `/api/documents/bulk`
   - `/api/index/import/cisi` or `/api/index/import/pubmed`
3. Parser reads source format:
   - CISI parser: `CISIParser`
   - PubMed parser: `PubMedCorpusReader`
4. Service transforms into `Document` objects.
5. Repository writes to Lucene fields (`id`, `title`, `content`, `author`, `dataset`, `collection`, `year`, `timestamp`).
6. Documents become searchable immediately after commit.

Presenter tip:

- Emphasize that this backend can ingest both CISI and PubMed, making it useful for IR experiments across datasets.

## 4) Index Build/Rebuild Flow (2 minutes)

Show:

- `POST /index/build` (or `/api/index/build`)
- `GET /index/status` (or `/api/index/stats`)

Explain what happens:

1. `IRPlatformService.rebuildIndex()` fetches all docs.
2. It clears and rewrites index entries (fresh commit).
3. It tracks metadata:
   - `lastIndexingTimeMs`
   - `lastIndexedTokens`
   - index document stats and size
4. Workflow state changes to `INDEXED`.

## 5) Search Flow (Main Core) (4-5 minutes)

Use `/search` as the centerpiece:

1. Request includes query + options:
   - `model` (`bm25`, `tf`, `tfidf`, `normalized`)
   - `tokenizer`, `stemming`, `expansion`
   - filters (`category`, `year`, `keywords`, `operator`)
2. Service optionally expands query via pseudo-relevance feedback.
3. Lucene `QueryParser` builds the executable query.
4. Searcher sets similarity model:
   - BM25 (default production style)
   - TF-style or classic weighting
5. Top hits are fetched and paginated.
6. Backend also stores:
   - `QueryRecord` (query text, latency, model)
   - `ResultRecord` (retrieved document IDs)
7. Response returns ranked results + score + latency + paging.

Presenter tip:

- Highlight that search is not just retrieval; it also captures traceability data (query/result records) for later evaluation.

## 6) Evaluation Flow (3-4 minutes)

Demo these endpoints:

- `POST /evaluation/run`
- `GET /evaluation/metrics`
- `GET /evaluation/pr-curve`
- `GET /evaluation/dataset` (CISI grounded evaluation)

Explain:

1. Retrieved IDs are compared against relevant IDs.
2. Metrics computed:
   - Precision
   - Recall
   - F1
   - MAP
3. Precision-Recall curve is generated from ranking positions.
4. Latest metrics are kept in memory for quick retrieval.
5. Workflow state becomes `EVALUATED`.

## 7) Experiment Flow (Comparative IR) (3-4 minutes)

Show these:

- `POST /experiments/variant/build`
- `GET /experiments/variant/search`
- `POST /experiments/run`

Explain:

1. Variant indexes are created per configuration:
   - dataset + tokenizer + stemming
2. Search runs independently per variant.
3. Full experiment loops through model/tokenizer/stemming combinations.
4. Scores are averaged across CISI queries.
5. Best configuration is selected by highest MAP.

Presenter tip:

- This is the strongest research/demo feature: reproducible side-by-side retrieval comparisons.

## 8) Workflow State Machine (1-2 minutes)

Use:

- `POST /workflow/upload`
- `GET /workflow/status`
- `POST /workflow/reset`

Explain the lifecycle:

- `EMPTY -> UPLOADED -> INDEXED -> SEARCHED -> EVALUATED`

This gives non-technical users a clean process status during demos.

## 9) Reliability, Logging, and Error Handling (1-2 minutes)

Point to:

- `RequestLoggingFilter`
- `GlobalExceptionHandler`

Explain:

1. Every endpoint call logs method/path and completion latency.
2. Validation and runtime exceptions are normalized into a common API response format.
3. This improves observability and API consistency for frontend clients.

## 10) Suggested Live Demo Script (Exact Order)

Follow this order during presentation:

1. Start app.
2. Show docs:
   - `README.md`
   - `Endpoints.md`
3. Upload/import a dataset:
   - `/api/index/import/cisi` or `/api/documents/upload`
4. Show index stats:
   - `/index/status`
5. Run a basic search:
   - `/search?query=information+retrieval&model=bm25`
6. Run advanced search:
   - add `stemming=true`, `expansion=true`, and filters
7. Run evaluation:
   - `/evaluation/dataset?dataset=CISI`
8. Run experiment:
   - `/experiments/run`
9. Show workflow status:
   - `/workflow/status`
10. Close with architectural recap (controller -> service -> repository -> Lucene).

## 11) Fast Q&A Cheat Sheet

If asked "Where is ranking chosen?":

- `IRPlatformService.setSimilarity(...)`

If asked "Where are documents stored?":

- Lucene index on disk (`index/`), managed by `LuceneDocumentRepositoryImpl`

If asked "How do tokenization and stemming affect search?":

- Analyzer selection comes from `AnalyzerFactory` + `IndexAnalysisSettings`

If asked "How is evaluation done?":

- `IRPlatformService.runEvaluation(...)` computes Precision, Recall, F1, MAP, and PR curve

---

## Presentation Closing Statement

"This backend provides a full IR lifecycle: ingest documents, build/rebuild Lucene indexes, retrieve with configurable ranking, evaluate with standard IR metrics, and compare experimental configurations in a reproducible way."
