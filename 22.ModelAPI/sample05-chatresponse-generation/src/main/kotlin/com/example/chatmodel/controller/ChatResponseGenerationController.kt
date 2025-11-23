package com.example.chatmodel.controller

import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.web.bind.annotation.*

/** Sample 05: ChatResponse and Generation */
@RestController
@RequestMapping("/api/response")
class ChatResponseGenerationController(private val chatModel: ChatModel) {

    @GetMapping("/full")
    fun fullResponse(@RequestParam message: String): Map<String, Any> {
        val chatResponse = chatModel.call(Prompt(message))

        return mapOf(
                "result" to
                        mapOf(
                                "content" to chatResponse.result.output.content,
                                "finishReason" to
                                        (chatResponse.result.metadata?.finishReason ?: "unknown")
                        ),
                "metadata" to
                        mapOf(
                                "model" to (chatResponse.metadata?.model ?: "unknown"),
                                "usage" to
                                        mapOf(
                                                "promptTokens" to
                                                        (chatResponse.metadata?.usage?.promptTokens
                                                                ?: 0),
                                                "generationTokens" to
                                                        (chatResponse
                                                                .metadata
                                                                ?.usage
                                                                ?.generationTokens
                                                                ?: 0),
                                                "totalTokens" to
                                                        (chatResponse.metadata?.usage?.totalTokens
                                                                ?: 0)
                                        )
                        ),
                "results" to
                        chatResponse.results.map { generation ->
                            mapOf(
                                    "content" to generation.output.content,
                                    "finishReason" to
                                            (generation.metadata?.finishReason ?: "unknown")
                            )
                        }
        )
    }

    @GetMapping("/usage")
    fun usageInfo(@RequestParam message: String): Map<String, Any> {
        val chatResponse = chatModel.call(Prompt(message))
        val usage = chatResponse.metadata?.usage

        return mapOf(
                "promptTokens" to (usage?.promptTokens ?: 0),
                "generationTokens" to (usage?.generationTokens ?: 0),
                "totalTokens" to (usage?.totalTokens ?: 0)
        )
    }
}
