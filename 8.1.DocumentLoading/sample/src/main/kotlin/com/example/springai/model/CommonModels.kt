package com.example.springai.model

/**
 * 공통으로 사용되는 데이터 클래스들
 */
data class LoadDocumentRequest(
    val filePath: String,
    val addToVectorStore: Boolean = true
)

data class LoadDirectoryRequest(
    val directoryPath: String,
    val addToVectorStore: Boolean = true
)

data class DocumentLoadResponse(
    val status: String,
    val message: String,
    val filePath: String? = null,
    val documentCount: Int = 0,
    val documents: List<DocumentInfo> = emptyList()
)

data class DocumentInfo(
    val text: String,
    val metadata: Map<String, Any> = emptyMap()
)

