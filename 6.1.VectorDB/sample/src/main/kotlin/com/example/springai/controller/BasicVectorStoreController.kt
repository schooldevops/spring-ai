package com.example.springai.controller

import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.web.bind.annotation.*
import com.example.springai.model.AddDocumentRequest
import com.example.springai.model.SearchRequest

/**
 * 기본 VectorStore 사용법을 보여주는 컨트롤러
 */
@RestController
@RequestMapping("/api/vectordb")
class BasicVectorStoreController(
    private val vectorStore: VectorStore
) {
    
    /**
     * 단일 문서 추가
     * POST http://localhost:8080/api/vectordb/add
     * Body: {"text": "Spring AI는 프레임워크입니다.", "metadata": {"category": "framework"}}
     */
    @PostMapping("/add")
    fun addDocument(@RequestBody request: AddDocumentRequest): Map<String, Any> {
        val document = Document(
            request.text,
            request.metadata ?: emptyMap()
        )
        
        vectorStore.add(listOf(document))
        
        return mapOf(
            "status" to "success",
            "message" to "문서가 추가되었습니다.",
            "content" to request.text,
            "metadata" to (request.metadata ?: emptyMap())
        )
    }
    
    /**
     * 유사도 검색
     * GET http://localhost:8080/api/vectordb/search?query=프로그래밍&topK=5
     * 또는 POST http://localhost:8080/api/vectordb/search
     * Body: {"query": "프로그래밍", "topK": 5}
     */
    @PostMapping("/search")
    fun search(@RequestBody request: SearchRequest): Map<String, Any> {
        // Spring AI 1.0.0-M6에서는 String만 받는 similaritySearch 사용
        val documents: List<Document> = vectorStore.similaritySearch(request.query) ?: emptyList()
        // topK는 결과에서 제한
        val limitedResults = documents.take(request.topK)
        
        return mapOf(
            "query" to request.query,
            "topK" to request.topK,
            "resultCount" to limitedResults.size,
            "results" to limitedResults.mapIndexed { index, doc ->
                mapOf(
                    "rank" to (index + 1),
                    "content" to doc.text,
                    "metadata" to doc.metadata,
                    "id" to (doc.id ?: "unknown")
                )
            }
        )
    }
    
    /**
     * GET 방식 검색
     * GET http://localhost:8080/api/vectordb/search?query=프로그래밍&topK=5
     */
    @GetMapping("/search")
    fun searchGet(
        @RequestParam query: String,
        @RequestParam(defaultValue = "5") topK: Int
    ): Map<String, Any> {
        val documents: List<Document> = vectorStore.similaritySearch(query) ?: emptyList()
        val limitedResults = documents.take(topK)
        
        return mapOf(
            "query" to query,
            "topK" to topK,
            "resultCount" to limitedResults.size,
            "results" to limitedResults.mapIndexed { index, doc ->
                mapOf(
                    "rank" to (index + 1),
                    "content" to doc.text,
                    "metadata" to doc.metadata
                )
            }
        )
    }
}

