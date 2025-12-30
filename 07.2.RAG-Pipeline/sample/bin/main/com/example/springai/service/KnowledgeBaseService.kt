package com.example.springai.service

import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.stereotype.Service

/**
 * 지식베이스 서비스
 * 
 * 문서를 VectorStore에 추가하고 관리하는 서비스입니다.
 */
@Service
class KnowledgeBaseService(
    private val vectorStore: VectorStore
) {
    /**
     * 문서 추가
     */
    fun addDocument(
        text: String,
        title: String,
        source: String? = null,
        category: String? = null,
        metadata: Map<String, Any> = emptyMap()
    ) {
        val documentMetadata = metadata.toMutableMap()
        documentMetadata["title"] = title
        if (source != null) {
            documentMetadata["source"] = source
        }
        if (category != null) {
            documentMetadata["category"] = category
        }
        documentMetadata["addedAt"] = System.currentTimeMillis()
        
        val document = Document(text, documentMetadata)
        vectorStore.add(listOf(document))
    }
    
    /**
     * 여러 문서 추가
     */
    fun addDocuments(documents: List<Pair<String, String>>, source: String? = null, category: String? = null) {
        val docs = documents.mapIndexed { index, (title, text) ->
            val metadata = mutableMapOf<String, Any>(
                "title" to title,
                "index" to index
            )
            if (source != null) {
                metadata["source"] = source
            }
            if (category != null) {
                metadata["category"] = category
            }
            metadata["addedAt"] = System.currentTimeMillis()
            
            Document(text, metadata)
        }
        
        vectorStore.add(docs)
    }
}

