package com.example.embedding.controller

import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.web.bind.annotation.*

/**
 * Sample 05: Amazon Bedrock Titan Embedding
 *
 * AWS Bedrock Titan 임베딩 모델 사용법
 * - amazon.titan-embed-text-v1: 1536 dimensions
 * - amazon.titan-embed-text-v2: 1024 dimensions (더 빠름)
 */
@RestController
@RequestMapping("/api/bedrock")
class BedrockEmbeddingController(private val embeddingModel: EmbeddingModel) {

    @GetMapping("/embed")
    fun embed(@RequestParam text: String): Map<String, Any> {
        val embedding = embeddingModel.embed(text)
        return mapOf(
                "provider" to "Amazon Bedrock",
                "model" to "amazon.titan-embed-text-v1",
                "text" to text,
                "dimensions" to embedding.size,
                "vector_preview" to embedding.take(10).toList()
        )
    }

    @PostMapping("/batch")
    fun batchEmbed(@RequestBody texts: List<String>): Map<String, Any> {
        val embeddings = embeddingModel.embed(texts)
        return mapOf(
                "provider" to "Amazon Bedrock",
                "count" to texts.size,
                "dimensions" to embeddings.first().size,
                "embeddings" to embeddings.map { it.take(5).toList() }
        )
    }

    @GetMapping("/info")
    fun getInfo(): Map<String, Any> {
        return mapOf(
                "provider" to "Amazon Bedrock",
                "model" to "Titan Embed Text",
                "dimensions" to embeddingModel.dimensions(),
                "features" to
                        listOf(
                                "AWS integrated",
                                "Enterprise-grade security",
                                "Multiple language support",
                                "Cost-effective"
                        ),
                "regions" to listOf("us-east-1", "us-west-2", "eu-west-1")
        )
    }
}
