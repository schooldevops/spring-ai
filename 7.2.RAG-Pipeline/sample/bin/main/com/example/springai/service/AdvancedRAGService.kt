package com.example.springai.service

import com.example.springai.model.DocumentSource
import com.example.springai.model.RAGResult
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Service

/**
 * 고급 RAG 서비스
 * 
 * 재랭킹, Context 길이 제한 등 고급 기능을 포함한 RAG 서비스입니다.
 */
@Service
class AdvancedRAGService(
    private val vectorStore: VectorStore,
    private val chatModel: ChatModel
) {
    private val ragTemplate = PromptTemplate("""
        다음 문서를 참고하여 질문에 답변해주세요:
        
        {context}
        
        질문: {question}
    """.trimIndent())
    
    /**
     * 재랭킹을 포함한 RAG
     * 
     * 더 많은 문서를 검색한 후, 질문과의 관련성을 재평가하여 최종 문서를 선택합니다.
     */
    fun askWithReranking(question: String, topK: Int = 5, finalK: Int = 3): RAGResult {
        // 1. Retrieval: 더 많은 문서 검색
        val documents = vectorStore.similaritySearch(question) ?: emptyList()
        val topDocuments = documents.take(topK)
        
        // 2. Re-ranking: 질문과의 관련성 재평가
        val questionWords = question.lowercase().split(" ").filter { it.length > 2 }
        val reranked = topDocuments.sortedByDescending { doc ->
            val docText = (doc.text ?: "").lowercase()
            // 간단한 키워드 매칭으로 관련성 점수 계산
            questionWords.count { it in docText }
        }
        
        // 3. Augmentation: 상위 finalK개만 Context로 변환
        val finalDocuments = reranked.take(finalK)
        val context = finalDocuments.joinToString("\n\n---\n\n") { doc ->
            val title = doc.metadata["title"] as? String ?: "문서"
            "[$title]\n${doc.text}"
        }
        
        // 4. Generation
        val prompt = ragTemplate.create(mapOf(
            "context" to context,
            "question" to question
        ))
        val response = chatModel.call(prompt)
        val answer = response.results.firstOrNull()?.output?.text ?: ""
        
        val sources = finalDocuments.map { doc ->
            DocumentSource(
                content = doc.text ?: "",
                metadata = doc.metadata
            )
        }
        
        return RAGResult(
            question = question,
            answer = answer,
            sources = sources,
            context = context
        )
    }
    
    /**
     * Context 길이 제한을 포함한 RAG
     */
    fun askWithLengthLimit(question: String, topK: Int = 5, maxLength: Int = 2000): RAGResult {
        val documents = vectorStore.similaritySearch(question) ?: emptyList()
        
        // Context 길이 제한
        var context = ""
        val selectedDocuments = mutableListOf<Document>()
        
        for (doc in documents.take(topK)) {
            val docText = doc.text ?: ""
            val newContext = if (context.isEmpty()) {
                docText
            } else {
                "$context\n\n---\n\n$docText"
            }
            
            if (newContext.length <= maxLength) {
                context = newContext
                selectedDocuments.add(doc)
            } else {
                break
            }
        }
        
        val prompt = ragTemplate.create(mapOf(
            "context" to context,
            "question" to question
        ))
        
        val response = chatModel.call(prompt)
        val answer = response.results.firstOrNull()?.output?.text ?: ""
        
        val sources = selectedDocuments.map { doc ->
            DocumentSource(
                content = doc.text ?: "",
                metadata = doc.metadata
            )
        }
        
        return RAGResult(
            question = question,
            answer = answer,
            sources = sources,
            context = context
        )
    }
    
    /**
     * 카테고리 필터링을 포함한 RAG
     */
    fun askWithCategory(question: String, category: String, topK: Int = 3): RAGResult {
        // 1. Retrieval: 모든 문서 검색
        val documents = vectorStore.similaritySearch(question) ?: emptyList()
        
        // 2. 카테고리 필터링
        val filtered = documents.filter { doc ->
            doc.metadata["category"] == category
        }
        
        val topDocuments = filtered.take(topK)
        
        // 3. Augmentation
        val context = topDocuments.joinToString("\n\n---\n\n") { doc ->
            val title = doc.metadata["title"] as? String ?: "문서"
            "[$title]\n${doc.text}"
        }
        
        // 4. Generation
        val prompt = ragTemplate.create(mapOf(
            "context" to context,
            "question" to question
        ))
        
        val response = chatModel.call(prompt)
        val answer = response.results.firstOrNull()?.output?.text ?: ""
        
        val sources = topDocuments.map { doc ->
            DocumentSource(
                content = doc.text ?: "",
                metadata = doc.metadata
            )
        }
        
        return RAGResult(
            question = question,
            answer = answer,
            sources = sources,
            context = context
        )
    }
}

