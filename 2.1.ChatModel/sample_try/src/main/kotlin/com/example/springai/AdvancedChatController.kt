package com.example.springai

import java.time.Instant
import org.springframework.ai.chat.messages.*
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/advanced")
class AdvancedChatController(private val chatModel: ChatModel) {
    @GetMapping("/conversation")
    fun conversation(): String {
        val messages =
                listOf(
                        SystemMessage("당신은 친절한 어시스트턴트입니다."),
                        UserMessage("저는 친절한 어시스턴트를 원합니다."),
                        AssistantMessage("무엇을 도와 드리면 될까요?"),
                        UserMessage("Spring AI 에 대해 설명을 해주세요.")
                )

        val prompt = Prompt(messages)
        val response = chatModel.call(prompt)

        return response.results.firstOrNull()?.output?.text ?: "응답 없음"
    }

    @PostMapping("/role")
    fun roleBasedChat(@RequestBody request: RoleChatRequest): String {
        val systemMessage =
                when (request.role.lowercase()) {
                    "teacher" -> SystemMessage("당신은 친절한 선생님 입니다. 교육적이고 이해하기 쉽게 설명해주세요.")
                    "doctor" -> SystemMessage("당신은 전문 의사입니다. 의학적 지식을 바탕으로 정확하게 답변해주세요.")
                    "chef" -> SystemMessage("당신은 유명한 셰프입니다. 요리에 대한 열정과 전문성을 보여주세요.")
                    else -> SystemMessage("당신은 도움이 되는 어시스턴트입니다.")
                }

        val prompt = Prompt(listOf(systemMessage, UserMessage(request.message)))
        val response = chatModel.call(prompt)

        return response.results.firstOrNull()?.output?.text ?: "응답 없음"
    }

    @PostMapping("/history")
    fun chatWithHistory(@RequestBody request: HistoryChatRequest): HistoryChatResponse {

        val message = mutableListOf<Message>()

        message.add(SystemMessage("당신은 친절한 어시스트턴트입니다."))
        message.add(UserMessage(request.message))

        val prompt = Prompt(message)
        val response = chatModel.call(prompt)

        val assistantText = response.results.firstOrNull()?.output?.text ?: "응답 없음"

        message.add(AssistantMessage(assistantText))

        return HistoryChatResponse(
                sessionId = request.sessionId,
                message = assistantText,
                messageCount = message.size,
                timestamp = Instant.now()
        )
    }

    @GetMapping("/metadata")
    fun metadataExample(): Map<String, Any> {
        val prompt = Prompt(UserMessage("간단히 인사해주세요"))
        val response = chatModel.call(prompt)

        val metadataMap =
                if (response.metadata != null) {
                    // ChatResponseMetadata를 Map으로 변환
                    mapOf(
                            "usage" to
                                    (response.metadata.usage?.let {
                                        mapOf(
                                                "promptTokens" to it.promptTokens,
                                                "generationTokens" to it.generationTokens,
                                                "totalTokens" to it.totalTokens
                                        )
                                    }
                                            ?: emptyMap<String, Any>()),
                            "model" to (response.metadata.model ?: "unknown")
                    )
                } else {
                    emptyMap<String, Any>()
                }

        return mapOf(
                "result" to (response.results.firstOrNull() != null),
                "output" to (response.results.firstOrNull()?.output != null),
                "text" to (response.results.firstOrNull()?.output?.text ?: "없음"),
                "metadata" to metadataMap
        )
    }
}

data class RoleChatRequest(val role: String, val message: String)

data class HistoryChatRequest(val sessionId: String, val message: String)

data class HistoryChatResponse(
        val sessionId: String,
        val message: String,
        val messageCount: Int,
        val timestamp: Instant
)
