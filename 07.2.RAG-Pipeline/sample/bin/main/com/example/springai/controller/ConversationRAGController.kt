package com.example.springai.controller

import com.example.springai.model.ConversationRequest
import com.example.springai.model.ConversationResponse
import com.example.springai.service.ConversationRAGService
import org.springframework.web.bind.annotation.*

/**
 * 대화 이력을 지원하는 RAG 컨트롤러
 * 
 * 멀티 턴 대화를 지원합니다.
 */
@RestController
@RequestMapping("/api/rag/conversation")
class ConversationRAGController(
    private val conversationRAGService: ConversationRAGService
) {
    
    /**
     * 대화 이력을 포함한 RAG 질문 답변
     * POST http://localhost:8080/api/rag/conversation/ask
     * Body: {"question": "...", "conversationId": "conv-123", "topK": 3}
     */
    @PostMapping("/ask")
    fun ask(@RequestBody request: ConversationRequest): ConversationResponse {
        val conversationId = request.conversationId ?: generateConversationId()
        
        val result = conversationRAGService.ask(
            question = request.question,
            conversationId = conversationId,
            topK = request.topK
        )
        
        return ConversationResponse(
            question = result.question,
            answer = result.answer,
            conversationId = result.conversationId,
            sources = result.sources.map { source ->
                com.example.springai.model.DocumentSource(
                    content = source.content,
                    metadata = source.metadata
                )
            }
        )
    }
    
    /**
     * 대화 이력 조회
     * GET http://localhost:8080/api/rag/conversation/history?conversationId=conv-123
     */
    @GetMapping("/history")
    fun getHistory(@RequestParam conversationId: String): Map<String, Any> {
        val history = conversationRAGService.getHistory(conversationId)
        
        return mapOf(
            "conversationId" to conversationId,
            "history" to history.map { (question, answer) ->
                mapOf(
                    "question" to question,
                    "answer" to answer
                )
            }
        )
    }
    
    /**
     * 대화 이력 초기화
     * DELETE http://localhost:8080/api/rag/conversation/history?conversationId=conv-123
     */
    @DeleteMapping("/history")
    fun clearHistory(@RequestParam conversationId: String): Map<String, Any> {
        conversationRAGService.clearHistory(conversationId)
        
        return mapOf(
            "status" to "success",
            "message" to "대화 이력이 초기화되었습니다.",
            "conversationId" to conversationId
        )
    }
    
    private fun generateConversationId(): String {
        return "conv-${System.currentTimeMillis()}"
    }
}

