package com.example.mcpserver.provider

import org.springframework.stereotype.Component

/**
 * Sample 04: Stateless MCP Server
 *
 * Provides prompt templates without session state
 */
@Component
class PromptProvider {

    private val prompts =
            mapOf(
                    "greeting" to "Hello, {name}! Welcome to our service.",
                    "farewell" to "Goodbye, {name}! Have a great day!",
                    "summary" to "Please summarize the following text: {text}",
                    "translate" to "Translate the following from {from} to {to}: {text}"
            )

    fun getPrompt(name: String, params: Map<String, String> = emptyMap()): String {
        var template = prompts[name] ?: throw IllegalArgumentException("Prompt not found: $name")

        params.forEach { (key, value) -> template = template.replace("{$key}", value) }

        return template
    }

    fun listPrompts(): List<Map<String, String>> {
        return prompts.map { (name, template) -> mapOf("name" to name, "template" to template) }
    }
}
