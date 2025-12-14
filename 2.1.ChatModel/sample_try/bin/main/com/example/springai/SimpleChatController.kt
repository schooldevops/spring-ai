package com.example.springai

import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/simple")
class SimpleChatController(private val chatModel: ChatModel) {

    @GetMapping("/basic")
    fun basicChat(): String {
        val userMessage = UserMessage("안녕하세요! 간단히 자기소개를 한글로 해주세요.")
        val prompt = Prompt(userMessage)

        val response = chatModel.call(prompt)

        return response.results.firstOrNull()?.output?.text ?: "응답을 생성할 수 없습니다."
    }

    @PostMapping("/chat")
    fun chat(@RequestBody request: ChatRequest): ChatResponse {
        val prompt = Prompt(UserMessage(request.message))
        val response = chatModel.call(prompt)

        return ChatResponse(response.results.firstOrNull()?.output?.text ?: "응답을 생성할 수 없습니다.")
    }

    @PostMapping("/safe")
    fun safeChat(@RequestBody request: ChatRequest): String {
        val prompt = Prompt(UserMessage(request.message))

        return try {
            val response = chatModel.call(prompt)
            response.results.firstOrNull()?.output?.text ?: "응답을 생성할 수 없습니다."
        } catch (e: Exception) {
            "오류 발생: ${e.message}"
        }
    }
}

data class ChatRequest(val message: String)

data class ChatResponse(val message: String)
