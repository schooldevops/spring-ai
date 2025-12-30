package com.example.springai.controller

import com.example.springai.model.RAGRequest
import com.example.springai.model.RAGResponse
import com.example.springai.service.AdvancedRAGService
import org.springframework.web.bind.annotation.*

/**
 * 고급 RAG 컨트롤러
 * 
 * 재랭킹, Context 길이 제한 등 고급 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/rag/advanced")
class AdvancedRAGController(
    private val advancedRAGService: AdvancedRAGService
) {
    
    /**
     * 재랭킹을 포함한 RAG
     * POST http://localhost:8080/api/rag/advanced/ask-with-reranking
     * Body: {"question": "...", "topK": 5, "finalK": 3}
     */
    @PostMapping("/ask-with-reranking")
    fun askWithReranking(@RequestBody request: RerankingRAGRequest): RAGResponse {
        val result = advancedRAGService.askWithReranking(
            question = request.question,
            topK = request.topK,
            finalK = request.finalK
        )
        
        return RAGResponse(
            question = result.question,
            answer = result.answer,
            sources = result.sources.map { source ->
                com.example.springai.model.DocumentSource(
                    content = source.content,
                    metadata = source.metadata
                )
            },
            context = result.context
        )
    }
    
    /**
     * Context 길이 제한을 포함한 RAG
     * POST http://localhost:8080/api/rag/advanced/ask-with-length-limit
     * Body: {"question": "...", "topK": 5, "maxLength": 2000}
     */
    @PostMapping("/ask-with-length-limit")
    fun askWithLengthLimit(@RequestBody request: LengthLimitRAGRequest): RAGResponse {
        val result = advancedRAGService.askWithLengthLimit(
            question = request.question,
            topK = request.topK,
            maxLength = request.maxLength
        )
        
        return RAGResponse(
            question = result.question,
            answer = result.answer,
            sources = result.sources.map { source ->
                com.example.springai.model.DocumentSource(
                    content = source.content,
                    metadata = source.metadata
                )
            },
            context = result.context
        )
    }
    
    /**
     * 카테고리 필터링을 포함한 RAG
     * POST http://localhost:8080/api/rag/advanced/ask-with-category
     * Body: {"question": "...", "category": "policy", "topK": 3}
     */
    @PostMapping("/ask-with-category")
    fun askWithCategory(@RequestBody request: CategoryRAGRequest): RAGResponse {
        val result = advancedRAGService.askWithCategory(
            question = request.question,
            category = request.category,
            topK = request.topK
        )
        
        return RAGResponse(
            question = result.question,
            answer = result.answer,
            sources = result.sources.map { source ->
                com.example.springai.model.DocumentSource(
                    content = source.content,
                    metadata = source.metadata
                )
            },
            context = result.context
        )
    }
}

data class RerankingRAGRequest(
    val question: String,
    val topK: Int = 5,
    val finalK: Int = 3
)

data class LengthLimitRAGRequest(
    val question: String,
    val topK: Int = 5,
    val maxLength: Int = 2000
)

data class CategoryRAGRequest(
    val question: String,
    val category: String,
    val topK: Int = 3
)

