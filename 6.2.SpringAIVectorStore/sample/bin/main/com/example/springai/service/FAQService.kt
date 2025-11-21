package com.example.springai.service

import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Service

/**
 * FAQ 서비스 예제
 */
@Service
class FAQService(
    private val vectorStore: VectorStore
) {
    /**
     * FAQ 추가
     */
    fun addFAQ(question: String, answer: String, category: String) {
        val document = Document(
            "Q: $question\nA: $answer",
            mapOf(
                "question" to question,
                "answer" to answer,
                "category" to category,
                "type" to "faq",
                "createdAt" to System.currentTimeMillis()
            )
        )
        
        vectorStore.add(listOf(document))
    }
    
    /**
     * FAQ 검색
     */
    fun searchFAQ(query: String, category: String? = null, topK: Int = 3): List<FAQResult> {
        val results = vectorStore.similaritySearch(query) ?: emptyList()
        
        val filtered = if (category != null) {
            results.filter { doc ->
                doc.metadata["type"] == "faq" && doc.metadata["category"] == category
            }
        } else {
            results.filter { doc ->
                doc.metadata["type"] == "faq"
            }
        }
        
        return filtered.take(topK).mapNotNull { doc ->
            FAQResult(
                question = (doc.metadata["question"] as? String) ?: "",
                answer = (doc.metadata["answer"] as? String) ?: "",
                category = (doc.metadata["category"] as? String) ?: ""
            )
        }
    }
}

data class FAQResult(
    val question: String,
    val answer: String,
    val category: String
)

