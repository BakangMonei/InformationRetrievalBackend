package com.moneibakang.informationretrievalbackend.controller;

import com.moneibakang.informationretrievalbackend.api.ApiResponse;
import com.moneibakang.informationretrievalbackend.dto.EvaluationRunRequest;
import com.moneibakang.informationretrievalbackend.model.Document;
import com.moneibakang.informationretrievalbackend.model.EvaluationMetrics;
import com.moneibakang.informationretrievalbackend.model.QueryRecord;
import com.moneibakang.informationretrievalbackend.model.ResultRecord;
import com.moneibakang.informationretrievalbackend.service.IRPlatformService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class IRPlatformController {
    private final IRPlatformService service;

    public IRPlatformController(IRPlatformService service) {
        this.service = service;
    }

    @PostMapping("/documents")
    public ResponseEntity<ApiResponse<Document>> createDocument(@Valid @RequestBody Document document) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(service.createDocument(document), "Document created"));
    }

    @GetMapping("/documents")
    public ResponseEntity<ApiResponse<List<Document>>> listDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer year) throws IOException {
        return ResponseEntity.ok(ApiResponse.ok(service.listDocuments(page, size, category, year), "Documents fetched"));
    }

    @GetMapping("/documents/{id}")
    public ResponseEntity<ApiResponse<Document>> getDocument(@PathVariable String id) throws IOException {
        Document doc = service.getDocument(id);
        if (doc == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Document not found"));
        }
        return ResponseEntity.ok(ApiResponse.ok(doc, "Document fetched"));
    }

    @PutMapping("/documents/{id}")
    public ResponseEntity<ApiResponse<Document>> updateDocument(@PathVariable String id, @RequestBody Document document) throws IOException {
        return ResponseEntity.ok(ApiResponse.ok(service.updateDocument(id, document), "Document updated"));
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable String id) throws IOException {
        service.deleteDocument(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Document deleted"));
    }

    @PostMapping("/index/build")
    public ResponseEntity<ApiResponse<Map<String, Object>>> buildIndex() throws IOException {
        return ResponseEntity.ok(ApiResponse.ok(service.rebuildIndex(), "Index built"));
    }

    @GetMapping("/index/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> indexStatus() throws IOException {
        return ResponseEntity.ok(ApiResponse.ok(service.getIndexStatus(), "Index status"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Map<String, Object>>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "bm25") String model,
            @RequestParam(defaultValue = "false") boolean stemming,
            @RequestParam(defaultValue = "false") boolean expansion,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String keywords,
            @RequestParam(defaultValue = "AND") String operator,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws IOException {
        Map<String, Object> result = service.search(query, model, stemming, expansion, category, year, keywords, operator, page, size);
        return ResponseEntity.ok(ApiResponse.ok(result, "Search completed"));
    }

    @PostMapping("/search/expand")
    public ResponseEntity<ApiResponse<Map<String, String>>> expandQuery(@RequestParam String query) throws IOException {
        String expanded = service.expandQuery(query, 5);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("original", query, "expanded", expanded), "Query expanded"));
    }

    @PostMapping("/evaluation/run")
    public ResponseEntity<ApiResponse<EvaluationMetrics>> runEvaluation(@RequestBody EvaluationRunRequest request) {
        EvaluationMetrics metrics = service.runEvaluation(request.getRetrievedDocIds(), request.getRelevantDocIds());
        return ResponseEntity.ok(ApiResponse.ok(metrics, "Evaluation completed"));
    }

    @GetMapping("/evaluation/metrics")
    public ResponseEntity<ApiResponse<EvaluationMetrics>> metrics() {
        return ResponseEntity.ok(ApiResponse.ok(service.getLastMetrics(), "Current metrics"));
    }

    @GetMapping("/evaluation/pr-curve")
    public ResponseEntity<ApiResponse<List<double[]>>> prCurve() {
        return ResponseEntity.ok(ApiResponse.ok(service.getLastPrCurve(), "Precision-recall curve"));
    }

    @GetMapping("/analytics/term-distribution")
    public ResponseEntity<ApiResponse<Map<String, Object>>> termDistribution() throws IOException {
        return ResponseEntity.ok(ApiResponse.ok(service.termDistributionStats(), "Term distribution stats"));
    }

    @GetMapping("/analytics/zipf")
    public ResponseEntity<ApiResponse<Map<String, Object>>> zipf() throws IOException {
        return ResponseEntity.ok(ApiResponse.ok(service.termDistributionStats(), "Zipf analysis"));
    }

    @PostMapping("/queries")
    public ResponseEntity<ApiResponse<QueryRecord>> createQuery(@RequestBody QueryRecord query) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(service.createQueryRecord(query), "Query created"));
    }

    @GetMapping("/queries")
    public ResponseEntity<ApiResponse<List<QueryRecord>>> listQueries() {
        return ResponseEntity.ok(ApiResponse.ok(service.listQueries(), "Queries fetched"));
    }

    @GetMapping("/queries/{id}")
    public ResponseEntity<ApiResponse<QueryRecord>> getQuery(@PathVariable String id) {
        QueryRecord query = service.getQuery(id);
        if (query == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Query not found"));
        }
        return ResponseEntity.ok(ApiResponse.ok(query, "Query fetched"));
    }

    @PutMapping("/queries/{id}")
    public ResponseEntity<ApiResponse<QueryRecord>> updateQuery(@PathVariable String id, @RequestBody QueryRecord query) {
        return ResponseEntity.ok(ApiResponse.ok(service.updateQuery(id, query), "Query updated"));
    }

    @DeleteMapping("/queries/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteQuery(@PathVariable String id) {
        service.deleteQuery(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Query deleted"));
    }

    @PostMapping("/results")
    public ResponseEntity<ApiResponse<ResultRecord>> createResult(@RequestBody ResultRecord result) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(service.createResultRecord(result), "Result created"));
    }

    @GetMapping("/results")
    public ResponseEntity<ApiResponse<List<ResultRecord>>> listResults() {
        return ResponseEntity.ok(ApiResponse.ok(service.listResults(), "Results fetched"));
    }

    @GetMapping("/results/{id}")
    public ResponseEntity<ApiResponse<ResultRecord>> getResult(@PathVariable String id) {
        ResultRecord result = service.getResult(id);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Result not found"));
        }
        return ResponseEntity.ok(ApiResponse.ok(result, "Result fetched"));
    }

    @PutMapping("/results/{id}")
    public ResponseEntity<ApiResponse<ResultRecord>> updateResult(@PathVariable String id, @RequestBody ResultRecord result) {
        return ResponseEntity.ok(ApiResponse.ok(service.updateResult(id, result), "Result updated"));
    }

    @DeleteMapping("/results/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteResult(@PathVariable String id) {
        service.deleteResult(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Result deleted"));
    }
}
