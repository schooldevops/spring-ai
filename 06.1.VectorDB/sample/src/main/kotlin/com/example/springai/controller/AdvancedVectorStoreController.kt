package com.example.springai.controller

import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.web.bind.annotation.*
import com.example.springai.model.AddDocumentRequest
import com.example.springai.model.SearchWithMetadataRequest

/**
 * 고급 VectorStore 기능 컨트롤러
 */
@RestController
@RequestMapping("/api/vectordb/advanced")
class AdvancedVectorStoreController(
    private val vectorStore: VectorStore
) {
    
    /**
     * 메타데이터와 함께 문서 추가
     * POST http://localhost:8080/api/vectordb/advanced/add-with-metadata
     */
    @PostMapping("/add-with-metadata")
    fun addWithMetadata(@RequestBody request: AddDocumentRequest): Map<String, Any> {
        val document = Document(
            request.id ?: java.util.UUID.randomUUID().toString(),
            request.text,
            request.metadata ?: emptyMap()
        )
        
        vectorStore.add(listOf(document))
        
        return mapOf(
            "status" to "success",
            "document" to mapOf(
                "id" to (request.id ?: "auto-generated"),
                "content" to request.text,
                "metadata" to (request.metadata ?: emptyMap())
            )
        )
    }
    
    /**
     * 메타데이터 필터링 검색
     * POST http://localhost:8080/api/vectordb/advanced/search-with-filter
     */
    @PostMapping("/search-with-filter")
    fun searchWithFilter(@RequestBody request: SearchWithMetadataRequest): Map<String, Any> {
        val documents: List<Document> = vectorStore.similaritySearch(request.query) ?: emptyList()
        
        // 메타데이터 필터링 (클라이언트 측)
        val filteredResults = if (request.metadataFilter != null) {
            documents.filter { doc ->
                request.metadataFilter.all { (key, value) ->
                    doc.metadata[key] == value
                }
            }
        } else {
            documents
        }
        
        // 메타데이터 필터링 후 topK 제한
        val finalResults = filteredResults.take(request.topK)
        
        return mapOf(
            "query" to request.query,
            "topK" to request.topK,
            "metadataFilter" to (request.metadataFilter ?: emptyMap()),
            "totalResults" to documents.size,
            "filteredResults" to filteredResults.size,
            "results" to finalResults.mapIndexed { index, doc ->
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
     * 다양한 유사도 임계값으로 검색
     * POST http://localhost:8080/api/vectordb/advanced/search-with-threshold
     */
    @PostMapping("/search-with-threshold")
    fun searchWithThreshold(
        @RequestBody request: Map<String, Any>
    ): Map<String, Any> {
        val query = request["query"] as String
        val topK = (request["topK"] as? Number)?.toInt() ?: 5
        val threshold = (request["threshold"] as? Number)?.toDouble() ?: 0.0
        
        // 임계값은 SimpleVectorStore에서 직접 지원하지 않으므로
        // 검색 후 클라이언트 측에서 필터링
        val documents: List<Document> = vectorStore.similaritySearch(query) ?: emptyList()
        val limitedResults = documents.take(topK)
        
        return mapOf(
            "query" to query,
            "topK" to topK,
            "threshold" to threshold,
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

