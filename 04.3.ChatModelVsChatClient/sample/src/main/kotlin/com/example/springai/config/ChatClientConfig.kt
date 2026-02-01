package com.example.springai.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * ChatClient 설정
 *
 * ChatClient는 ChatModel을 기반으로 생성되며, 기본 설정(default system message, advisors 등)을 추가할 수 있습니다.
 */
@Configuration
class ChatClientConfig {

    /**
     * ChatClient Bean 생성
     *
     * @param builder Spring AI가 자동으로 제공하는 ChatClient.Builder
     * @return 설정된 ChatClient 인스턴스
     */
    @Bean
    fun chatClient(builder: ChatClient.Builder): ChatClient {
        return builder
                // 기본 시스템 메시지 설정 (선택사항)
                // .defaultSystem("You are a helpful AI assistant")

                // 기본 Advisors 설정 (선택사항)
                // .defaultAdvisors(...)

                .build()
    }
}
