package com.example.springai.config

import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.ai.vectorstore.SimpleVectorStore
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * VectorStore 설정
 * 
 * RAG 패턴을 위한 VectorStore 설정입니다.
 * SimpleVectorStore를 사용하여 메모리 기반으로 동작합니다.
 */
@Configuration
class VectorStoreConfig(
    private val embeddingModel: EmbeddingModel
) {
    @Bean
    fun vectorStore(): VectorStore {
        return SimpleVectorStore.builder(embeddingModel).build()
    }
}

