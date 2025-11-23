package com.example.chatmodel.controller

import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.web.bind.annotation.*

/** Sample 04: ChatOptions */
@RestController
@RequestMapping("/api/options")
class ChatOptionsController(private val chatModel: ChatModel) {

    @GetMapping("/temperature")
    fun withTemperature(
            @RequestParam message: String,
            @RequestParam(defaultValue = "0.7") temperature: Double
    ): String {
        val options = OpenAiChatOptions.builder().withTemperature(temperature).build()
        val prompt = Prompt(message, options)
        return chatModel.call(prompt).result.output.content
    }

    @GetMapping("/max-tokens")
    fun withMaxTokens(
            @RequestParam message: String,
            @RequestParam(defaultValue = "100") maxTokens: Int
    ): String {
        val options = OpenAiChatOptions.builder().withMaxTokens(maxTokens).build()
        val prompt = Prompt(message, options)
        return chatModel.call(prompt).result.output.content
    }

    @GetMapping("/multiple-options")
    fun withMultipleOptions(@RequestParam message: String): Map<String, Any> {
        val options =
                OpenAiChatOptions.builder()
                        .withModel("gpt-4o-mini")
                        .withTemperature(0.8)
                        .withMaxTokens(200)
                        .withTopP(0.9)
                        .build()
        val prompt = Prompt(message, options)
        val response = chatModel.call(prompt)

        return mapOf(
                "content" to response.result.output.content,
                "model" to (response.metadata?.model ?: "unknown"),
                "usage" to mapOf("totalTokens" to (response.metadata?.usage?.totalTokens ?: 0))
        )
    }
}
