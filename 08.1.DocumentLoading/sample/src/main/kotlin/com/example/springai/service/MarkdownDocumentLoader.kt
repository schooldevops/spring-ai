package com.example.springai.service

import org.springframework.ai.document.Document
import org.springframework.stereotype.Service
import java.io.File

/**
 * Markdown 파일 로더
 * 
 * Markdown 파일을 로드합니다.
 */
@Service
class MarkdownDocumentLoader {
    
    /**
     * Markdown 파일 로드
     */
    fun loadMarkdownFile(filePath: String): List<Document> {
        val file = File(filePath)
        val content = file.readText()
        return listOf(Document(content, mapOf("source" to filePath, "type" to "markdown")))
    }
    
    /**
     * Markdown 파일 로드 (메타데이터 포함)
     */
    fun loadMarkdownWithMetadata(filePath: String): List<Document> {
        val file = File(filePath)
        val content = file.readText()
        return listOf(
            Document(
                content,
                mapOf(
                    "source" to filePath,
                    "type" to "markdown",
                    "fileName" to file.name,
                    "fileSize" to file.length(),
                    "loadedAt" to System.currentTimeMillis()
                )
            )
        )
    }
}

