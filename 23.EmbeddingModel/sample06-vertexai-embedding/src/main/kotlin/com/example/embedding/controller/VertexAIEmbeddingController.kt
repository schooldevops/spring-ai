package com.example.embedding.controller

import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.web.bind.annotation.*

/**
 * Sample 06: Google VertexAI Embedding
 *
 * Google VertexAI 임베딩 모델 사용법
 * - textembedding-gecko@003: 768 dimensions
 * - text-embedding-preview-0409: 768 dimensions
 */
@RestController
@RequestMapping("/api/vertexai")
class VertexAIEmbeddingController(private val embeddingModel: EmbeddingModel) {

    @GetMapping("/embed")
    fun embed(@RequestParam text: String): Map<String, Any> {
        val embedding = embeddingModel.embed(text)
        return mapOf(
                "provider" to "Google VertexAI",
                "model" to "textembedding-gecko@003",
                "text" to text,
                "dimensions" to embedding.size,
                "vector_preview" to embedding.take(10).toList()
        )
    }

    @PostMapping("/batch")
    fun batchEmbed(@RequestBody texts: List<String>): Map<String, Any> {
        val embeddings = embeddingModel.embed(texts)
        return mapOf(
                "provider" to "Google VertexAI",
                "count" to texts.size,
                "dimensions" to embeddings.first().size,
                "embeddings" to embeddings.map { it.take(5).toList() }
        )
    }

    @GetMapping("/info")
    fun getInfo(): Map<String, Any> {
        return mapOf(
                "provider" to "Google VertexAI",
                "model" to "Text Embedding Gecko",
                "dimensions" to embeddingModel.dimensions(),
                "features" to
                        listOf(
                                "Google Cloud integrated",
                                "Multilingual support",
                                "High quality embeddings",
                                "Enterprise security"
                        )
        )
    }
}
