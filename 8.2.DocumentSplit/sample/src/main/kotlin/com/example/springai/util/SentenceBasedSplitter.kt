package com.example.springai.util

import org.springframework.ai.document.Document

/**
 * 문장 기반 텍스트 분할기
 * 
 * 문장 단위로 문서를 분할하여 의미를 보존합니다.
 */
class SentenceBasedSplitter(
    private val maxChunkSize: Int = 500
) {
    /**
     * 문서를 문장 기반으로 분할
     */
    fun split(document: Document): List<Document> {
        val text = document.text ?: return emptyList()
        
        // 문장 분리 (간단한 예제)
        val sentenceEndings = Regex("[.!?]\\s+")
        val sentences = text.split(sentenceEndings)
        
        val chunks = mutableListOf<Document>()
        var currentChunk = StringBuilder()
        var chunkIndex = 0
        
        for (sentence in sentences) {
            val sentenceWithEnding = sentence.trim() + ". "
            
            // 현재 청크에 문장을 추가했을 때 크기 확인
            if (currentChunk.length + sentenceWithEnding.length > maxChunkSize * 4 && currentChunk.isNotEmpty()) {
                // 현재 청크 저장
                val chunkMetadata = document.metadata.toMutableMap()
                chunkMetadata["chunkIndex"] = chunkIndex
                chunkMetadata["splitStrategy"] = "sentence-based"
                chunks.add(Document(currentChunk.toString().trim(), chunkMetadata.toMap()))
                
                // 새 청크 시작
                currentChunk = StringBuilder()
                chunkIndex++
            }
            
            currentChunk.append(sentenceWithEnding)
        }
        
        // 마지막 청크 추가
        if (currentChunk.isNotEmpty()) {
            val chunkMetadata = document.metadata.toMutableMap()
            chunkMetadata["chunkIndex"] = chunkIndex
            chunkMetadata["splitStrategy"] = "sentence-based"
            chunks.add(Document(currentChunk.toString().trim(), chunkMetadata.toMap()))
        }
        
        // 총 청크 수 추가
        return chunks.mapIndexed { index, chunk ->
            val updatedMetadata = chunk.metadata.toMutableMap()
            updatedMetadata["totalChunks"] = chunks.size
            Document(chunk.text ?: "", updatedMetadata.toMap())
        }
    }
}

