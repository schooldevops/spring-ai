package com.example.springai.controller

import com.example.springai.dto.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

/**
 * ChatModel 기본 사용법 컨트롤러
 *
 * ChatModel은 Spring AI의 저수준(Low-Level) API입니다.
 */
@RestController
@RequestMapping("/api/chatmodel")
class ChatModelController(private val chatModel: ChatModel) {
    private val objectMapper = jacksonObjectMapper()

    @PostMapping("/basic")
    fun basicChat(@RequestBody request: QuestionRequest): String {
        val prompt = Prompt(UserMessage(request.question))
        val response: ChatResponse = chatModel.call(prompt)
        return response.result.output.text
    }

    @PostMapping("/with-system")
    fun chatWithSystem(@RequestBody request: ContextQuestionRequest): String {
        val messages =
                listOf(SystemMessage(request.systemMessage), UserMessage(request.userMessage))
        val prompt = Prompt(messages)
        val response = chatModel.call(prompt)
        return response.result.output.text
    }

    @PostMapping("/expert")
    fun askExpert(@RequestBody request: ExpertQuestionRequest): String {
        val messages =
                listOf(
                        SystemMessage(
                                "You are an expert in ${request.domain}. Provide detailed and accurate information."
                        ),
                        UserMessage(request.question)
                )
        val prompt = Prompt(messages)
        val response = chatModel.call(prompt)
        return response.result.output.text
    }

    @PostMapping("/book-recommendation")
    fun recommendBook(@RequestParam genre: String): BookRecommendation {
        val messages =
                listOf(
                        SystemMessage(
                                "You are a book expert. Return your recommendation in JSON format."
                        ),
                        UserMessage("Recommend a $genre book")
                )
        val prompt = Prompt(messages)
        val response = chatModel.call(prompt)
        val jsonContent = response.result.output.text

        return try {
            objectMapper.readValue(jsonContent, BookRecommendation::class.java)
        } catch (e: Exception) {
            BookRecommendation(
                    title = "Parsing Failed",
                    author = "Unknown",
                    genre = genre,
                    summary = jsonContent
            )
        }
    }

    @PostMapping("/with-metadata")
    fun chatWithMetadata(@RequestBody request: QuestionRequest): ResponseWithMetadata {
        val prompt = Prompt(UserMessage(request.question))
        val response: ChatResponse = chatModel.call(prompt)
        val metadata = response.metadata
        val usage = metadata.usage

        return ResponseWithMetadata(
                content = response.result.output.text,
                model = metadata.model ?: "unknown",
                totalTokens = usage?.totalTokens?.toInt() ?: 0,
                promptTokens = usage?.promptTokens?.toInt() ?: 0,
                completionTokens = usage?.generationTokens?.toInt() ?: 0
        )
    }

    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamChat(@RequestParam question: String): Flux<String> {
        val prompt = Prompt(UserMessage(question))
        return chatModel.stream(prompt).map { response -> response.result?.output?.text ?: "" }
    }

    @PostMapping("/conversation")
    fun conversation(@RequestBody questions: List<String>): List<String> {
        val messages = mutableListOf<org.springframework.ai.chat.messages.Message>()
        val responses = mutableListOf<String>()

        messages.add(SystemMessage("You are a helpful assistant. Keep your answers concise."))

        questions.forEach { question ->
            messages.add(UserMessage(question))
            val prompt = Prompt(messages)
            val response = chatModel.call(prompt)
            val assistantResponse = response.result.output.text
            messages.add(AssistantMessage(assistantResponse))
            responses.add(assistantResponse)
        }

        return responses
    }
}
