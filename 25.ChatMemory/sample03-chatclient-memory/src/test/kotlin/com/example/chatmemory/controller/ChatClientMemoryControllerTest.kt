package com.example.chatmemory.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ChatClientMemoryControllerTest {

    @Autowired lateinit var chatClient: ChatClient

    @Test
    fun `should remember context with ChatClient advisor`() {
        val convId = "test-chatclient"

        // First message
        chatClient
                .prompt()
                .user("My favorite food is pizza")
                .advisors { it.param(CHAT_MEMORY_CONVERSATION_ID_KEY, convId) }
                .call()
                .content()

        // Second message - should remember
        val response =
                chatClient
                        .prompt()
                        .user("What is my favorite food?")
                        .advisors { it.param(CHAT_MEMORY_CONVERSATION_ID_KEY, convId) }
                        .call()
                        .content()

        assertThat(response.lowercase()).contains("pizza")
    }
}
