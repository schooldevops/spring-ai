package com.example.embedding.controller

import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.web.bind.annotation.*

/**
 * Sample 08: MistralAI Embedding
 *
 * MistralAI 임베딩 모델 사용법
 * - mistral-embed: 1024 dimensions
 */
@RestController
@RequestMapping("/api/mistralai")
class MistralAIEmbeddingController(private val embeddingModel: EmbeddingModel) {

    @GetMapping("/embed")
    fun embed(@RequestParam text: String): Map<String, Any> {
        val embedding = embeddingModel.embed(text)
        return mapOf(
                "provider" to "MistralAI",
                "model" to "mistral-embed",
                "text" to text,
                "dimensions" to embedding.size,
                "vector_preview" to embedding.take(10).toList()
        )
    }

    @PostMapping("/batch")
    fun batchEmbed(@RequestBody texts: List<String>): Map<String, Any> {
        val embeddings = embeddingModel.embed(texts)
        return mapOf(
                "provider" to "MistralAI",
                "count" to texts.size,
                "dimensions" to embeddings.first().size,
                "embeddings" to embeddings.map { it.take(5).toList() }
        )
    }

    @GetMapping("/info")
    fun getInfo(): Map<String, Any> {
        return mapOf(
                "provider" to "MistralAI",
                "model" to "mistral-embed",
                "dimensions" to embeddingModel.dimensions(),
                "features" to
                        listOf(
                                "High performance",
                                "Competitive pricing",
                                "European AI provider",
                                "1024 dimensions"
                        )
        )
    }
}
