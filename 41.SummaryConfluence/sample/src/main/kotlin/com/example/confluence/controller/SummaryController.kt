package com.example.confluence.controller

import com.example.confluence.model.SummaryRequest
import com.example.confluence.model.SummaryResponse
import com.example.confluence.service.SummaryService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

/** 문서 요약 REST API 컨트롤러 */
@RestController
@RequestMapping("/api/summary")
class SummaryController(private val summaryService: SummaryService) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /** 페이지 트리 요약 POST /api/summary/tree */
    @PostMapping("/tree")
    fun summarizeTree(@RequestBody request: SummaryRequest): SummaryResponse {
        logger.info("Received summary request: $request")
        return summaryService.summarizePageTree(request)
    }

    /** 헬스 체크 */
    @GetMapping("/health")
    fun health(): Map<String, String> {
        return mapOf("status" to "UP", "service" to "Confluence Summarizer")
    }
}
