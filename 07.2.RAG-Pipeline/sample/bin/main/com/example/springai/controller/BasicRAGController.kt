package com.example.springai.controller

import com.example.springai.model.AddDocumentRequest
import com.example.springai.model.RAGRequest
import com.example.springai.model.RAGResponse
import com.example.springai.service.KnowledgeBaseService
import com.example.springai.service.PromptTemplateRAGService
import org.springframework.web.bind.annotation.*

/**
 * 기본 RAG 컨트롤러
 * 
 * PromptTemplate을 사용한 RAG 파이프라인 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/rag")
class BasicRAGController(
    private val ragService: PromptTemplateRAGService,
    private val knowledgeBaseService: KnowledgeBaseService
) {
    
    /**
     * 기본 RAG 질문 답변
     * POST http://localhost:8080/api/rag/ask
     */
    @PostMapping("/ask")
    fun ask(@RequestBody request: RAGRequest): RAGResponse {
        val result = ragService.ask(request.question, request.topK)
        
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
     * GET 방식 질문 답변
     * GET http://localhost:8080/api/rag/ask?question=환불 정책은?&topK=3
     */
    @GetMapping("/ask")
    fun askGet(
        @RequestParam question: String,
        @RequestParam(defaultValue = "3") topK: Int
    ): RAGResponse {
        val result = ragService.ask(question, topK)
        
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
     * 구조화된 RAG 질문 답변
     * POST http://localhost:8080/api/rag/ask-structured
     */
    @PostMapping("/ask-structured")
    fun askStructured(@RequestBody request: RAGRequest): RAGResponse {
        val result = ragService.askStructured(request.question, request.topK)
        
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
     * 역할 기반 RAG 질문 답변
     * POST http://localhost:8080/api/rag/ask-with-role
     * Body: {"question": "...", "role": "고객 지원 담당자", "tone": "친절", "topK": 3}
     */
    @PostMapping("/ask-with-role")
    fun askWithRole(@RequestBody request: RoleBasedRAGRequest): RAGResponse {
        val result = ragService.askWithRole(
            question = request.question,
            role = request.role ?: "고객 지원 담당자",
            tone = request.tone ?: "친절",
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
    
    /**
     * 문서 추가
     * POST http://localhost:8080/api/rag/documents
     */
    @PostMapping("/documents")
    fun addDocument(@RequestBody request: AddDocumentRequest): Map<String, Any> {
        knowledgeBaseService.addDocument(
            text = request.text,
            title = request.title ?: "문서",
            source = request.source,
            category = request.category,
            metadata = request.metadata ?: emptyMap()
        )
        
        return mapOf(
            "status" to "success",
            "message" to "문서가 추가되었습니다.",
            "title" to (request.title ?: "문서")
        )
    }
}

data class RoleBasedRAGRequest(
    val question: String,
    val role: String? = null,
    val tone: String? = null,
    val topK: Int = 3
)

