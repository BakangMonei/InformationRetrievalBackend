package com.moneibakang.informationretrievalbackend.controller;

import com.moneibakang.informationretrievalbackend.api.ApiResponse;
import com.moneibakang.informationretrievalbackend.model.QueryRecord;
import com.moneibakang.informationretrievalbackend.service.IRPlatformService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Same query CRUD as {@link IRPlatformController} {@code /queries}, under {@code /api}.
 */
@RestController
@RequestMapping("/api/queries")
@CrossOrigin(origins = "*")
public class ApiQueriesController {

    private final IRPlatformService irPlatformService;

    public ApiQueriesController(IRPlatformService irPlatformService) {
        this.irPlatformService = irPlatformService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<QueryRecord>> createQuery(@RequestBody QueryRecord query) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(irPlatformService.createQueryRecord(query), "Query created", HttpStatus.CREATED.value()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<QueryRecord>>> listQueries() {
        return ResponseEntity.ok(ApiResponse.ok(irPlatformService.listQueries(), "Queries fetched", HttpStatus.OK.value()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QueryRecord>> getQuery(@PathVariable String id) {
        QueryRecord query = irPlatformService.getQuery(id);
        if (query == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Query not found", HttpStatus.NOT_FOUND.value()));
        }
        return ResponseEntity.ok(ApiResponse.ok(query, "Query fetched", HttpStatus.OK.value()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<QueryRecord>> updateQuery(@PathVariable String id, @RequestBody QueryRecord query) {
        return ResponseEntity.ok(ApiResponse.ok(irPlatformService.updateQuery(id, query), "Query updated", HttpStatus.OK.value()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteQuery(@PathVariable String id) {
        irPlatformService.deleteQuery(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.ok(null, "Query deleted", HttpStatus.NO_CONTENT.value()));
    }
}
