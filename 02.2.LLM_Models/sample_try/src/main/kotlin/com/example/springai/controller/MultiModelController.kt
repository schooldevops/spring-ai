package com.example.springai.controller

import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/multi")
class MultiModelController(
        private val primaryChatModel: ChatModel,
        @Qualifier("ollamaChatModel") private val ollamaChatModel: ChatModel? = null
) {
    @GetMapping("/default")
    fun chatWithDefault(@RequestParam message: String): Map<String, Any> {
        val prompt = Prompt(UserMessage(message))
        val response = primaryChatModel.call(prompt)

        return mapOf(
                "model" to "default (primary)",
                "message" to (response.results.firstOrNull()?.output?.text ?: "응답 없음")
        )
    }

    @GetMapping("/ollama")
    fun chatWithOllama(@RequestParam message: String): Map<String, Any> {
        if (ollamaChatModel == null) {
            return mapOf(
                    "error" to
                            "Ollama is not configured. Please uncomment ollama dependency in build.gradle.kts"
            )
        }

        val prompt = Prompt(UserMessage(message))
        val response = ollamaChatModel.call(prompt)

        return mapOf(
                "model" to "Ollama",
                "message" to (response.results.firstOrNull()?.output?.text ?: "응답 없음")
        )
    }

    @GetMapping("/compare")
    fun compareModels(@RequestParam message: String): Map<String, Any> {
        val prompt = Prompt(UserMessage(message))

        val defaultResponse = primaryChatModel.call(prompt)
        val defaultText = defaultResponse.results.firstOrNull()?.output?.text ?: "응답 없음"

        val result =
                mutableMapOf<String, Any>(
                        "default" to mapOf("model" to "Primary", "message" to defaultText)
                )

        if (ollamaChatModel != null) {
            val ollamaResponse = ollamaChatModel.call(prompt)
            val ollamaText = ollamaResponse.results.firstOrNull()?.output?.text ?: "응답 없음"

            result["ollama"] = mapOf("model" to "Ollama", "message" to ollamaText)
        } else {
            result["ollama"] = mapOf("error" to "Ollama not configured")
        }

        return result
    }
}
