package com.example.springai.util

import org.springframework.ai.document.Document

/**
 * 토큰 기반 텍스트 분할기
 * 
 * 간단한 토큰 추정을 사용하여 문서를 분할합니다.
 * 실제로는 tiktoken 같은 라이브러리를 사용하는 것이 더 정확합니다.
 */
class TokenTextSplitter(
    private val chunkSize: Int = 500,
    private val chunkOverlap: Int = 50
) {
    /**
     * 문서를 청크로 분할
     * 
     * 참고: 실제 토큰 수는 모델에 따라 다르지만,
     * 여기서는 간단한 추정을 사용합니다 (1 토큰 ≈ 4 문자).
     */
    fun apply(document: Document): List<Document> {
        val text = document.text ?: return emptyList()
        
        // 간단한 토큰 추정 (1 토큰 ≈ 4 문자)
        val estimatedTokens = text.length / 4
        val chunkSizeInChars = chunkSize * 4
        val overlapInChars = chunkOverlap * 4
        
        val chunks = mutableListOf<Document>()
        var start = 0
        
        while (start < text.length) {
            val end = minOf(start + chunkSizeInChars, text.length)
            val chunkText = text.substring(start, end)
            
            // 청크 메타데이터 생성
            val chunkMetadata = document.metadata.toMutableMap()
            chunkMetadata["chunkIndex"] = chunks.size
            chunkMetadata["chunkStart"] = start
            chunkMetadata["chunkEnd"] = end
            chunkMetadata["chunkLength"] = chunkText.length
            
            chunks.add(Document(chunkText, chunkMetadata.toMap()))
            
            // 다음 청크 시작 위치 (오버랩 고려)
            start = end - overlapInChars
            if (start >= text.length) break
        }
        
        // 총 청크 수를 각 청크 메타데이터에 추가
        return chunks.mapIndexed { index, chunk ->
            val updatedMetadata = chunk.metadata.toMutableMap()
            updatedMetadata["totalChunks"] = chunks.size
            Document(chunk.text ?: "", updatedMetadata.toMap())
        }
    }
    
    /**
     * 여러 문서를 분할
     */
    fun apply(documents: List<Document>): List<Document> {
        return documents.flatMap { apply(it) }
    }
}

