package com.moneibakang.informationretrievalbackend.controller;

import com.moneibakang.informationretrievalbackend.api.ApiResponse;
import com.moneibakang.informationretrievalbackend.dto.EvaluationRunRequest;
import com.moneibakang.informationretrievalbackend.model.Document;
import com.moneibakang.informationretrievalbackend.model.EvaluationMetrics;
import com.moneibakang.informationretrievalbackend.model.QueryRecord;
import com.moneibakang.informationretrievalbackend.model.ResultRecord;
import com.moneibakang.informationretrievalbackend.service.IRPlatformService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class IRPlatformController {
    private static final Logger log = LoggerFactory.getLogger(IRPlatformController.class);
    private final IRPlatformService service;

    public IRPlatformController(IRPlatformService service) {
        this.service = service;
    }

    @PostMapping("/documents")
    public ResponseEntity<ApiResponse<Document>> createDocument(@Valid @RequestBody Document document) throws IOException {
        log.info("POST /documents called");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(service.createDocument(document), "Document created", HttpStatus.CREATED.value()));
    }

    @GetMapping("/documents")
    public ResponseEntity<ApiResponse<List<Document>>> listDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer year) throws IOException {
        log.info("GET /documents called page={} size={} category={} year={}", page, size, category, year);
        return ResponseEntity.ok(ApiResponse.ok(service.listDocuments(page, size, category, year), "Documents fetched", HttpStatus.OK.value()));
    }

    @GetMapping("/documents/{id}")
    public ResponseEntity<ApiResponse<Document>> getDocument(@PathVariable String id) throws IOException {
        log.info("GET /documents/{} called", id);
        Document doc = service.getDocument(id);
        if (doc == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Document not found", HttpStatus.NOT_FOUND.value()));
        }
        return ResponseEntity.ok(ApiResponse.ok(doc, "Document fetched", HttpStatus.OK.value()));
    }

    @PutMapping("/documents/{id}")
    public ResponseEntity<ApiResponse<Document>> updateDocument(@PathVariable String id, @RequestBody Document document) throws IOException {
        log.info("PUT /documents/{} called", id);
        return ResponseEntity.ok(ApiResponse.ok(service.updateDocument(id, document), "Document updated", HttpStatus.OK.value()));
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable String id) throws IOException {
        log.info("DELETE /documents/{} called", id);
        service.deleteDocument(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.ok(null, "Document deleted", HttpStatus.NO_CONTENT.value()));
    }

    @PostMapping("/index/build")
    public ResponseEntity<ApiResponse<Map<String, Object>>> buildIndex() throws IOException {
        log.info("POST /index/build called");
        return ResponseEntity.ok(ApiResponse.ok(service.rebuildIndex(), "Index built", HttpStatus.OK.value()));
    }

    @GetMapping("/index/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> indexStatus() throws IOException {
        log.info("GET /index/status called");
        return ResponseEntity.ok(ApiResponse.ok(service.getIndexStatus(), "Index status", HttpStatus.OK.value()));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Map<String, Object>>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "bm25") String model,
            @RequestParam(defaultValue = "standard") String tokenizer,
            @RequestParam(defaultValue = "false") boolean stemming,
            @RequestParam(defaultValue = "false") boolean expansion,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String keywords,
            @RequestParam(defaultValue = "AND") String operator,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws IOException {
        log.info("GET /search called query={} model={} tokenizer={} page={} size={}", query, model, tokenizer, page, size);
        Map<String, Object> result = service.search(query, model, tokenizer, stemming, expansion, category, year, keywords, operator, page, size);
        return ResponseEntity.ok(ApiResponse.ok(result, "Search completed", HttpStatus.OK.value()));
    }

    @PostMapping("/search/expand")
    public ResponseEntity<ApiResponse<Map<String, String>>> expandQuery(@RequestParam String query) throws IOException {
        log.info("POST /search/expand called query={}", query);
        String expanded = service.expandQuery(query, 5);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("original", query, "expanded", expanded), "Query expanded", HttpStatus.OK.value()));
    }

    @PostMapping("/evaluation/run")
    public ResponseEntity<ApiResponse<EvaluationMetrics>> runEvaluation(@RequestBody EvaluationRunRequest request) {
        log.info("POST /evaluation/run called");
        EvaluationMetrics metrics = service.runEvaluation(request.getRetrievedDocIds(), request.getRelevantDocIds());
        return ResponseEntity.ok(ApiResponse.ok(metrics, "Evaluation completed", HttpStatus.OK.value()));
    }

    @GetMapping("/evaluation/metrics")
    public ResponseEntity<ApiResponse<EvaluationMetrics>> metrics() {
        log.info("GET /evaluation/metrics called");
        return ResponseEntity.ok(ApiResponse.ok(service.getLastMetrics(), "Current metrics", HttpStatus.OK.value()));
    }

    @GetMapping("/evaluation/pr-curve")
    public ResponseEntity<ApiResponse<List<double[]>>> prCurve() {
        log.info("GET /evaluation/pr-curve called");
        return ResponseEntity.ok(ApiResponse.ok(service.getLastPrCurve(), "Precision-recall curve", HttpStatus.OK.value()));
    }

    @GetMapping("/analytics/term-distribution")
    public ResponseEntity<ApiResponse<Map<String, Object>>> termDistribution() throws IOException {
        log.info("GET /analytics/term-distribution called");
        return ResponseEntity.ok(ApiResponse.ok(service.termDistributionStats(), "Term distribution stats", HttpStatus.OK.value()));
    }

    @GetMapping("/analytics/zipf")
    public ResponseEntity<ApiResponse<Map<String, Object>>> zipf() throws IOException {
        log.info("GET /analytics/zipf called");
        return ResponseEntity.ok(ApiResponse.ok(service.termDistributionStats(), "Zipf analysis", HttpStatus.OK.value()));
    }

    @PostMapping("/queries")
    public ResponseEntity<ApiResponse<QueryRecord>> createQuery(@RequestBody QueryRecord query) {
        log.info("POST /queries called");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(service.createQueryRecord(query), "Query created", HttpStatus.CREATED.value()));
    }

    @GetMapping("/queries")
    public ResponseEntity<ApiResponse<List<QueryRecord>>> listQueries() {
        log.info("GET /queries called");
        return ResponseEntity.ok(ApiResponse.ok(service.listQueries(), "Queries fetched", HttpStatus.OK.value()));
    }

    @GetMapping("/queries/{id}")
    public ResponseEntity<ApiResponse<QueryRecord>> getQuery(@PathVariable String id) {
        log.info("GET /queries/{} called", id);
        QueryRecord query = service.getQuery(id);
        if (query == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Query not found", HttpStatus.NOT_FOUND.value()));
        }
        return ResponseEntity.ok(ApiResponse.ok(query, "Query fetched", HttpStatus.OK.value()));
    }

    @PutMapping("/queries/{id}")
    public ResponseEntity<ApiResponse<QueryRecord>> updateQuery(@PathVariable String id, @RequestBody QueryRecord query) {
        log.info("PUT /queries/{} called", id);
        return ResponseEntity.ok(ApiResponse.ok(service.updateQuery(id, query), "Query updated", HttpStatus.OK.value()));
    }

    @DeleteMapping("/queries/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteQuery(@PathVariable String id) {
        log.info("DELETE /queries/{} called", id);
        service.deleteQuery(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.ok(null, "Query deleted", HttpStatus.NO_CONTENT.value()));
    }

    @PostMapping("/results")
    public ResponseEntity<ApiResponse<ResultRecord>> createResult(@RequestBody ResultRecord result) {
        log.info("POST /results called");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(service.createResultRecord(result), "Result created", HttpStatus.CREATED.value()));
    }

    @GetMapping("/results")
    public ResponseEntity<ApiResponse<List<ResultRecord>>> listResults() {
        log.info("GET /results called");
        return ResponseEntity.ok(ApiResponse.ok(service.listResults(), "Results fetched", HttpStatus.OK.value()));
    }

    @GetMapping("/results/{id}")
    public ResponseEntity<ApiResponse<ResultRecord>> getResult(@PathVariable String id) {
        log.info("GET /results/{} called", id);
        ResultRecord result = service.getResult(id);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Result not found", HttpStatus.NOT_FOUND.value()));
        }
        return ResponseEntity.ok(ApiResponse.ok(result, "Result fetched", HttpStatus.OK.value()));
    }

    @PutMapping("/results/{id}")
    public ResponseEntity<ApiResponse<ResultRecord>> updateResult(@PathVariable String id, @RequestBody ResultRecord result) {
        log.info("PUT /results/{} called", id);
        return ResponseEntity.ok(ApiResponse.ok(service.updateResult(id, result), "Result updated", HttpStatus.OK.value()));
    }

    @DeleteMapping("/results/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteResult(@PathVariable String id) {
        log.info("DELETE /results/{} called", id);
        service.deleteResult(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.ok(null, "Result deleted", HttpStatus.NO_CONTENT.value()));
    }

    @PostMapping("/experiments/variant/build")
    public ResponseEntity<ApiResponse<Map<String, Object>>> buildVariant(@RequestParam(defaultValue = "CISI") String dataset,
                                                                         @RequestParam(defaultValue = "standard") String tokenizer,
                                                                         @RequestParam(defaultValue = "false") boolean stemming) throws IOException {
        return ResponseEntity.ok(ApiResponse.ok(service.buildIndexVariant(dataset, tokenizer, stemming), "Variant index built", HttpStatus.OK.value()));
    }

    @GetMapping("/experiments/variant/search")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchVariant(@RequestParam String query,
                                                                          @RequestParam(defaultValue = "CISI") String dataset,
                                                                          @RequestParam(defaultValue = "standard") String tokenizer,
                                                                          @RequestParam(defaultValue = "false") boolean stemming,
                                                                          @RequestParam(defaultValue = "bm25") String model,
                                                                          @RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "10") int size) throws IOException {
        return ResponseEntity.ok(ApiResponse.ok(service.searchVariant(query, dataset, tokenizer, stemming, model, page, size), "Variant search complete", HttpStatus.OK.value()));
    }

    @PostMapping("/experiments/run")
    public ResponseEntity<ApiResponse<Map<String, Object>>> runExperiment() throws IOException {
        return ResponseEntity.ok(ApiResponse.ok(service.runCisiExperiment(), "Experiment run complete", HttpStatus.OK.value()));
    }

    @GetMapping("/evaluation/dataset")
    public ResponseEntity<ApiResponse<Map<String, Object>>> evaluateDataset(@RequestParam String dataset,
                                                                            @RequestParam(required = false) String queryFilePath,
                                                                            @RequestParam(required = false) String relevanceFilePath) throws IOException {
        return ResponseEntity.ok(ApiResponse.ok(service.runDatasetEvaluation(dataset, queryFilePath, relevanceFilePath), "Dataset evaluation complete", HttpStatus.OK.value()));
    }

    @PostMapping("/workflow/upload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadForWorkflow(@RequestParam("file") MultipartFile file,
                                                                               @RequestParam(required = false) String dataset) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(service.markUpload(file, dataset), "Upload completed", HttpStatus.CREATED.value()));
    }

    @GetMapping("/workflow/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> workflowStatus() {
        return ResponseEntity.ok(ApiResponse.ok(service.workflowStatus(), "Workflow status", HttpStatus.OK.value()));
    }

    @PostMapping("/workflow/reset")
    public ResponseEntity<ApiResponse<Map<String, Object>>> workflowReset() {
        return ResponseEntity.ok(ApiResponse.ok(service.resetWorkflow(), "Workflow reset", HttpStatus.OK.value()));
    }
}
