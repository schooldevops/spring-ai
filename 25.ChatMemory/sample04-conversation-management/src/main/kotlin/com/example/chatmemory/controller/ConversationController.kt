package com.example.chatmemory.controller

import com.example.chatmemory.service.ConversationService
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY
import org.springframework.web.bind.annotation.*

/** Sample 04: Conversation Management 여러 대화 세션 관리 */
@RestController
@RequestMapping("/api/conversations")
class ConversationController(
        private val conversationService: ConversationService,
        private val chatClient: ChatClient
) {

    @PostMapping
    fun createConversation(): Map<String, String> {
        val id = conversationService.createConversation()
        return mapOf("conversationId" to id)
    }

    @GetMapping fun listConversations() = conversationService.listConversations()

    @DeleteMapping("/{id}")
    fun deleteConversation(@PathVariable id: String): Map<String, String> {
        conversationService.deleteConversation(id)
        return mapOf("message" to "Conversation deleted")
    }

    @PostMapping("/{id}/messages")
    fun sendMessage(
            @PathVariable id: String,
            @RequestBody request: Map<String, String>
    ): Map<String, String> {
        val message = request["message"] ?: throw IllegalArgumentException("Message required")

        val response =
                chatClient
                        .prompt()
                        .user(message)
                        .advisors { it.param(CHAT_MEMORY_CONVERSATION_ID_KEY, id) }
                        .call()
                        .content()

        return mapOf("response" to response)
    }
}
