package com.example.springai.controller

import com.example.springai.model.AddDocumentRequest
import com.example.springai.model.DeleteDocumentRequest
import com.example.springai.model.SearchRequest
import com.example.springai.model.SearchWithMetadataRequest
import com.example.springai.service.DocumentService
import org.springframework.web.bind.annotation.*

/**
 * 서비스 기반 컨트롤러
 */
@RestController
@RequestMapping("/api/service")
class ServiceBasedController(
    private val documentService: DocumentService
) {
    
    /**
     * 서비스를 통한 문서 추가
     * POST http://localhost:8080/api/service/document/add
     */
    @PostMapping("/document/add")
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
     * POST http://localhost:8080/api/service/document/search
     */
    @PostMapping("/document/search")
    fun searchDocument(@RequestBody request: SearchRequest): Map<String, Any> {
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
     * 메타데이터 필터링 검색
     * POST http://localhost:8080/api/service/document/search-with-filter
     */
    @PostMapping("/document/search-with-filter")
    fun searchWithFilter(@RequestBody request: SearchWithMetadataRequest): Map<String, Any> {
        val documents = documentService.searchWithMetadata(
            query = request.query,
            category = request.category,
            metadataFilter = request.metadataFilter,
            topK = request.topK
        )
        
        return mapOf(
            "query" to request.query,
            "topK" to request.topK,
            "category" to (request.category ?: "all"),
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
     * 문서 삭제
     * POST http://localhost:8080/api/service/document/delete
     */
    @PostMapping("/document/delete")
    fun deleteDocuments(@RequestBody request: DeleteDocumentRequest): Map<String, Any> {
        documentService.deleteDocuments(request.documentIds)
        
        return mapOf(
            "status" to "success",
            "message" to "${request.documentIds.size}개의 문서가 삭제되었습니다.",
            "deletedIds" to request.documentIds
        )
    }
    
    /**
     * 통계 조회
     * GET http://localhost:8080/api/service/document/stats
     */
    @GetMapping("/document/stats")
    fun getStats(): Map<String, Any> {
        return documentService.getStatistics()
    }
}

