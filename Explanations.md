# Information Retrieval Platform - Endpoint Explanations

## Base URL

`http://localhost:8080`

## Standard API Response

All new platform endpoints return:

```json
{
  "success": true,
  "data": {},
  "message": "..."
}
```

## New Platform Endpoints

### Document Management

- `POST /documents` - Create document
- `GET /documents?page=0&size=10&category=academic&year=2025` - List documents with pagination and metadata filters
- `GET /documents/{id}` - Get one document
- `PUT /documents/{id}` - Update document
- `DELETE /documents/{id}` - Delete document

### Query and Result CRUD

- `POST /queries`
- `GET /queries`
- `GET /queries/{id}`
- `PUT /queries/{id}`
- `DELETE /queries/{id}`

- `POST /results`
- `GET /results`
- `GET /results/{id}`
- `PUT /results/{id}`
- `DELETE /results/{id}`

### Indexing

- `POST /index/build` - Build/rebuild index and track timing/token stats
- `GET /index/status` - Index metadata (size, docs, deleted docs, timing, tokens)

### Search and Retrieval

- `GET /search` with:
  - `query` (required)
  - `model=tf|tfidf|normalized|bm25`
  - `stemming=true|false`
  - `expansion=true|false`
  - `category`, `year`
  - `keywords`, `operator=AND|OR`
  - `page`, `size`

### Query Expansion

- `POST /search/expand?query=...`
- Uses pseudo-relevance feedback style expansion from top-ranked terms.

### Evaluation APIs

- `POST /evaluation/run`
  - Body:
  ```json
  {
    "retrievedDocIds": ["d1", "d2", "d3"],
    "relevantDocIds": ["d2", "d3", "d9"]
  }
  ```
- `GET /evaluation/metrics` - Returns precision, recall, F1, MAP
- `GET /evaluation/pr-curve` - Returns PR points

### Analytics (Distinction Features)

- `GET /analytics/term-distribution` - Vocabulary size and top term frequencies
- `GET /analytics/zipf` - Zipf-style slope approximation and term rank data

## Example Calls

```bash
# Create document
curl -X POST http://localhost:8080/documents \
  -H "Content-Type: application/json" \
  -d '{
    "title":"IR and Lucene",
    "content":"BM25 is robust for ranking in sparse document spaces.",
    "author":"Principal Architect",
    "collection":"academic",
    "dataset":"CISI"
  }'

# Search with BM25 + expansion + filters
curl "http://localhost:8080/search?query=information%20retrieval&model=bm25&stemming=true&expansion=true&category=academic&operator=AND&page=0&size=10"

# Build index
curl -X POST http://localhost:8080/index/build

# Run evaluation
curl -X POST http://localhost:8080/evaluation/run \
  -H "Content-Type: application/json" \
  -d '{
    "retrievedDocIds":["1","2","3","4"],
    "relevantDocIds":["2","4","9"]
  }'
```

## Backward Compatibility Note

Legacy endpoints under `/api/*` are still present in the codebase.
The endpoints listed above are the new modular IR platform surface.
