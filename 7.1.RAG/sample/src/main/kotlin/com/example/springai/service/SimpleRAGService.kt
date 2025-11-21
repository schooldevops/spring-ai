package com.example.springai.service

import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Service

/**
 * 간단한 RAG 서비스
 * 
 * RAG 패턴의 기본 동작을 보여주는 서비스입니다.
 * 1. 질문에 대한 관련 문서 검색
 * 2. 검색된 문서를 Context로 변환
 * 3. Context와 질문을 LLM에 전달
 * 4. 응답 생성
 */
@Service
class SimpleRAGService(
    private val vectorStore: VectorStore,
    private val chatModel: ChatModel
) {
    /**
     * RAG를 사용한 질문 답변
     * 
     * @param question 사용자 질문
     * @param topK 검색할 문서 수 (기본값: 3)
     * @return RAG 응답 (답변, 출처, 컨텍스트)
     */
    fun ask(question: String, topK: Int = 3): RAGResult {
        // 1. 관련 문서 검색 (Retrieval)
        val documents = vectorStore.similaritySearch(question) ?: emptyList()
        val topDocuments = documents.take(topK)
        
        // 2. 문서를 Context로 변환 (Augmentation)
        val context = if (topDocuments.isNotEmpty()) {
            topDocuments.joinToString("\n\n---\n\n") { doc ->
                val title = doc.metadata["title"] as? String ?: "문서"
                """
                [문서: $title]
                ${doc.text}
                """.trimIndent()
            }
        } else {
            "관련 문서를 찾을 수 없습니다."
        }
        
        // 3. Context와 질문을 LLM에 전달 (Generation)
        val prompt = Prompt(
            listOf(
                SystemMessage("""
                    당신은 도움이 되는 AI 어시스턴트입니다.
                    제공된 문서를 참고하여 질문에 정확하게 답변해주세요.
                    문서에 없는 내용은 추측하지 말고, "문서에 해당 정보가 없습니다"라고 답변해주세요.
                """.trimIndent()),
                UserMessage("""
                    참고 문서:
                    $context
                    
                    질문: $question
                """.trimIndent())
            )
        )
        
        // 4. LLM 호출
        val response = chatModel.call(prompt)
        val answer = response.results.firstOrNull()?.output?.text ?: "답변을 생성할 수 없습니다."
        
        // 5. 출처 정보 수집
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

data class RAGResult(
    val question: String,
    val answer: String,
    val sources: List<DocumentSource>,
    val context: String
)

data class DocumentSource(
    val content: String,
    val metadata: Map<String, Any> = emptyMap()
)

