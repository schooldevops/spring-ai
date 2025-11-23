package com.example.chatclient.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.ai.chat.client.ChatClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.test.StepVerifier

/** TDD: Sample 04 - Streaming 테스트 */
@SpringBootTest
class StreamingControllerTest {

    @Autowired lateinit var chatClient: ChatClient

    @Test
    fun `should stream content as Flux of String`() {
        // When
        val flux = chatClient.prompt().user("Count from 1 to 5").stream().content()

        // Then
        StepVerifier.create(flux)
                .expectNextMatches { it.isNotEmpty() }
                .thenConsumeWhile { true }
                .verifyComplete()
    }

    @Test
    fun `should collect streamed content`() {
        // When
        val flux = chatClient.prompt().user("Say 'Hello'").stream().content()

        val content = flux.collectList().block()?.joinToString("")

        // Then
        assertThat(content).isNotNull()
        assertThat(content).isNotEmpty()
    }

    @Test
    fun `should stream ChatResponse`() {
        // When
        val flux = chatClient.prompt().user("Say 'Test'").stream().chatResponse()

        // Then
        StepVerifier.create(flux)
                .expectNextMatches { response -> response.results.isNotEmpty() }
                .thenConsumeWhile { true }
                .verifyComplete()
    }
}
