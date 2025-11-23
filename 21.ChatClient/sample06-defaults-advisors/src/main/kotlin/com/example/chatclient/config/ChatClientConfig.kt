package com.example.chatclient.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Sample 06: Defaults and Advisors Configuration
 *
 * 기본 시스템 메시지와 파라미터 설정
 */
@Configuration
class ChatClientConfig {

    /** 기본 설정이 없는 ChatClient */
    @Bean
    fun chatClient(builder: ChatClient.Builder): ChatClient {
        return builder.build()
    }

    /** 기본 시스템 메시지가 설정된 ChatClient */
    @Bean("chatClientWithDefaults")
    fun chatClientWithDefaults(builder: ChatClient.Builder): ChatClient {
        return builder.defaultSystem("You are a helpful AI assistant. Be concise and clear.")
                .build()
    }

    /** 기본 파라미터가 설정된 ChatClient */
    @Bean("chatClientWithParams")
    fun chatClientWithParams(builder: ChatClient.Builder): ChatClient {
        return builder.defaultSystem("You are a {role}")
                .defaultUser { u -> u.param("role", "helpful assistant") }
                .build()
    }
}
