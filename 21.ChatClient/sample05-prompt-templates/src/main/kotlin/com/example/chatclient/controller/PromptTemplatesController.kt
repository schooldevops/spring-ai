package com.example.chatclient.controller

import org.springframework.ai.chat.client.ChatClient
import org.springframework.web.bind.annotation.*

/**
 * Sample 05: Prompt Templates
 *
 * 템플릿 변수를 사용한 동적 프롬프트 생성
 */
@RestController
@RequestMapping("/api/template")
class PromptTemplatesController(private val chatClient: ChatClient) {

    /** 1. 단일 템플릿 변수 GET /api/template/single */
    @GetMapping("/single")
    fun singleVariable(@RequestParam topic: String): String {
        return chatClient
                .prompt()
                .user { u -> u.text("Tell me about {topic} in 2 sentences").param("topic", topic) }
                .call()
                .content()
                ?: ""
    }

    /** 2. 다중 템플릿 변수 GET /api/template/multiple */
    @GetMapping("/multiple")
    fun multipleVariables(
            @RequestParam topic: String,
            @RequestParam language: String,
            @RequestParam length: String
    ): String {
        return chatClient
                .prompt()
                .user { u ->
                    u.text("Explain {topic} in {language} in {length} sentences")
                            .param("topic", topic)
                            .param("language", language)
                            .param("length", length)
                }
                .call()
                .content()
                ?: ""
    }

    /** 3. System 메시지에 템플릿 사용 GET /api/template/system-template */
    @GetMapping("/system-template")
    fun systemTemplate(@RequestParam role: String, @RequestParam question: String): String {
        return chatClient
                .prompt()
                .system { s -> s.text("You are a {role}. Answer concisely.").param("role", role) }
                .user(question)
                .call()
                .content()
                ?: ""
    }

    /** 4. 복잡한 템플릿 POST /api/template/complex */
    @PostMapping("/complex")
    fun complexTemplate(
            @RequestParam subject: String,
            @RequestParam audience: String,
            @RequestParam format: String
    ): String {
        return chatClient
                .prompt()
                .system { s -> s.text("You are an expert in {subject}").param("subject", subject) }
                .user { u ->
                    u.text("Explain {subject} for {audience} in {format} format")
                            .param("subject", subject)
                            .param("audience", audience)
                            .param("format", format)
                }
                .call()
                .content()
                ?: ""
    }
}
