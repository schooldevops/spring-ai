package com.example.springai.service

import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class ModelSelectorService(
        private val primaryChatModel: ChatModel,
        @Qualifier("ollamaChatModel") private val ollamaChatModel: ChatModel? = null
) {
    fun selectModel(question: String): ChatModel {
        return when {
            question.length < 50 && ollamaChatModel != null -> ollamaChatModel
            else -> primaryChatModel
        }
    }

    fun smartChat(message: String): Map<String, Any> {
        val model = selectModel(message)
        val modelName = if (model == ollamaChatModel) "Ollama" else "Primary (OpenAI)"

        val prompt = Prompt(UserMessage(message))
        val response = model.call(prompt)

        return mapOf(
                "selectedModel" to modelName,
                "message" to (response.results.firstOrNull()?.output?.text ?: "응답 없음"),
                "questionLength" to message.length
        )
    }

    fun costOptimizedChat(message: String): Map<String, Any> {
        val useOllama =
                message.length < 30 &&
                        message.lowercase().contains(Regex("안녕|hi|hello|thanks|감사")) &&
                        ollamaChatModel != null

        val model = if (useOllama) ollamaChatModel!! else primaryChatModel
        val modelName = if (useOllama) "Ollama (free)" else "Primary (paid)"

        val prompt = Prompt(UserMessage(message))
        val response = model.call(prompt)

        return mapOf(
                "selectedModel" to modelName,
                "message" to (response.results.firstOrNull()?.output?.text ?: "응답 없음"),
                "costOptimized" to useOllama
        )
    }
}
