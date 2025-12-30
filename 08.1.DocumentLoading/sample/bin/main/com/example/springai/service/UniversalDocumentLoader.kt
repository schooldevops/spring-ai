package com.example.springai.service

import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Service
import java.io.File

/**
 * 통합 문서 로더
 * 
 * 여러 형식의 문서를 지원하는 통합 로더입니다.
 */
@Service
class UniversalDocumentLoader(
    private val vectorStore: VectorStore,
    private val textDocumentLoader: TextDocumentLoader,
    private val pdfDocumentLoader: PdfDocumentLoader,
    private val markdownDocumentLoader: MarkdownDocumentLoader
) {
    
    /**
     * 파일 형식에 따라 적절한 Reader를 사용하여 문서 로드
     */
    fun loadDocument(filePath: String): List<Document> {
        val file = File(filePath)
        
        if (!file.exists()) {
            throw IllegalArgumentException("파일을 찾을 수 없습니다: $filePath")
        }
        
        if (!file.isFile) {
            throw IllegalArgumentException("파일이 아닙니다: $filePath")
        }
        
        val extension = file.extension.lowercase()
        
        val documents = when (extension) {
            "txt" -> {
                textDocumentLoader.loadWithMetadata(filePath)
            }
            "pdf" -> {
                pdfDocumentLoader.loadPdfWithMetadata(filePath)
            }
            "md", "markdown" -> {
                markdownDocumentLoader.loadMarkdownWithMetadata(filePath)
            }
            else -> {
                throw UnsupportedOperationException("지원하지 않는 파일 형식: $extension")
            }
        }
        
        return documents
    }
    
    /**
     * 문서를 로드하고 VectorStore에 추가
     */
    fun loadAndStore(filePath: String): Map<String, Any> {
        return try {
            val documents = loadDocument(filePath)
            vectorStore.add(documents)
            
            mapOf(
                "status" to "success",
                "message" to "문서가 성공적으로 로드되고 저장되었습니다.",
                "filePath" to filePath,
                "documentCount" to documents.size
            )
        } catch (e: Exception) {
            mapOf(
                "status" to "error",
                "message" to "문서 로드 실패: ${e.message}",
                "filePath" to filePath
            )
        }
    }
    
    /**
     * 디렉토리 내의 모든 문서를 로드
     */
    fun loadDirectory(directoryPath: String): List<Document> {
        val directory = File(directoryPath)
        if (!directory.isDirectory) {
            throw IllegalArgumentException("디렉토리가 아닙니다: $directoryPath")
        }
        
        val allDocuments = mutableListOf<Document>()
        
        directory.walkTopDown().forEach { file ->
            if (file.isFile) {
                try {
                    val documents = loadDocument(file.absolutePath)
                    allDocuments.addAll(documents)
                } catch (e: Exception) {
                    println("파일 로드 실패: ${file.name} - ${e.message}")
                }
            }
        }
        
        return allDocuments
    }
    
    /**
     * 디렉토리 내의 모든 문서를 로드하고 VectorStore에 추가
     */
    fun loadAndStoreDirectory(directoryPath: String): Map<String, Any> {
        return try {
            val documents = loadDirectory(directoryPath)
            vectorStore.add(documents)
            
            mapOf(
                "status" to "success",
                "message" to "디렉토리 내의 모든 문서가 성공적으로 로드되고 저장되었습니다.",
                "directoryPath" to directoryPath,
                "documentCount" to documents.size
            )
        } catch (e: Exception) {
            mapOf(
                "status" to "error",
                "message" to "디렉토리 로드 실패: ${e.message}",
                "directoryPath" to directoryPath
            )
        }
    }
}

