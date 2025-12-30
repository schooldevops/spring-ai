package com.example.springai.controller

import com.example.springai.model.ChunkInfo
import com.example.springai.model.DocumentSplitRequest
import com.example.springai.model.SplitRequest
import com.example.springai.model.SplitResponse
import com.example.springai.service.DocumentSplitService
import com.example.springai.service.SplitStrategy
import org.springframework.ai.document.Document
import org.springframework.web.bind.annotation.*

/**
 * 문서 분할 컨트롤러
 * 
 * 문서를 청크로 분할하는 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/split")
class DocumentSplitController(
    private val documentSplitService: DocumentSplitService
) {
    
    /**
     * 텍스트를 청크로 분할
     * POST http://localhost:8080/api/split/text
     */
    @PostMapping("/text")
    fun splitText(@RequestBody request: SplitRequest): SplitResponse {
        return try {
            val strategy = when (request.strategy.uppercase()) {
                "TOKEN" -> SplitStrategy.TOKEN
                "SENTENCE" -> SplitStrategy.SENTENCE
                "PARAGRAPH" -> SplitStrategy.PARAGRAPH
                else -> SplitStrategy.TOKEN
            }
            
            val document = Document(request.text, request.metadata)
            
            val result = if (request.addToVectorStore) {
                documentSplitService.splitAndStore(
                    document = document,
                    strategy = strategy,
                    chunkSize = request.chunkSize,
                    overlap = request.overlap
                )
            } else {
                val chunks = when (strategy) {
                    SplitStrategy.TOKEN -> documentSplitService.splitByToken(document, request.chunkSize, request.overlap)
                    SplitStrategy.SENTENCE -> documentSplitService.splitBySentence(document, request.chunkSize)
                    SplitStrategy.PARAGRAPH -> documentSplitService.splitByParagraph(document, request.chunkSize)
                }
                com.example.springai.service.SplitResult(
                    originalDocument = document,
                    chunks = chunks,
                    strategy = strategy,
                    chunkSize = request.chunkSize,
                    overlap = request.overlap
                )
            }
            
            SplitResponse(
                status = "success",
                message = "문서가 성공적으로 분할되었습니다.",
                originalLength = request.text.length,
                chunkCount = result.chunks.size,
                strategy = request.strategy,
                chunks = result.chunks.mapIndexed { index, chunk ->
                    ChunkInfo(
                        index = index,
                        text = chunk.text ?: "",
                        length = chunk.text?.length ?: 0,
                        metadata = chunk.metadata
                    )
                }
            )
        } catch (e: Exception) {
            SplitResponse(
                status = "error",
                message = "문서 분할 실패: ${e.message}",
                originalLength = request.text.length,
                chunkCount = 0,
                strategy = request.strategy
            )
        }
    }
    
    /**
     * Document 객체를 청크로 분할
     * POST http://localhost:8080/api/split/document
     */
    @PostMapping("/document")
    fun splitDocument(@RequestBody request: DocumentSplitRequest): SplitResponse {
        return try {
            val strategy = when (request.strategy.uppercase()) {
                "TOKEN" -> SplitStrategy.TOKEN
                "SENTENCE" -> SplitStrategy.SENTENCE
                "PARAGRAPH" -> SplitStrategy.PARAGRAPH
                else -> SplitStrategy.TOKEN
            }
            
            val result = if (request.addToVectorStore) {
                documentSplitService.splitAndStore(
                    document = request.document,
                    strategy = strategy,
                    chunkSize = request.chunkSize,
                    overlap = request.overlap
                )
            } else {
                val chunks = when (strategy) {
                    SplitStrategy.TOKEN -> documentSplitService.splitByToken(request.document, request.chunkSize, request.overlap)
                    SplitStrategy.SENTENCE -> documentSplitService.splitBySentence(request.document, request.chunkSize)
                    SplitStrategy.PARAGRAPH -> documentSplitService.splitByParagraph(request.document, request.chunkSize)
                }
                com.example.springai.service.SplitResult(
                    originalDocument = request.document,
                    chunks = chunks,
                    strategy = strategy,
                    chunkSize = request.chunkSize,
                    overlap = request.overlap
                )
            }
            
            SplitResponse(
                status = "success",
                message = "문서가 성공적으로 분할되었습니다.",
                originalLength = request.document.text?.length ?: 0,
                chunkCount = result.chunks.size,
                strategy = request.strategy,
                chunks = result.chunks.mapIndexed { index, chunk ->
                    ChunkInfo(
                        index = index,
                        text = chunk.text ?: "",
                        length = chunk.text?.length ?: 0,
                        metadata = chunk.metadata
                    )
                }
            )
        } catch (e: Exception) {
            SplitResponse(
                status = "error",
                message = "문서 분할 실패: ${e.message}",
                originalLength = request.document.text?.length ?: 0,
                chunkCount = 0,
                strategy = request.strategy
            )
        }
    }
}

