package com.example.springai.controller

import org.springframework.ai.vectorstore.VectorStore
import org.springframework.web.bind.annotation.*
import com.example.springai.model.DeleteDocumentRequest

/**
 * 문서 삭제 컨트롤러
 * 
 * PGVectorStore는 문서 삭제를 지원합니다.
 */
@RestController
@RequestMapping("/api/documents")
class DeleteDocumentController(
    private val vectorStore: VectorStore
) {
    
    /**
     * 문서 삭제
     * POST http://localhost:8080/api/documents/delete
     * Body: {"documentIds": ["id1", "id2"]}
     */
    @PostMapping("/delete")
    fun deleteDocuments(@RequestBody request: DeleteDocumentRequest): Map<String, Any> {
        vectorStore.delete(request.documentIds)
        
        return mapOf(
            "status" to "success",
            "message" to "${request.documentIds.size}개의 문서가 삭제되었습니다.",
            "deletedIds" to request.documentIds
        )
    }
}

