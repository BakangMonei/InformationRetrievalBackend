package com.moneibakang.informationretrievalbackend.controller;

/*
 * @Author: Monei Bakang
 * @Date: 19 March 2026
 * @Time: 09:51 hours
 */

import com.moneibakang.informationretrievalbackend.service.*;
import com.moneibakang.informationretrievalbackend.service.IndexService;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/index")
@CrossOrigin(origins = "http://localhost:3000")
public class LoggingController {
    private static final Logger logger = LoggerFactory.getLogger(LoggingController.class);

    private final IndexService indexService;
    private final DocumentService documentService;

    @Autowired
    public LoggingController(IndexService indexService, DocumentService documentService) {
        this.indexService = indexService;
        this.documentService = documentService;
    }

//    @GetMapping("/stats")
//    public ResponseEntity<String> getConsoleLogs() {
//        // Log console output to a textfile
//        List<String> logs = indexService.getConsoleLogs();
//        StringBuilder sb = new StringBuilder();
//        for (String log : logs) {
//            sb.append(log).append("\n");
//        }
//        return ResponseEntity.ok(sb.toString());
//
//
//    }

}
