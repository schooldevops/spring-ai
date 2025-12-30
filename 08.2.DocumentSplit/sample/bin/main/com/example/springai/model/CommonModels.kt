package com.example.springai.model

import org.springframework.ai.document.Document

/**
 * 공통으로 사용되는 데이터 클래스들
 */
data class SplitRequest(
    val text: String,
    val strategy: String = "TOKEN",  // TOKEN, SENTENCE, PARAGRAPH
    val chunkSize: Int = 500,
    val overlap: Int = 50,
    val addToVectorStore: Boolean = true,
    val metadata: Map<String, Any> = emptyMap()
)

data class SplitResponse(
    val status: String,
    val message: String,
    val originalLength: Int,
    val chunkCount: Int,
    val strategy: String,
    val chunks: List<ChunkInfo> = emptyList()
)

data class ChunkInfo(
    val index: Int,
    val text: String,
    val length: Int,
    val metadata: Map<String, Any> = emptyMap()
)

data class DocumentSplitRequest(
    val document: Document,
    val strategy: String = "TOKEN",
    val chunkSize: Int = 500,
    val overlap: Int = 50,
    val addToVectorStore: Boolean = true
)

