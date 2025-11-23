package com.example.embedding.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "MISTRAL_AI_API_KEY", matches = ".+")
class MistralAIEmbeddingControllerTest {

    @Autowired lateinit var embeddingModel: EmbeddingModel

    @Test
    fun `should embed with MistralAI`() {
        val embedding = embeddingModel.embed("MistralAI embedding test")
        assertThat(embedding).isNotNull()
        assertThat(embedding.size).isEqualTo(1024)
    }
}
