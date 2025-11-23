package com.example.chatmemory.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class MessageWindowControllerTest {

    @Autowired lateinit var chatMemory: ChatMemory

    @Test
    fun `should limit message window size`() {
        val convId = "test-window"

        // Add many messages
        repeat(20) { i ->
            chatMemory.add(
                    convId,
                    listOf(org.springframework.ai.chat.messages.UserMessage("Message $i"))
            )
        }

        // Get only last 5
        val history = chatMemory.get(convId, 5)
        assertThat(history).hasSizeLessThanOrEqualTo(5)
    }
}
