package com.example.chatmemory.controller

import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.web.bind.annotation.*

/** Sample 02: Message Window Memory 최근 N개 메시지만 유지하여 토큰 제한 관리 */
@RestController
@RequestMapping("/api/chat")
class MessageWindowController(
        private val chatModel: ChatModel,
        private val chatMemory: ChatMemory
) {

    data class ChatRequest(
            val conversationId: String,
            val message: String,
            val windowSize: Int = 6
    )

    @PostMapping("/message")
    fun chat(@RequestBody request: ChatRequest): Map<String, Any> {
        // 최근 N개 메시지만 가져오기
        val history = chatMemory.get(request.conversationId, request.windowSize)
        val newMessage = UserMessage(request.message)

        val response = chatModel.call(Prompt(history + newMessage))
        val assistantMessage = response.result.output

        chatMemory.add(request.conversationId, listOf(newMessage, assistantMessage))

        return mapOf(
                "response" to assistantMessage.content,
                "windowSize" to request.windowSize,
                "messagesInWindow" to history.size
        )
    }

    @GetMapping("/history/{conversationId}")
    fun getHistory(
            @PathVariable conversationId: String,
            @RequestParam(defaultValue = "10") limit: Int
    ): Map<String, Any> {
        val history = chatMemory.get(conversationId, limit)
        return mapOf(
                "conversationId" to conversationId,
                "messageCount" to history.size,
                "messages" to
                        history.map { mapOf("role" to it.messageType.value, "content" to it.text) }
        )
    }
}
