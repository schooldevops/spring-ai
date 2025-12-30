package com.example.springai.controller

import com.example.springai.model.AddDocumentRequest
import com.example.springai.model.SearchRequest
import com.example.springai.model.SearchWithMetadataRequest
import com.example.springai.service.DocumentService
import com.example.springai.service.FAQService
import com.example.springai.service.KnowledgeBaseService
import org.springframework.web.bind.annotation.*

/**
 * 서비스 기반 컨트롤러
 */
@RestController
@RequestMapping("/api/service")
class ServiceBasedController(
    private val documentService: DocumentService,
    private val knowledgeBaseService: KnowledgeBaseService,
    private val faqService: FAQService
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
     * 통계 조회
     * GET http://localhost:8080/api/service/document/stats
     */
    @GetMapping("/document/stats")
    fun getStats(): Map<String, Any> {
        return documentService.getStatistics()
    }
    
    /**
     * 지식 추가
     * POST http://localhost:8080/api/service/knowledge/add
     */
    @PostMapping("/knowledge/add")
    fun addKnowledge(@RequestBody request: AddKnowledgeRequest): Map<String, Any> {
        knowledgeBaseService.addKnowledge(
            title = request.title,
            content = request.content,
            topic = request.topic
        )
        
        return mapOf(
            "status" to "success",
            "message" to "지식이 추가되었습니다."
        )
    }
    
    /**
     * 지식 검색
     * POST http://localhost:8080/api/service/knowledge/search
     */
    @PostMapping("/knowledge/search")
    fun searchKnowledge(@RequestBody request: SearchKnowledgeRequest): Map<String, Any> {
        val results = knowledgeBaseService.searchKnowledge(
            query = request.query,
            topic = request.topic,
            topK = request.topK
        )
        
        return mapOf(
            "query" to request.query,
            "topic" to (request.topic ?: "all"),
            "topK" to request.topK,
            "resultCount" to results.size,
            "results" to results
        )
    }
    
    /**
     * FAQ 추가
     * POST http://localhost:8080/api/service/faq/add
     */
    @PostMapping("/faq/add")
    fun addFAQ(@RequestBody request: AddFAQRequest): Map<String, Any> {
        faqService.addFAQ(
            question = request.question,
            answer = request.answer,
            category = request.category
        )
        
        return mapOf(
            "status" to "success",
            "message" to "FAQ가 추가되었습니다."
        )
    }
    
    /**
     * FAQ 검색
     * POST http://localhost:8080/api/service/faq/search
     */
    @PostMapping("/faq/search")
    fun searchFAQ(@RequestBody request: SearchFAQRequest): Map<String, Any> {
        val results = faqService.searchFAQ(
            query = request.query,
            category = request.category,
            topK = request.topK
        )
        
        return mapOf(
            "query" to request.query,
            "category" to (request.category ?: "all"),
            "topK" to request.topK,
            "resultCount" to results.size,
            "results" to results
        )
    }
}

data class AddKnowledgeRequest(
    val title: String,
    val content: String,
    val topic: String
)

data class SearchKnowledgeRequest(
    val query: String,
    val topic: String? = null,
    val topK: Int = 3
)

data class AddFAQRequest(
    val question: String,
    val answer: String,
    val category: String
)

data class SearchFAQRequest(
    val query: String,
    val category: String? = null,
    val topK: Int = 3
)

