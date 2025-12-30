package com.example.springai.service

import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Service

/**
 * 지식베이스 서비스 예제
 */
@Service
class KnowledgeBaseService(
    private val vectorStore: VectorStore
) {
    /**
     * 지식 추가
     */
    fun addKnowledge(title: String, content: String, topic: String) {
        val document = Document(
            "$title\n\n$content",
            mapOf(
                "title" to title,
                "topic" to topic,
                "type" to "knowledge",
                "createdAt" to System.currentTimeMillis()
            )
        )
        
        vectorStore.add(listOf(document))
    }
    
    /**
     * 주제별 지식 검색
     */
    fun searchKnowledge(query: String, topic: String? = null, topK: Int = 3): List<KnowledgeResult> {
        val results = vectorStore.similaritySearch(query) ?: emptyList()
        
        val filtered = if (topic != null) {
            results.filter { doc ->
                doc.metadata["topic"] == topic && doc.metadata["type"] == "knowledge"
            }
        } else {
            results.filter { doc ->
                doc.metadata["type"] == "knowledge"
            }
        }
        
        return filtered.take(topK).mapNotNull { doc ->
            KnowledgeResult(
                title = (doc.metadata["title"] as? String) ?: "",
                content = doc.text ?: "",
                topic = (doc.metadata["topic"] as? String) ?: ""
            )
        }
    }
}

data class KnowledgeResult(
    val title: String,
    val content: String,
    val topic: String
)

