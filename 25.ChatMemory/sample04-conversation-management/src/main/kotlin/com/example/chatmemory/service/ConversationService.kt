package com.example.chatmemory.service

import java.util.UUID
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.stereotype.Service

@Service
class ConversationService(private val chatMemory: ChatMemory) {

    data class Conversation(val id: String, val messageCount: Int, val lastMessage: String?)

    private val conversations = mutableMapOf<String, MutableList<String>>()

    fun createConversation(): String {
        val id = UUID.randomUUID().toString()
        conversations[id] = mutableListOf()
        return id
    }

    fun listConversations(): List<Conversation> {
        return conversations.map { (id, messages) ->
            val history = chatMemory.get(id, 100)
            Conversation(
                    id = id,
                    messageCount = history.size,
                    lastMessage = history.lastOrNull()?.text
            )
        }
    }

    fun deleteConversation(conversationId: String) {
        chatMemory.clear(conversationId)
        conversations.remove(conversationId)
    }
}
