package com.example.chatmemory.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ConversationServiceTest {

    @Autowired lateinit var conversationService: ConversationService

    @Test
    fun `should create and list conversations`() {
        val id1 = conversationService.createConversation()
        val id2 = conversationService.createConversation()

        val conversations = conversationService.listConversations()

        assertThat(conversations).hasSizeGreaterThanOrEqualTo(2)
        assertThat(conversations.map { it.id }).contains(id1, id2)
    }

    @Test
    fun `should delete conversation`() {
        val id = conversationService.createConversation()
        conversationService.deleteConversation(id)

        val conversations = conversationService.listConversations()
        assertThat(conversations.map { it.id }).doesNotContain(id)
    }
}
