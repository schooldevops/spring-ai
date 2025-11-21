package com.example.springai.service

import org.springframework.ai.document.Document
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import java.io.File

/**
 * 텍스트 파일 로더
 * 
 * 텍스트 파일을 로드하여 Document로 변환합니다.
 */
@Service
class TextDocumentLoader {
    
    /**
     * 파일 시스템에서 텍스트 파일 로드
     */
    fun loadFromFileSystem(filePath: String): List<Document> {
        val file = File(filePath)
        val content = file.readText()
        return listOf(Document(content, mapOf("source" to filePath, "type" to "text")))
    }
    
    /**
     * 클래스패스에서 텍스트 파일 로드
     */
    fun loadFromClasspath(filePath: String): List<Document> {
        val resource: Resource = ClassPathResource(filePath)
        val content = resource.inputStream.bufferedReader().use { it.readText() }
        return listOf(Document(content, mapOf("source" to filePath, "type" to "text")))
    }
    
    /**
     * URL에서 텍스트 파일 로드
     */
    fun loadFromUrl(url: String): List<Document> {
        val resource: Resource = UrlResource(url)
        val content = resource.inputStream.bufferedReader().use { it.readText() }
        return listOf(Document(content, mapOf("source" to url, "type" to "text")))
    }
    
    /**
     * 메타데이터를 포함하여 텍스트 파일 로드
     */
    fun loadWithMetadata(filePath: String): List<Document> {
        val file = File(filePath)
        val content = file.readText()
        return listOf(
            Document(
                content,
                mapOf(
                    "source" to filePath,
                    "type" to "text",
                    "fileName" to file.name,
                    "fileSize" to file.length(),
                    "loadedAt" to System.currentTimeMillis()
                )
            )
        )
    }
}

