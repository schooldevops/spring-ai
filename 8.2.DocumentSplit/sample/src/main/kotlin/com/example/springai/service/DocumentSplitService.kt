package com.example.springai.service

import com.example.springai.util.ParagraphBasedSplitter
import com.example.springai.util.SentenceBasedSplitter
import com.example.springai.util.TokenTextSplitter
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Service

/**
 * 문서 분할 서비스
 * 
 * 다양한 전략을 사용하여 문서를 분할합니다.
 */
@Service
class DocumentSplitService(
    private val vectorStore: VectorStore
) {
    /**
     * 토큰 기반 분할
     */
    fun splitByToken(
        document: Document,
        chunkSize: Int = 500,
        overlap: Int = 50
    ): List<Document> {
        val splitter = TokenTextSplitter(
            chunkSize = chunkSize,
            chunkOverlap = overlap
        )
        return splitter.apply(document)
    }
    
    /**
     * 문장 기반 분할
     */
    fun splitBySentence(
        document: Document,
        maxChunkSize: Int = 500
    ): List<Document> {
        val splitter = SentenceBasedSplitter(maxChunkSize = maxChunkSize)
        return splitter.split(document)
    }
    
    /**
     * 단락 기반 분할
     */
    fun splitByParagraph(
        document: Document,
        maxChunkSize: Int = 1000
    ): List<Document> {
        val splitter = ParagraphBasedSplitter(maxChunkSize = maxChunkSize)
        return splitter.split(document)
    }
    
    /**
     * 분할하고 VectorStore에 추가
     */
    fun splitAndStore(
        document: Document,
        strategy: SplitStrategy = SplitStrategy.TOKEN,
        chunkSize: Int = 500,
        overlap: Int = 50
    ): SplitResult {
        val chunks = when (strategy) {
            SplitStrategy.TOKEN -> splitByToken(document, chunkSize, overlap)
            SplitStrategy.SENTENCE -> splitBySentence(document, chunkSize)
            SplitStrategy.PARAGRAPH -> splitByParagraph(document, chunkSize)
        }
        
        // 원본 메타데이터 보존
        val chunksWithMetadata = chunks.mapIndexed { index, chunk ->
            Document(
                chunk.text ?: "",
                (document.metadata + chunk.metadata + mapOf(
                    "originalSource" to (document.metadata["source"] as? String ?: "unknown"),
                    "splitStrategy" to strategy.name.lowercase()
                )).toMap()
            )
        }
        
        // VectorStore에 추가
        vectorStore.add(chunksWithMetadata)
        
        return SplitResult(
            originalDocument = document,
            chunks = chunksWithMetadata,
            strategy = strategy,
            chunkSize = chunkSize,
            overlap = overlap
        )
    }
    
    /**
     * 여러 문서를 분할하고 추가
     */
    fun splitAndStoreBatch(
        documents: List<Document>,
        strategy: SplitStrategy = SplitStrategy.TOKEN,
        chunkSize: Int = 500,
        overlap: Int = 50
    ): BatchSplitResult {
        val allChunks = mutableListOf<Document>()
        
        documents.forEach { document ->
            val chunks = when (strategy) {
                SplitStrategy.TOKEN -> splitByToken(document, chunkSize, overlap)
                SplitStrategy.SENTENCE -> splitBySentence(document, chunkSize)
                SplitStrategy.PARAGRAPH -> splitByParagraph(document, chunkSize)
            }
            
            val chunksWithMetadata = chunks.mapIndexed { index, chunk ->
                Document(
                    chunk.text ?: "",
                    (document.metadata + chunk.metadata + mapOf(
                        "originalSource" to (document.metadata["source"] as? String ?: "unknown"),
                        "splitStrategy" to strategy.name.lowercase()
                    )).toMap()
                )
            }
            
            allChunks.addAll(chunksWithMetadata)
        }
        
        vectorStore.add(allChunks)
        
        return BatchSplitResult(
            originalDocumentCount = documents.size,
            totalChunks = allChunks.size,
            strategy = strategy,
            averageChunkSize = allChunks.map { it.text?.length ?: 0 }.average()
        )
    }
}

enum class SplitStrategy {
    TOKEN,      // 토큰 기반 분할
    SENTENCE,   // 문장 기반 분할
    PARAGRAPH   // 단락 기반 분할
}

data class SplitResult(
    val originalDocument: Document,
    val chunks: List<Document>,
    val strategy: SplitStrategy,
    val chunkSize: Int,
    val overlap: Int
)

data class BatchSplitResult(
    val originalDocumentCount: Int,
    val totalChunks: Int,
    val strategy: SplitStrategy,
    val averageChunkSize: Double
)

