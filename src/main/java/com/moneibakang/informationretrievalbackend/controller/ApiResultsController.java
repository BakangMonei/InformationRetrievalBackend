package com.moneibakang.informationretrievalbackend.controller;

import com.moneibakang.informationretrievalbackend.api.ApiResponse;
import com.moneibakang.informationretrievalbackend.model.ResultRecord;
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
 * Same result CRUD as {@link IRPlatformController} {@code /results}, under {@code /api} for clients
 * that use a single API prefix.
 */
@RestController
@RequestMapping("/api/results")
@CrossOrigin(origins = "*")
public class ApiResultsController {

    private final IRPlatformService irPlatformService;

    public ApiResultsController(IRPlatformService irPlatformService) {
        this.irPlatformService = irPlatformService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ResultRecord>> createResult(@RequestBody ResultRecord result) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(irPlatformService.createResultRecord(result), "Result created", HttpStatus.CREATED.value()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ResultRecord>>> listResults() {
        return ResponseEntity.ok(ApiResponse.ok(irPlatformService.listResults(), "Results fetched", HttpStatus.OK.value()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ResultRecord>> getResult(@PathVariable String id) {
        ResultRecord result = irPlatformService.getResult(id);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Result not found", HttpStatus.NOT_FOUND.value()));
        }
        return ResponseEntity.ok(ApiResponse.ok(result, "Result fetched", HttpStatus.OK.value()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ResultRecord>> updateResult(@PathVariable String id, @RequestBody ResultRecord result) {
        return ResponseEntity.ok(ApiResponse.ok(irPlatformService.updateResult(id, result), "Result updated", HttpStatus.OK.value()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteResult(@PathVariable String id) {
        irPlatformService.deleteResult(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.ok(null, "Result deleted", HttpStatus.NO_CONTENT.value()));
    }
}
