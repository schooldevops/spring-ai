package com.example.chatmodel.controller

import org.springframework.ai.chat.messages.*
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.web.bind.annotation.*

/** Sample 03: Messages and Prompt */
@RestController
@RequestMapping("/api/messages")
class MessagesPromptController(private val chatModel: ChatModel) {

    @GetMapping("/user-message")
    fun userMessage(@RequestParam message: String): String {
        val userMsg = UserMessage(message)
        val prompt = Prompt(userMsg)
        return chatModel.call(prompt).result.output.content
    }

    @GetMapping("/system-user")
    fun systemAndUser(@RequestParam system: String, @RequestParam user: String): String {
        val messages = listOf(SystemMessage(system), UserMessage(user))
        return chatModel.call(Prompt(messages)).result.output.content
    }

    @GetMapping("/conversation")
    fun conversation(@RequestParam question: String): String {
        val messages =
                listOf(
                        SystemMessage("You are a helpful assistant"),
                        UserMessage("What is AI?"),
                        AssistantMessage("AI is Artificial Intelligence..."),
                        UserMessage(question)
                )
        return chatModel.call(Prompt(messages)).result.output.content
    }
}
