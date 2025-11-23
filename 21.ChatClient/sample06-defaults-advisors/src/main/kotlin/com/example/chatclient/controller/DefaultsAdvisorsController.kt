package com.example.chatclient.controller

import org.springframework.ai.chat.client.ChatClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.bind.annotation.*

/**
 * Sample 06: Defaults and Advisors
 *
 * 기본 설정을 활용한 ChatClient 사용
 */
@RestController
@RequestMapping("/api/defaults")
class DefaultsAdvisorsController(
        private val chatClient: ChatClient,
        @Qualifier("chatClientWithDefaults") private val chatClientWithDefaults: ChatClient
) {

    /** 1. 기본 설정 없는 ChatClient GET /api/defaults/no-defaults */
    @GetMapping("/no-defaults")
    fun noDefaults(@RequestParam question: String): String {
        return chatClient
                .prompt()
                .system("You are a helpful assistant")
                .user(question)
                .call()
                .content()
                ?: ""
    }

    /** 2. 기본 시스템 메시지 사용 GET /api/defaults/with-defaults */
    @GetMapping("/with-defaults")
    fun withDefaults(@RequestParam question: String): String {
        // 기본 시스템 메시지가 자동으로 적용됨
        return chatClientWithDefaults.prompt().user(question).call().content() ?: ""
    }

    /** 3. 기본값 오버라이드 GET /api/defaults/override */
    @GetMapping("/override")
    fun overrideDefaults(@RequestParam question: String, @RequestParam role: String): String {
        // 런타임에 기본 시스템 메시지 오버라이드
        return chatClientWithDefaults
                .prompt()
                .system("You are a $role")
                .user(question)
                .call()
                .content()
                ?: ""
    }

    /** 4. 기본값과 추가 설정 조합 POST /api/defaults/combined */
    @PostMapping("/combined")
    fun combined(@RequestParam question: String, @RequestParam additionalContext: String?): String {
        val prompt = chatClientWithDefaults.prompt().user(question)

        // 추가 컨텍스트가 있으면 추가
        additionalContext?.let { prompt.user("Additional context: $it") }

        return prompt.call().content() ?: ""
    }
}
