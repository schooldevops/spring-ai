package com.example.chatclient.controller

import org.springframework.ai.chat.client.ChatClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

/**
 * Sample 04: Streaming Responses
 *
 * stream() 메서드를 사용한 실시간 응답 처리
 */
@RestController
@RequestMapping("/api/stream")
class StreamingController(private val chatClient: ChatClient) {

    /** 1. 기본 스트리밍 - Flux<String> GET /api/stream/content */
    @GetMapping("/content", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamContent(@RequestParam prompt: String): Flux<String> {
        return chatClient.prompt().user(prompt).stream().content()
    }

    /** 2. 스트림 집계 - 전체 내용 반환 GET /api/stream/aggregated */
    @GetMapping("/aggregated")
    fun streamAggregated(@RequestParam prompt: String): String {
        val flux = chatClient.prompt().user(prompt).stream().content()

        return flux.collectList().block()?.joinToString("") ?: ""
    }

    /** 3. ChatResponse 스트리밍 GET /api/stream/chat-response */
    @GetMapping("/chat-response", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamChatResponse(@RequestParam prompt: String): Flux<String> {
        return chatClient.prompt().user(prompt).stream().chatResponse().map { response ->
            response.results.firstOrNull()?.output?.content ?: ""
        }
    }
}
