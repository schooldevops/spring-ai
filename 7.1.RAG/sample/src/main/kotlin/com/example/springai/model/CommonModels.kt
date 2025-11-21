package com.example.springai.model

/**
 * 공통으로 사용되는 데이터 클래스들
 */
data class RAGRequest(
    val question: String,
    val topK: Int = 3
)

data class RAGResponse(
    val question: String,
    val answer: String,
    val sources: List<DocumentSource> = emptyList(),
    val context: String? = null
)

data class DocumentSource(
    val content: String,
    val metadata: Map<String, Any> = emptyMap()
)

data class AddDocumentRequest(
    val text: String,
    val title: String? = null,
    val source: String? = null,
    val metadata: Map<String, Any>? = null
)

