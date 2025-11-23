package com.example.chatclient.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.ai.chat.client.ChatClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

/** TDD: Sample 05 - Prompt Templates 테스트 */
@SpringBootTest
class PromptTemplatesControllerTest {

    @Autowired lateinit var chatClient: ChatClient

    @Test
    fun `should use template variable with param`() {
        // When
        val response =
                chatClient
                        .prompt()
                        .user { u -> u.text("Tell me about {topic}").param("topic", "Spring AI") }
                        .call()
                        .content()

        // Then
        assertThat(response).isNotNull()
        assertThat(response).isNotEmpty()
    }

    @Test
    fun `should use multiple template variables`() {
        // When
        val response =
                chatClient
                        .prompt()
                        .user { u ->
                            u.text("Explain {topic} in {language}")
                                    .param("topic", "ChatClient")
                                    .param("language", "Korean")
                        }
                        .call()
                        .content()

        // Then
        assertThat(response).isNotNull()
        assertThat(response).isNotEmpty()
    }

    @Test
    fun `should use template in system message`() {
        // When
        val response =
                chatClient
                        .prompt()
                        .system { s ->
                            s.text("You are a {role}").param("role", "helpful assistant")
                        }
                        .user("What is AI?")
                        .call()
                        .content()

        // Then
        assertThat(response).isNotNull()
        assertThat(response).isNotEmpty()
    }
}
