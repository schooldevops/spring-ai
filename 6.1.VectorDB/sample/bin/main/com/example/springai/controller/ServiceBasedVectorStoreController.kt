package com.example.springai.controller

import com.example.springai.model.AddDocumentRequest
import com.example.springai.model.SearchRequest
import com.example.springai.service.DocumentManagementService
import org.springframework.web.bind.annotation.*

/**
 * DocumentManagementService를 사용하는 컨트롤러
 */
@RestController
@RequestMapping("/api/vectordb/service")
class ServiceBasedVectorStoreController(
    private val documentService: DocumentManagementService
) {
    
    /**
     * 서비스를 통한 문서 추가
     * POST http://localhost:8080/api/vectordb/service/add
     */
    @PostMapping("/add")
    fun addDocument(@RequestBody request: AddDocumentRequest): Map<String, Any> {
        val documentId = documentService.addDocument(
            text = request.text,
            metadata = request.metadata ?: emptyMap()
        )
        
        return mapOf(
            "status" to "success",
            "documentId" to documentId,
            "message" to "문서가 추가되었습니다."
        )
    }
    
    /**
     * 서비스를 통한 검색
     * POST http://localhost:8080/api/vectordb/service/search
     */
    @PostMapping("/search")
    fun search(@RequestBody request: SearchRequest): Map<String, Any> {
        val documents = documentService.search(request.query, request.topK)
        
        return mapOf(
            "query" to request.query,
            "topK" to request.topK,
            "resultCount" to documents.size,
            "results" to documents.mapIndexed { index, doc ->
                mapOf(
                    "rank" to (index + 1),
                    "content" to doc.text,
                    "metadata" to doc.metadata
                )
            }
        )
    }
    
    /**
     * 통계 조회
     * GET http://localhost:8080/api/vectordb/service/stats
     */
    @GetMapping("/stats")
    fun getStats(): Map<String, Any> {
        return documentService.getStats()
    }
}

