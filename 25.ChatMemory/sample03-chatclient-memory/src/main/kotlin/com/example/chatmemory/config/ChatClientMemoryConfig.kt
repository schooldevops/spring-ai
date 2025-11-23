package com.example.chatmemory.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor
import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.memory.InMemoryChatMemory
import org.springframework.ai.chat.model.ChatModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ChatClientMemoryConfig {

    @Bean
    fun chatMemory(): ChatMemory {
        return InMemoryChatMemory()
    }

    @Bean
    fun chatClient(chatModel: ChatModel, chatMemory: ChatMemory): ChatClient {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(PromptChatMemoryAdvisor(chatMemory))
                .build()
    }
}
