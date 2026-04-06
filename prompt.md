# Prompt for Frontend Upgrade (React.js)

You are a Principal Frontend Architect and Senior UX Engineer.  
Refactor and upgrade my existing React.js frontend into a production-grade Information Retrieval platform UI that integrates with my Spring Boot backend.

## Mission

Build a scalable, modular, high-performance frontend that supports:

- Full CRUD for Documents, Queries, and Results
- Advanced search and retrieval workflows
- Evaluation and analytics visualization
- Clean architecture, strong UX, and maintainable code

Do not do a shallow rewrite. Preserve what works, improve structure and quality.

---

## Backend API Contract (Use These Endpoints)

Base URL: `http://localhost:8080`

### Documents

- `POST /documents`
- `GET /documents?page=&size=&category=&year=`
- `GET /documents/{id}`
- `PUT /documents/{id}`
- `DELETE /documents/{id}`

### Queries

- `POST /queries`
- `GET /queries`
- `GET /queries/{id}`
- `PUT /queries/{id}`
- `DELETE /queries/{id}`

### Results

- `POST /results`
- `GET /results`
- `GET /results/{id}`
- `PUT /results/{id}`
- `DELETE /results/{id}`

### Indexing

- `POST /index/build`
- `GET /index/status`

### Search

- `GET /search?query=&model=tf|tfidf|normalized|bm25&stemming=true|false&expansion=true|false&category=&year=&keywords=&operator=AND|OR&page=&size=`
- `POST /search/expand?query=...`

### Evaluation

- `POST /evaluation/run`
- `GET /evaluation/metrics`
- `GET /evaluation/pr-curve`

### Analytics

- `GET /analytics/term-distribution`
- `GET /analytics/zipf`

### Response format

All APIs return:

```json
{
  "success": true,
  "data": {},
  "message": "",
  "statusCode": 200
}
```

---

## Technical Requirements

### 1) Frontend Architecture

Implement or refactor into:

- `src/app` (app shell, providers, router)
- `src/features/*` (document, query, result, search, evaluation, analytics, indexing)
- `src/shared/*` (ui components, hooks, api client, utils, constants, types)
- `src/pages/*` (route-level pages)

Use:

- React 18+
- React Router
- Axios or Fetch wrapper with interceptors
- Centralized state strategy (React Query strongly preferred)
- Type safety with TypeScript if possible; if JS project, define robust JSDoc types

### 2) UI/UX Requirements

Design a professional dashboard with:

- Left navigation: Documents, Search, Queries, Results, Indexing, Evaluation, Analytics, Settings
- Top bar: global search, system status
- Responsive layouts for desktop + tablet
- Loading skeletons, empty states, and helpful error states
- Toast notifications for success/error actions
- Confirmation modals for destructive actions
- Accessible forms (labels, keyboard support, aria where needed)

### 3) Feature Implementation

#### Documents

- Data table with pagination, filter by category/year
- Create/Edit modal form with validation
- View details panel
- Delete action with confirm modal

#### Search

- Advanced search form:
  - query text
  - model dropdown (tf/tfidf/normalized/bm25)
  - stemming toggle
  - expansion toggle
  - metadata filters (category/year)
  - keyword + operator (AND/OR)
  - page + size
- Search results list with scores
- Save query and save result set actions
- Query expansion trigger and display original vs expanded query

#### Indexing

- "Build/Rebuild Index" action
- Index status widget (size, docs, deleted docs, indexing time, token stats)
- Real-time feedback while action is running

#### Evaluation

- Input relevant and retrieved doc IDs
- Run evaluation button
- Display Precision, Recall, F1, MAP cards
- PR curve chart (line chart)

#### Analytics

- Term distribution table/chart
- Zipf analysis visualization (rank vs frequency trend)

### 4) Performance Requirements

- Use memoization where needed
- Use debounced inputs for search/filter text fields
- Prevent unnecessary rerenders
- Cache server state and refetch smartly
- Paginate large lists and avoid rendering huge arrays at once

### 5) API Layer Standards

- Create a single API client module
- Centralize endpoint constants
- Handle `success=false` and `statusCode` gracefully
- Unified error parser and user-friendly error messages
- Add request/response logging in development mode

### 6) Code Quality

- Keep components small and focused
- Prefer reusable UI primitives
- Enforce linting and formatting
- Add unit tests for critical logic/components
- Add integration tests for major user flows

### 7) Security and Reliability

- Sanitize and validate form input on client side
- Prevent unsafe HTML rendering
- Guard against undefined/null API states
- Add error boundaries around major sections

---

## Deliverables

1. Refactored React frontend with modular structure
2. Full integration with all listed backend endpoints
3. Production-quality UX and visual polish
4. Reusable API/service layer
5. Test coverage for core features
6. Updated frontend README with:
   - setup
   - env variables
   - run/build/test scripts
   - architecture overview

---

## Execution Rules

- Preserve existing working functionality while improving structure.
- Do not remove useful existing pages without replacing them.
- Prioritize maintainability, consistency, and performance.
- Explain major design choices before finalizing.
- If backend assumptions are unclear, define explicit assumptions in code comments and README.

Now implement this as a complete frontend modernization task.
