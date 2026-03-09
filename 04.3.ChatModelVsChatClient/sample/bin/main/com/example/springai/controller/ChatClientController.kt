package com.example.springai.controller

import com.example.springai.dto.*
import org.springframework.ai.chat.client.ChatClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

/**
 * ChatClient 기본 사용법 컨트롤러
 *
 * ChatClient는 Spring AI의 고수준(High-Level) API입니다.
 */
@RestController
@RequestMapping("/api/chatclient")
class ChatClientController(private val chatClient: ChatClient) {

    @PostMapping("/basic")
    fun basicChat(@RequestBody request: QuestionRequest): String {
        return chatClient.prompt(request.question).call().content() ?: ""
    }

    @PostMapping("/with-system")
    fun chatWithSystem(@RequestBody request: ContextQuestionRequest): String {
        return chatClient
                .prompt()
                .system(request.systemMessage)
                .user(request.userMessage)
                .call()
                .content()
                ?: ""
    }

    @PostMapping("/expert")
    fun askExpert(@RequestBody request: ExpertQuestionRequest): String {
        return chatClient
                .prompt()
                .system(
                        "You are an expert in ${request.domain}. Provide detailed and accurate information."
                )
                .user(request.question)
                .call()
                .content()
                ?: ""
    }

    @PostMapping("/book-recommendation")
    fun recommendBook(@RequestParam genre: String): BookRecommendation? {
        return chatClient
                .prompt()
                .system("You are a book expert.")
                .user("Recommend a $genre book")
                .call()
                .entity(BookRecommendation::class.java)
    }

    @PostMapping("/with-metadata")
    fun chatWithMetadata(@RequestBody request: QuestionRequest): ResponseWithMetadata {
        val response = chatClient.prompt(request.question).call().chatResponse()
        val metadata = response?.metadata
        val usage = metadata?.usage

        return ResponseWithMetadata(
                content = response?.result?.output?.text ?: "",
                model = metadata?.model ?: "unknown",
                totalTokens = usage?.totalTokens?.toInt() ?: 0,
                promptTokens = usage?.promptTokens?.toInt() ?: 0,
                completionTokens = usage?.generationTokens?.toInt() ?: 0
        )
    }

    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamChat(@RequestParam question: String): Flux<String> {
        return chatClient.prompt(question).stream().content()
    }

    @PostMapping("/translate")
    fun translate(@RequestBody request: TranslationRequest): String {
        return chatClient
                .prompt()
                .user { u ->
                    u.text("Translate '{text}' to {language}")
                            .param("text", request.text)
                            .param("language", request.targetLanguage)
                }
                .call()
                .content()
                ?: ""
    }

    @PostMapping("/movie-recommendation")
    fun recommendMovie(@RequestParam genre: String): MovieRecommendation? {
        return chatClient
                .prompt()
                .system("You are a movie critic and expert.")
                .user("Recommend a highly-rated $genre movie with details")
                .call()
                .entity(MovieRecommendation::class.java)
    }

    @PostMapping("/extract-product")
    fun extractProduct(@RequestBody description: String): Product? {
        return chatClient
                .prompt()
                .system("Extract product information from the description.")
                .user(description)
                .call()
                .entity(Product::class.java)
    }

    @PostMapping("/multi-question")
    fun multiQuestion(@RequestBody questions: List<String>): List<String> {
        return questions.map { question ->
            chatClient
                    .prompt()
                    .system("You are a helpful assistant. Keep your answers concise.")
                    .user(question)
                    .call()
                    .content()
                    ?: ""
        }
    }
}
