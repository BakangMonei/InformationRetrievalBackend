# Information Retrieval Backend - Endpoints

Base URL: `http://localhost:8080`

All modern endpoints return:

```json
{
  "success": true,
  "data": {},
  "message": "",
  "statusCode": 200
}
```

---

## 1) Document Management

### `POST /documents`
- Create a new document.
- Validates required fields (`title`, `content`).

### `GET /documents`
- List documents with pagination/filtering.
- Query params:
  - `page` (default `0`)
  - `size` (default `10`)
  - `category` (optional)
  - `year` (optional)

### `GET /documents/{id}`
- Retrieve one document by ID.

### `PUT /documents/{id}`
- Update a document by ID.

### `DELETE /documents/{id}`
- Delete document by ID.

---

## 2) Query CRUD

### `POST /queries`
- Create query record.

### `GET /queries`
- List all query records.

### `GET /queries/{id}`
- Get query record by ID.

### `PUT /queries/{id}`
- Update query record.

### `DELETE /queries/{id}`
- Delete query record.

---

## 3) Result CRUD

### `POST /results`
- Create result record.

### `GET /results`
- List result records.

### `GET /results/{id}`
- Get result record by ID.

### `PUT /results/{id}`
- Update result record.

### `DELETE /results/{id}`
- Delete result record.

---

## 4) Indexing

### `POST /index/build`
- Build/rebuild the primary index.
- Tracks indexing timing and token statistics.

### `GET /index/status`
- Returns index metadata:
  - doc counts
  - deleted docs
  - index size
  - last indexing duration
  - token count

---

## 5) Search and Retrieval

### `GET /search`
- Main retrieval endpoint.
- Query params:
  - `query` (required)
  - `model` (`tf`, `tfidf`, `normalized`, `bm25`)
  - `tokenizer` (`standard`, `simple`, `whitespace`)
  - `stemming` (`true|false`)
  - `expansion` (`true|false`)
  - `category` (optional)
  - `year` (optional)
  - `keywords` (optional)
  - `operator` (`AND|OR`)
  - `page` (default `0`)
  - `size` (default `10`)
- Returns ranked results + score + latency + query metadata.

### `POST /search/expand`
- Query expansion endpoint.
- Param: `query`
- Expands query via pseudo-relevance feedback style terms.

---

## 6) Evaluation

### `POST /evaluation/run`
- Run evaluation directly from provided IDs.
- Body:
```json
{
  "retrievedDocIds": ["d1", "d2"],
  "relevantDocIds": ["d2", "d9"]
}
```
- Returns:
  - precision
  - recall
  - f1
  - map

### `GET /evaluation/metrics`
- Returns latest computed evaluation metrics.

### `GET /evaluation/pr-curve`
- Returns latest Precision-Recall curve points.

### `GET /evaluation/dataset`
- Dataset-grounded evaluation (auto-wired for CISI).
- Query params:
  - `dataset` (required, e.g. `CISI`)
  - `queryFilePath` (optional override)
  - `relevanceFilePath` (optional override)
- Uses built-in defaults for CISI:
  - `src/main/resources/CISI.QRY`
  - `src/main/resources/CISI.REL`

---

## 7) Analytics

### `GET /analytics/term-distribution`
- Returns vocabulary size and top term frequencies.

### `GET /analytics/zipf`
- Returns Zipf-style analysis derived from term distribution.

---

## 8) Experiment Runner (Comparative IR Testing)

### `POST /experiments/variant/build`
- Build persistent index variant for a specific configuration.
- Query params:
  - `dataset` (default `CISI`)
  - `tokenizer` (default `standard`)
  - `stemming` (default `false`)
- Persists variant under `index_variants/*`.

### `GET /experiments/variant/search`
- Search a specific variant index directly.
- Query params:
  - `query` (required)
  - `dataset` (default `CISI`)
  - `tokenizer` (default `standard`)
  - `stemming` (default `false`)
  - `model` (default `bm25`)
  - `page`, `size`

### `POST /experiments/run`
- Runs side-by-side experiment comparisons across:
  - tokenizer variants
  - stemmed vs unstemmed
  - ranking models (`tf`, `tfidf`, `normalized`)
- Returns comparison table + best configuration (by MAP).

---

## 9) Guided Workflow (Upload -> Index -> Search -> Evaluate)

### `POST /workflow/upload`
- Upload file and mark workflow stage as uploaded.
- Multipart form:
  - `file` (required)
  - `dataset` (optional)

### `GET /workflow/status`
- Returns workflow state machine status:
  - `uploaded`
  - `indexBuilt`
  - `searched`
  - `evaluated`
  - `stage`

### `POST /workflow/reset`
- Reset workflow state back to initial stage.

---

## 10) Legacy Endpoints (Backward Compatibility)

These remain available under `/api/*`:

### Documents (`/api/documents`)
- CRUD + legacy search + upload/bulk routes.

### Index (`/api/index`)
- `POST /api/index/recreate`
- `GET /api/index/stats`
- `POST /api/index/import/cisi`
- `POST /api/index/import/pubmed`
- Config endpoints:
  - tokenizer/stemming/ranking/normalization (GET + PUT)
- `GET /api/index/metrics`
- `GET /api/index/health`

### IR (`/api/ir`)
- Legacy indexing/search/evaluation endpoints used in earlier flow.

---

## Notes

- Endpoint request/response logging is enabled globally via request filter.
- Errors are returned in the same API envelope with `success=false` and `statusCode`.
