package com.example.springai.service

import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Service

/**
 * 문서 관리 서비스
 */
@Service
class DocumentManagementService(
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
    fun addDocuments(texts: List<String>, source: String = "unknown"): Int {
        val documents = texts.mapIndexed { index, text ->
            Document(
                text,
                mapOf(
                    "index" to index,
                    "source" to source,
                    "addedAt" to System.currentTimeMillis()
                )
            )
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
     * 문서 통계
     */
    fun getStats(): Map<String, Any> {
        // SimpleVectorStore는 직접적인 통계 메서드가 없으므로
        // 검색을 통해 간접적으로 확인
        val testResults: List<Document> = vectorStore.similaritySearch("test") ?: emptyList()
        
        return mapOf(
            "estimatedDocumentCount" to testResults.size,
            "note" to "SimpleVectorStore는 정확한 문서 수를 제공하지 않습니다."
        )
    }
}

