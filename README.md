# Information Retrieval Backend

Production-oriented Spring Boot backend for an Information Retrieval (IR) platform using Apache Lucene.

## What This Backend Supports

- Full CRUD for `Document`, `Query`, and `Result` records
- Index build/rebuild and index status observability
- Search and retrieval with configurable ranking and refinement
- Query expansion (pseudo-relevance feedback style)
- Evaluation endpoints (Precision, Recall, F1, MAP, PR curve)
- Term distribution and Zipf-style analytics
- Standard API envelope for consistent responses

## Architecture

- **Controller Layer**: REST APIs
- **Service Layer**: business + IR logic (`IRPlatformService`)
- **Repository Layer**: persistence abstraction (`LuceneDocumentRepository`)
- **IR Engine Layer**: Lucene indexing and retrieval internals

Legacy `/api/*` endpoints remain available for backward compatibility.
New modular platform endpoints are exposed from root paths.

## API Base URL

`http://localhost:8080`

## Standard Response Format

```json
{
  "success": true,
  "data": {},
  "message": "..."
}
```

## New Endpoint Coverage

### Document CRUD
- `POST /documents`
- `GET /documents`
- `GET /documents/{id}`
- `PUT /documents/{id}`
- `DELETE /documents/{id}`

### Query CRUD
- `POST /queries`
- `GET /queries`
- `GET /queries/{id}`
- `PUT /queries/{id}`
- `DELETE /queries/{id}`

### Result CRUD
- `POST /results`
- `GET /results`
- `GET /results/{id}`
- `PUT /results/{id}`
- `DELETE /results/{id}`

### Indexing
- `POST /index/build`
- `GET /index/status`

### Search and Retrieval
- `GET /search`
  - Required: `query`
  - Optional:
    - `model=tf|tfidf|normalized|bm25`
    - `stemming=true|false`
    - `expansion=true|false`
    - `category`, `year`
    - `keywords`, `operator=AND|OR`
    - `page`, `size`

### Query Expansion
- `POST /search/expand?query=...`

### Evaluation
- `POST /evaluation/run`
- `GET /evaluation/metrics`
- `GET /evaluation/pr-curve`

### Analytics
- `GET /analytics/term-distribution`
- `GET /analytics/zipf`

## Quick Start

### Run
```bash
./mvnw spring-boot:run
```

### Build
```bash
./mvnw clean package
```

### OpenAPI / Swagger UI
- [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

## Example Requests

```bash
# Create a document
curl -X POST http://localhost:8080/documents \
  -H "Content-Type: application/json" \
  -d '{
    "title":"Lucene BM25 Notes",
    "content":"BM25 generally outperforms plain TF in many corpora.",
    "author":"IR Team",
    "collection":"academic",
    "dataset":"CISI"
  }'

# Search with filtering + ranking model
curl "http://localhost:8080/search?query=lucene%20ranking&model=bm25&stemming=true&expansion=true&category=academic&operator=AND&page=0&size=10"

# Get index status
curl http://localhost:8080/index/status
```

## Notes

- Document validation requires non-empty `title` and `content`.
- Logging includes search latency and indexing metrics.
- Existing `index` and dataset import logic remain available in legacy controllers.
