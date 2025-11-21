package com.example.springai.service

import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Service

/**
 * 문서 관리 서비스
 * 
 * PGVectorStore를 사용한 문서 관리 기능을 제공합니다.
 */
@Service
class DocumentService(
    private val vectorStore: VectorStore
) {
    /**
     * 문서 추가
     */
    fun addDocument(text: String, metadata: Map<String, Any> = emptyMap()): String {
        val document = Document(text, metadata)
        vectorStore.add(listOf(document))
        return document.id ?: "unknown"
    }
    
    /**
     * 배치 문서 추가
     */
    fun addDocuments(texts: List<String>, source: String = "unknown", baseMetadata: Map<String, Any> = emptyMap()): Int {
        val documents = texts.mapIndexed { index, text ->
            val metadata = baseMetadata.toMutableMap()
            metadata["index"] = index
            metadata["source"] = source
            metadata["addedAt"] = System.currentTimeMillis()
            
            Document(text, metadata)
        }
        
        vectorStore.add(documents)
        return documents.size
    }
    
    /**
     * 유사도 검색
     */
    fun search(query: String, topK: Int = 5): List<Document> {
        val results = vectorStore.similaritySearch(query) ?: emptyList()
        return results.take(topK)
    }
    
    /**
     * 메타데이터 필터링 검색
     */
    fun searchWithMetadata(
        query: String,
        category: String? = null,
        metadataFilter: Map<String, Any>? = null,
        topK: Int = 5
    ): List<Document> {
        val results = vectorStore.similaritySearch(query) ?: emptyList()
        
        val filtered = results.filter { doc ->
            val metadata = doc.metadata
            
            // 카테고리 필터
            val categoryMatch = category == null || metadata["category"] == category
            
            // 추가 메타데이터 필터
            val metadataMatch = metadataFilter == null || metadataFilter.all { (key, value) ->
                metadata[key] == value
            }
            
            categoryMatch && metadataMatch
        }
        
        return filtered.take(topK)
    }
    
    /**
     * 문서 삭제
     */
    fun deleteDocuments(documentIds: List<String>) {
        vectorStore.delete(documentIds)
    }
    
    /**
     * 문서 통계 (PGVectorStore는 정확한 통계 제공)
     * 
     * 주의: 실제 문서 수를 얻으려면 직접 데이터베이스 쿼리가 필요합니다.
     */
    fun getStatistics(): Map<String, Any> {
        // 모든 문서 검색 (매우 큰 쿼리로 모든 문서 가져오기 시도)
        val allDocs = vectorStore.similaritySearch("test") ?: emptyList()
        
        // 카테고리별 통계
        val categoryStats = allDocs
            .mapNotNull { it.metadata["category"] as? String }
            .groupingBy { it }
            .eachCount()
        
        // 타입별 통계
        val typeStats = allDocs
            .mapNotNull { it.metadata["type"] as? String }
            .groupingBy { it }
            .eachCount()
        
        // 소스별 통계
        val sourceStats = allDocs
            .mapNotNull { it.metadata["source"] as? String }
            .groupingBy { it }
            .eachCount()
        
        return mapOf(
            "estimatedDocumentCount" to allDocs.size,
            "categoryStats" to categoryStats,
            "typeStats" to typeStats,
            "sourceStats" to sourceStats,
            "note" to "정확한 문서 수는 데이터베이스 쿼리로 확인하세요."
        )
    }
}

