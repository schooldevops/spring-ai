package com.example.springai.service

import com.example.springai.model.DocumentSource
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

/**
 * 대화 이력을 지원하는 RAG 서비스
 * 
 * 멀티 턴 대화를 지원하여 이전 대화 내용을 참고할 수 있습니다.
 */
@Service
class ConversationRAGService(
    private val vectorStore: VectorStore,
    private val chatModel: ChatModel
) {
    // 대화 이력 저장 (실제로는 Redis 등 사용 권장)
    private val conversationHistory = ConcurrentHashMap<String, MutableList<Pair<String, String>>>()
    
    private val conversationRAGTemplate = PromptTemplate("""
        다음 문서를 참고하여 질문에 답변해주세요:
        
        {context}
        
        이전 대화:
        {history}
        
        현재 질문: {question}
        
        답변:
    """.trimIndent())
    
    /**
     * 대화 이력을 포함한 RAG 질문 답변
     */
    fun ask(
        question: String,
        conversationId: String,
        topK: Int = 3,
        maxHistory: Int = 3
    ): ConversationRAGResult {
        // 1. Retrieval: 문서 검색
        val documents = vectorStore.similaritySearch(question) ?: emptyList()
        val topDocuments = documents.take(topK)
        
        // 2. Augmentation: Context 생성
        val context = topDocuments.joinToString("\n\n---\n\n") { doc ->
            val title = doc.metadata["title"] as? String ?: "문서"
            "[$title]\n${doc.text}"
        }
        
        // 3. 대화 이력 가져오기
        val history = conversationHistory[conversationId]?.takeLast(maxHistory) ?: emptyList()
        val historyText = if (history.isNotEmpty()) {
            history.joinToString("\n") { (q, a) ->
                "Q: $q\nA: $a"
            }
        } else {
            "이전 대화가 없습니다."
        }
        
        // 4. PromptTemplate에 주입
        val prompt = conversationRAGTemplate.create(mapOf(
            "context" to context,
            "history" to historyText,
            "question" to question
        ))
        
        // 5. Generation: LLM 호출
        val response = chatModel.call(prompt)
        val answer = response.results.firstOrNull()?.output?.text ?: "답변을 생성할 수 없습니다."
        
        // 6. 대화 이력 업데이트
        conversationHistory.getOrPut(conversationId) { mutableListOf() }.add(question to answer)
        
        val sources = topDocuments.map { doc ->
            DocumentSource(
                content = doc.text ?: "",
                metadata = doc.metadata
            )
        }
        
        return ConversationRAGResult(
            question = question,
            answer = answer,
            conversationId = conversationId,
            sources = sources
        )
    }
    
    /**
     * 대화 이력 초기화
     */
    fun clearHistory(conversationId: String) {
        conversationHistory.remove(conversationId)
    }
    
    /**
     * 대화 이력 조회
     */
    fun getHistory(conversationId: String): List<Pair<String, String>> {
        return conversationHistory[conversationId] ?: emptyList()
    }
}

data class ConversationRAGResult(
    val question: String,
    val answer: String,
    val conversationId: String,
    val sources: List<DocumentSource>
)

