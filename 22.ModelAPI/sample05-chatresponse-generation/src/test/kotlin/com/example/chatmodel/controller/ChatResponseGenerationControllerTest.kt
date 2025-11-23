package com.example.chatmodel.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ChatResponseGenerationControllerTest {

    @Autowired lateinit var chatModel: ChatModel

    @Test
    fun `should access ChatResponse structure`() {
        val chatResponse = chatModel.call(Prompt("Say Hello"))
        assertThat(chatResponse.result).isNotNull()
        assertThat(chatResponse.results).isNotEmpty()
        assertThat(chatResponse.metadata).isNotNull()
    }

    @Test
    fun `should access usage metadata`() {
        val chatResponse = chatModel.call(Prompt("What is AI?"))
        val usage = chatResponse.metadata?.usage
        assertThat(usage).isNotNull()
        assertThat(usage?.totalTokens).isGreaterThan(0)
    }
}
