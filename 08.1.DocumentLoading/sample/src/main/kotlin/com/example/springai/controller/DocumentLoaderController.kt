package com.example.springai.controller

import com.example.springai.model.DocumentInfo
import com.example.springai.model.DocumentLoadResponse
import com.example.springai.model.LoadDirectoryRequest
import com.example.springai.model.LoadDocumentRequest
import com.example.springai.service.MarkdownDocumentLoader
import com.example.springai.service.PdfDocumentLoader
import com.example.springai.service.TextDocumentLoader
import com.example.springai.service.UniversalDocumentLoader
import org.springframework.web.bind.annotation.*

/**
 * 문서 로더 컨트롤러
 * 
 * 다양한 형식의 문서를 로드하는 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/documents")
class DocumentLoaderController(
    private val textDocumentLoader: TextDocumentLoader,
    private val pdfDocumentLoader: PdfDocumentLoader,
    private val markdownDocumentLoader: MarkdownDocumentLoader,
    private val universalDocumentLoader: UniversalDocumentLoader
) {
    
    /**
     * 텍스트 파일 로드
     * POST http://localhost:8080/api/documents/load-text
     */
    @PostMapping("/load-text")
    fun loadTextFile(@RequestBody request: LoadDocumentRequest): DocumentLoadResponse {
        return try {
            val documents = textDocumentLoader.loadWithMetadata(request.filePath)
            
            if (request.addToVectorStore) {
                universalDocumentLoader.loadAndStore(request.filePath)
            }
            
            DocumentLoadResponse(
                status = "success",
                message = "텍스트 파일이 성공적으로 로드되었습니다.",
                filePath = request.filePath,
                documentCount = documents.size,
                documents = documents.map { doc ->
                    DocumentInfo(
                        text = doc.text ?: "",
                        metadata = doc.metadata
                    )
                }
            )
        } catch (e: Exception) {
            DocumentLoadResponse(
                status = "error",
                message = "텍스트 파일 로드 실패: ${e.message}",
                filePath = request.filePath
            )
        }
    }
    
    /**
     * PDF 파일 로드
     * POST http://localhost:8080/api/documents/load-pdf
     */
    @PostMapping("/load-pdf")
    fun loadPdfFile(@RequestBody request: LoadDocumentRequest): DocumentLoadResponse {
        return try {
            val documents = pdfDocumentLoader.loadPdfWithMetadata(request.filePath)
            
            if (request.addToVectorStore) {
                universalDocumentLoader.loadAndStore(request.filePath)
            }
            
            DocumentLoadResponse(
                status = "success",
                message = "PDF 파일이 성공적으로 로드되었습니다.",
                filePath = request.filePath,
                documentCount = documents.size,
                documents = documents.map { doc ->
                    DocumentInfo(
                        text = doc.text ?: "",
                        metadata = doc.metadata
                    )
                }
            )
        } catch (e: Exception) {
            DocumentLoadResponse(
                status = "error",
                message = "PDF 파일 로드 실패: ${e.message}",
                filePath = request.filePath
            )
        }
    }
    
    /**
     * Markdown 파일 로드
     * POST http://localhost:8080/api/documents/load-markdown
     */
    @PostMapping("/load-markdown")
    fun loadMarkdownFile(@RequestBody request: LoadDocumentRequest): DocumentLoadResponse {
        return try {
            val documents = markdownDocumentLoader.loadMarkdownWithMetadata(request.filePath)
            
            if (request.addToVectorStore) {
                universalDocumentLoader.loadAndStore(request.filePath)
            }
            
            DocumentLoadResponse(
                status = "success",
                message = "Markdown 파일이 성공적으로 로드되었습니다.",
                filePath = request.filePath,
                documentCount = documents.size,
                documents = documents.map { doc ->
                    DocumentInfo(
                        text = doc.text ?: "",
                        metadata = doc.metadata
                    )
                }
            )
        } catch (e: Exception) {
            DocumentLoadResponse(
                status = "error",
                message = "Markdown 파일 로드 실패: ${e.message}",
                filePath = request.filePath
            )
        }
    }
    
    /**
     * 통합 문서 로드 (파일 형식 자동 감지)
     * POST http://localhost:8080/api/documents/load
     */
    @PostMapping("/load")
    fun loadDocument(@RequestBody request: LoadDocumentRequest): DocumentLoadResponse {
        return try {
            val documents = universalDocumentLoader.loadDocument(request.filePath)
            
            if (request.addToVectorStore) {
                universalDocumentLoader.loadAndStore(request.filePath)
            }
            
            DocumentLoadResponse(
                status = "success",
                message = "문서가 성공적으로 로드되었습니다.",
                filePath = request.filePath,
                documentCount = documents.size,
                documents = documents.map { doc ->
                    DocumentInfo(
                        text = doc.text ?: "",
                        metadata = doc.metadata
                    )
                }
            )
        } catch (e: Exception) {
            DocumentLoadResponse(
                status = "error",
                message = "문서 로드 실패: ${e.message}",
                filePath = request.filePath
            )
        }
    }
    
    /**
     * 디렉토리 내의 모든 문서 로드
     * POST http://localhost:8080/api/documents/load-directory
     */
    @PostMapping("/load-directory")
    fun loadDirectory(@RequestBody request: LoadDirectoryRequest): Map<String, Any> {
        return if (request.addToVectorStore) {
            universalDocumentLoader.loadAndStoreDirectory(request.directoryPath)
        } else {
            val documents = universalDocumentLoader.loadDirectory(request.directoryPath)
            mapOf(
                "status" to "success",
                "message" to "디렉토리 내의 모든 문서가 성공적으로 로드되었습니다.",
                "directoryPath" to request.directoryPath,
                "documentCount" to documents.size
            )
        }
    }
}

