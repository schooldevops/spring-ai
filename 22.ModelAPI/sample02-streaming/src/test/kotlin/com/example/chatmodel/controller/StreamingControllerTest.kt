package com.example.chatmodel.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.ai.chat.model.ChatModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.test.StepVerifier

@SpringBootTest
class StreamingControllerTest {

    @Autowired lateinit var chatModel: ChatModel

    @Test
    fun `should stream simple message`() {
        val flux = chatModel.stream("Count 1 to 3")
        StepVerifier.create(flux)
                .expectNextMatches { it.isNotEmpty() }
                .thenConsumeWhile { true }
                .verifyComplete()
    }

    @Test
    fun `should aggregate stream`() {
        val content = chatModel.stream("Say Hello").collectList().block()?.joinToString("")
        assertThat(content).isNotNull().isNotEmpty()
    }
}
