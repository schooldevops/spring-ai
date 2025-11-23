package com.example.chatmemory.controller

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY
import org.springframework.web.bind.annotation.*

/** Sample 03: ChatClient with Memory Advisor ChatClient가 자동으로 메모리 관리 */
@RestController
@RequestMapping("/api/chat")
class ChatClientMemoryController(private val chatClient: ChatClient) {

    data class ChatRequest(val conversationId: String, val message: String)

    @PostMapping("/message")
    fun chat(@RequestBody request: ChatRequest): Map<String, String> {
        val response =
                chatClient
                        .prompt()
                        .user(request.message)
                        .advisors {
                            it.param(CHAT_MEMORY_CONVERSATION_ID_KEY, request.conversationId)
                        }
                        .call()
                        .content()

        return mapOf("conversationId" to request.conversationId, "response" to response)
    }
}
