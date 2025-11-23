package com.example.chatmodel.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ChatOptionsControllerTest {

    @Autowired lateinit var chatModel: ChatModel

    @Test
    fun `should use temperature option`() {
        val options = OpenAiChatOptions.builder().withTemperature(0.5).build()
        val prompt = Prompt("Say Hello", options)
        val response = chatModel.call(prompt)
        assertThat(response.result.output.content).isNotEmpty()
    }

    @Test
    fun `should use max tokens option`() {
        val options = OpenAiChatOptions.builder().withMaxTokens(50).build()
        val prompt = Prompt("Tell a story", options)
        val response = chatModel.call(prompt)
        assertThat(response.result.output.content).isNotEmpty()
    }
}
