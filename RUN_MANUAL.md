# Information Retrieval System Run Manual

This document explains how to run both the backend and frontend for the Information Retrieval system.

## 1. Prerequisites

- Java 21 installed
- Node.js and npm installed
- Git installed
- Backend and frontend projects available locally

## 2. Start the Backend

1. Open a terminal.
2. Navigate to the backend project:

```bash
cd /path/to/InformationRetrievalBackend
```

3. Build the backend:

```bash
./mvnw clean package
```

4. Start the backend:

```bash
./mvnw spring-boot:run
```

5. Confirm backend is running:
- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- Index status: [http://localhost:8080/api/index/status](http://localhost:8080/api/index/status)

## 3. Prepare Data for Search and Evaluation

Import and index data before testing search/evaluation.

### Option A: Import CISI

- `POST /api/index/import/cisi`

### Option B: Import PubMed

- `POST /api/index/import/pubmed?filePath=/absolute/path/to/file`

After import:

1. Build/rebuild index:
- `POST /api/index/build`

2. Confirm index:
- `GET /api/index/status`

## 4. Start the Frontend

1. Open a second terminal.
2. Navigate to the frontend project:

```bash
cd /path/to/frontend
```

3. Install dependencies:

```bash
npm install
```

4. Start frontend:

```bash
npm run dev
```

If your frontend uses a different script, use `npm start` instead.

5. Open frontend in browser:
- [http://localhost:3000](http://localhost:3000)

## 5. End-to-End Verification

1. Open frontend.
2. Run a search query.
3. Confirm results are returned.

For evaluation:

1. Run evaluation:
- `POST /evaluation/run` or `POST /api/evaluation/run`

2. Fetch metrics and PR curve:
- `GET /api/evaluation/metrics`
- `GET /api/evaluation/pr-curve`

## 6. Common Issues and Fixes

### 404 or "No mapping found"
- Check endpoint path.
- Avoid accidental double prefix such as `/api/api/...` unless your client is configured for it.

### Evaluation is disabled
- Ensure all required steps are complete:
  - data imported/uploaded
  - index built
  - evaluation run called at least once

### CORS errors
- Frontend should run on `http://localhost:3000`.
- Backend CORS is configured for this origin.

### Port conflict on 8080
- Stop the existing process using port 8080, or change backend port in `application.properties`.

## 7. Quick Demo Checklist

- Backend started on `localhost:8080`
- Data imported (CISI or PubMed)
- Index built successfully
- Frontend started on `localhost:3000`
- Search works
- Evaluation metrics and PR curve load

