package com.example.chatclient.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.ai.chat.client.ChatClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest

/** TDD: Sample 06 - Defaults and Advisors 테스트 */
@SpringBootTest
class DefaultsAdvisorsControllerTest {

    @Autowired lateinit var chatClient: ChatClient

    @Autowired @Qualifier("chatClientWithDefaults") lateinit var chatClientWithDefaults: ChatClient

    @Test
    fun `should use default system message`() {
        // When - 기본 시스템 메시지가 자동 적용됨
        val response = chatClientWithDefaults.prompt().user("What is AI?").call().content()

        // Then
        assertThat(response).isNotNull()
        assertThat(response).isNotEmpty()
    }

    @Test
    fun `should override default system message`() {
        // When - 기본값을 런타임에 오버라이드
        val response =
                chatClientWithDefaults
                        .prompt()
                        .system("You are a math tutor")
                        .user("What is 2+2?")
                        .call()
                        .content()

        // Then
        assertThat(response).isNotNull()
        assertThat(response).contains("4")
    }

    @Test
    fun `should use chatClient without defaults`() {
        // When - 기본값 없는 ChatClient
        val response =
                chatClient.prompt().system("You are helpful").user("Say hello").call().content()

        // Then
        assertThat(response).isNotNull()
        assertThat(response).isNotEmpty()
    }
}
