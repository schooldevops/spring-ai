package com.example.chatmodel.controller

import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

/** Sample 02: StreamingChatModel */
@RestController
@RequestMapping("/api/stream")
class StreamingController(private val chatModel: ChatModel) {

    @GetMapping("/simple", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamSimple(@RequestParam message: String): Flux<String> {
        return chatModel.stream(message)
    }

    @GetMapping("/prompt", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamPrompt(@RequestParam message: String): Flux<String> {
        return chatModel.stream(Prompt(message)).map { it.result.output.content }
    }

    @GetMapping("/aggregated")
    fun streamAggregated(@RequestParam message: String): String {
        return chatModel.stream(message).collectList().block()?.joinToString("") ?: ""
    }
}
