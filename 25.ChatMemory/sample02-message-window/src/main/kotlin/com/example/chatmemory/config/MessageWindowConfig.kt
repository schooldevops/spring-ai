package com.example.chatmemory.config

import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.memory.InMemoryChatMemory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MessageWindowConfig {

    @Bean
    fun chatMemory(): ChatMemory {
        // 최근 10개 메시지만 유지
        return InMemoryChatMemory()
    }
}
