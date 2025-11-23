package com.example.embedding.controller

import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.web.bind.annotation.*

/**
 * Sample 07: Ollama Embedding
 *
 * Ollama 로컬 임베딩 모델 사용법
 * - nomic-embed-text: 768 dimensions (추천)
 * - mxbai-embed-large: 1024 dimensions
 * - all-minilm: 384 dimensions (빠름)
 *
 * 사전 요구사항: ollama pull nomic-embed-text
 */
@RestController
@RequestMapping("/api/ollama")
class OllamaEmbeddingController(private val embeddingModel: EmbeddingModel) {

    @GetMapping("/embed")
    fun embed(@RequestParam text: String): Map<String, Any> {
        val embedding = embeddingModel.embed(text)
        return mapOf(
                "provider" to "Ollama (Local)",
                "model" to "nomic-embed-text",
                "text" to text,
                "dimensions" to embedding.size,
                "vector_preview" to embedding.take(10).toList()
        )
    }

    @PostMapping("/batch")
    fun batchEmbed(@RequestBody texts: List<String>): Map<String, Any> {
        val embeddings = embeddingModel.embed(texts)
        return mapOf(
                "provider" to "Ollama (Local)",
                "count" to texts.size,
                "dimensions" to embeddings.first().size,
                "embeddings" to embeddings.map { it.take(5).toList() }
        )
    }

    @GetMapping("/info")
    fun getInfo(): Map<String, Any> {
        return mapOf(
                "provider" to "Ollama",
                "model" to "nomic-embed-text",
                "dimensions" to embeddingModel.dimensions(),
                "features" to
                        listOf(
                                "Runs locally",
                                "No API costs",
                                "Privacy-focused",
                                "Offline capable"
                        ),
                "setup" to "ollama pull nomic-embed-text"
        )
    }
}
