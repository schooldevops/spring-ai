package com.example.chatmodel.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.ai.chat.messages.*
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class MessagesPromptControllerTest {

    @Autowired lateinit var chatModel: ChatModel

    @Test
    fun `should use UserMessage`() {
        val prompt = Prompt(UserMessage("Say Hello"))
        val response = chatModel.call(prompt)
        assertThat(response.result.output.content).isNotEmpty()
    }

    @Test
    fun `should use SystemMessage and UserMessage`() {
        val messages = listOf(SystemMessage("You are helpful"), UserMessage("What is 2+2?"))
        val response = chatModel.call(Prompt(messages))
        assertThat(response.result.output.content).contains("4")
    }
}
