package com.example.springai.service

import com.example.springai.model.DocumentSource
import com.example.springai.model.RAGResult
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Service

/**
 * PromptTemplate을 사용한 RAG 서비스
 * 
 * PromptTemplate을 활용하여 Context를 동적으로 주입하는 RAG 파이프라인입니다.
 */
@Service
class PromptTemplateRAGService(
    private val vectorStore: VectorStore,
    private val chatModel: ChatModel
) {
    // 기본 RAG 템플릿
    private val basicRAGTemplate = PromptTemplate("""
        다음 문서를 참고하여 질문에 답변해주세요:
        
        {context}
        
        질문: {question}
        
        답변 시 주의사항:
        - 문서에 명시된 내용만 답변하세요
        - 문서에 없는 내용은 "문서에 해당 정보가 없습니다"라고 답변하세요
    """.trimIndent())
    
    // 구조화된 RAG 템플릿
    private val structuredRAGTemplate = PromptTemplate("""
        당신은 전문가입니다. 다음 문서를 참고하여 질문에 정확하게 답변해주세요.
        
        === 참고 문서 ===
        {context}
        ================
        
        질문: {question}
        
        답변 형식:
        1. 핵심 답변
        2. 상세 설명
        3. 관련 정보
    """.trimIndent())
    
    // 역할 기반 RAG 템플릿
    private val roleBasedRAGTemplate = PromptTemplate("""
        역할: {role}
        
        다음 문서를 참고하여 {tone}한 톤으로 질문에 답변해주세요:
        
        {context}
        
        질문: {question}
    """.trimIndent())
    
    /**
     * 기본 RAG 질문 답변
     */
    fun ask(question: String, topK: Int = 3): RAGResult {
        // 1. Retrieval: 문서 검색
        val documents = vectorStore.similaritySearch(question) ?: emptyList()
        val topDocuments = documents.take(topK)
        
        // 2. Augmentation: Context 생성
        val context = topDocuments.joinToString("\n\n---\n\n") { doc ->
            doc.text ?: ""
        }
        
        // 3. PromptTemplate에 Context와 질문 주입
        val prompt = basicRAGTemplate.create(mapOf(
            "context" to context,
            "question" to question
        ))
        
        // 4. Generation: LLM 호출
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
    
    /**
     * 구조화된 RAG 질문 답변
     */
    fun askStructured(question: String, topK: Int = 3): RAGResult {
        val documents = vectorStore.similaritySearch(question) ?: emptyList()
        val topDocuments = documents.take(topK)
        
        val context = topDocuments.joinToString("\n\n---\n\n") { doc ->
            val title = doc.metadata["title"] as? String ?: "문서"
            "[$title]\n${doc.text}"
        }
        
        val prompt = structuredRAGTemplate.create(mapOf(
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
    
    /**
     * 역할 기반 RAG 질문 답변
     */
    fun askWithRole(
        question: String,
        role: String = "고객 지원 담당자",
        tone: String = "친절",
        topK: Int = 3
    ): RAGResult {
        val documents = vectorStore.similaritySearch(question) ?: emptyList()
        val topDocuments = documents.take(topK)
        
        val context = topDocuments.joinToString("\n\n---\n\n") { doc ->
            doc.text ?: ""
        }
        
        val prompt = roleBasedRAGTemplate.create(mapOf(
            "role" to role,
            "tone" to tone,
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

