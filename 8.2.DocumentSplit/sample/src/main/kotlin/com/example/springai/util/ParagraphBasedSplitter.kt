package com.example.springai.util

import org.springframework.ai.document.Document

/**
 * 단락 기반 텍스트 분할기
 * 
 * 단락 단위로 문서를 분할합니다.
 */
class ParagraphBasedSplitter(
    private val maxChunkSize: Int = 1000
) {
    /**
     * 문서를 단락 기반으로 분할
     */
    fun split(document: Document): List<Document> {
        val text = document.text ?: return emptyList()
        
        // 단락 분리 (빈 줄 2개 이상)
        val paragraphs = text.split(Regex("\\n\\n+"))
        
        val chunks = mutableListOf<Document>()
        var currentChunk = StringBuilder()
        var chunkIndex = 0
        
        for (paragraph in paragraphs) {
            val paragraphWithNewline = paragraph.trim() + "\n\n"
            
            // 현재 청크에 단락을 추가했을 때 크기 확인
            if (currentChunk.length + paragraphWithNewline.length > maxChunkSize * 4 && currentChunk.isNotEmpty()) {
                // 현재 청크 저장
                val chunkMetadata = document.metadata.toMutableMap()
                chunkMetadata["chunkIndex"] = chunkIndex
                chunkMetadata["splitStrategy"] = "paragraph-based"
                chunks.add(Document(currentChunk.toString().trim(), chunkMetadata.toMap()))
                
                // 새 청크 시작
                currentChunk = StringBuilder()
                chunkIndex++
            }
            
            currentChunk.append(paragraphWithNewline)
        }
        
        // 마지막 청크 추가
        if (currentChunk.isNotEmpty()) {
            val chunkMetadata = document.metadata.toMutableMap()
            chunkMetadata["chunkIndex"] = chunkIndex
            chunkMetadata["splitStrategy"] = "paragraph-based"
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

