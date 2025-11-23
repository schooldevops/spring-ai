package com.example.mcpserver.provider

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PromptProviderTest {

    @Autowired lateinit var promptProvider: PromptProvider

    @Test
    fun `should get greeting prompt`() {
        val prompt = promptProvider.getPrompt("greeting", mapOf("name" to "Alice"))
        assertThat(prompt).contains("Alice")
    }

    @Test
    fun `should list all prompts`() {
        val prompts = promptProvider.listPrompts()
        assertThat(prompts).hasSizeGreaterThan(0)
    }
}
