package com.example.springai

import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt

fun ChatModel.simpleCall(message: String): String {
    val prompt = Prompt(UserMessage(message))
    return this.call(prompt).results.firstOrNull()?.output?.text ?: "응답을 생성할 수 없습니다."
}

fun ChatModel.safeCall(message: String): Result<String> {
    return runCatching {
        val prompt = Prompt(UserMessage(message))
        this.call(prompt).results.firstOrNull()?.output?.text
                ?: throw IllegalStateException("응답 없음")
    }
}

fun ChatModel.multiCall(vararg messages: String): String {
    val userMessages = messages.map { UserMessage(it) }
    val prompt = Prompt(userMessages)
    return this.call(prompt).results.firstOrNull()?.output?.text ?: "응답을 생성할 수 없습니다."
}
