package com.example.function.controller

import org.springframework.ai.chat.client.ChatClient
import org.springframework.web.bind.annotation.*

/** Sample 02: Weather Function */
@RestController
@RequestMapping("/api/chat")
class WeatherController(private val chatClient: ChatClient) {

    @PostMapping("/ask")
    fun ask(@RequestBody request: Map<String, String>): Map<String, String> {
        val question = request["question"] ?: throw IllegalArgumentException("Question required")

        val response = chatClient.prompt().user(question).call().content()

        return mapOf("response" to response)
    }
}
