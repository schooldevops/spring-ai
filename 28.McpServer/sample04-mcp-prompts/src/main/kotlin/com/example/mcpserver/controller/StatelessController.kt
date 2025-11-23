package com.example.mcpserver.controller

import com.example.mcpserver.provider.PromptProvider
import org.springframework.web.bind.annotation.*

/** Stateless Server Controller */
@RestController
@RequestMapping("/api/prompts")
class StatelessController(private val promptProvider: PromptProvider) {

    @GetMapping
    fun listPrompts(): Map<String, Any> {
        return mapOf("transport" to "Stateless", "prompts" to promptProvider.listPrompts())
    }

    @PostMapping("/{name}")
    fun getPrompt(
            @PathVariable name: String,
            @RequestBody params: Map<String, String>
    ): Map<String, String> {
        val prompt = promptProvider.getPrompt(name, params)
        return mapOf("prompt" to prompt)
    }

    @GetMapping("/info")
    fun getInfo(): Map<String, Any> {
        return mapOf(
                "transport" to "Stateless",
                "description" to "No session management, pure request/response",
                "features" to
                        listOf("No server-side state", "Horizontally scalable", "Simple and fast")
        )
    }
}
