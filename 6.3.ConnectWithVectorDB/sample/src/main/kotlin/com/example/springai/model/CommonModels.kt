package com.example.springai.model

/**
 * 공통으로 사용되는 데이터 클래스들
 */
data class AddDocumentRequest(
    val text: String,
    val metadata: Map<String, Any>? = null,
    val id: String? = null
)

data class BatchAddRequest(
    val texts: List<String>,
    val source: String? = null,
    val metadata: Map<String, Any>? = null
)

data class SearchRequest(
    val query: String,
    val topK: Int = 5
)

data class SearchWithMetadataRequest(
    val query: String,
    val topK: Int = 5,
    val category: String? = null,
    val metadataFilter: Map<String, Any>? = null
)

data class DeleteDocumentRequest(
    val documentIds: List<String>
)

