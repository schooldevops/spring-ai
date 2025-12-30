package com.example.springai.controller

import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.web.bind.annotation.*
import com.example.springai.model.BatchAddRequest

/**
 * 배치 문서 추가 컨트롤러
 */
@RestController
@RequestMapping("/api/vectordb/batch")
class BatchVectorStoreController(
    private val vectorStore: VectorStore
) {
    
    /**
     * 여러 문서를 한 번에 추가
     * POST http://localhost:8080/api/vectordb/batch/add
     * Body: {"texts": ["텍스트1", "텍스트2", "텍스트3"], "source": "manual"}
     */
    @PostMapping("/add")
    fun addBatch(@RequestBody request: BatchAddRequest): Map<String, Any> {
        val documents = request.texts.mapIndexed { index, text ->
            Document(
                text,
                mapOf(
                    "index" to index,
                    "source" to (request.source ?: "unknown"),
                    "addedAt" to System.currentTimeMillis()
                )
            )
        }
        
        vectorStore.add(documents)
        
        return mapOf(
            "status" to "success",
            "message" to "문서들이 추가되었습니다.",
            "count" to documents.size,
            "source" to (request.source ?: "unknown")
        )
    }
}

