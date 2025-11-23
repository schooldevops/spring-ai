package com.example.embedding.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@EnabledIfSystemProperty(named = "ollama.enabled", matches = "true")
class OllamaEmbeddingControllerTest {

    @Autowired lateinit var embeddingModel: EmbeddingModel

    @Test
    fun `should embed with Ollama`() {
        val embedding = embeddingModel.embed("Ollama embedding test")
        assertThat(embedding).isNotNull()
        assertThat(embedding.size).isGreaterThan(0)
    }
}
