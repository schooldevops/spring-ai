package com.example.springai.controller

import com.example.springai.dto.QuestionRequest
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.web.bind.annotation.*

/** ChatModel vs ChatClient 직접 비교 컨트롤러 */
@RestController
@RequestMapping("/api/comparison")
class ComparisonController(private val chatModel: ChatModel, private val chatClient: ChatClient) {

        @PostMapping("/basic/chatmodel")
        fun basicChatModel(@RequestBody request: QuestionRequest): Map<String, Any> {
                val startTime = System.currentTimeMillis()

                val prompt = Prompt(UserMessage(request.question))
                val response = chatModel.call(prompt)
                val content = response.result.output.text

                val endTime = System.currentTimeMillis()

                return mapOf(
                        "method" to "ChatModel",
                        "content" to content,
                        "linesOfCode" to 3,
                        "executionTimeMs" to (endTime - startTime)
                )
        }

        @PostMapping("/basic/chatclient")
        fun basicChatClient(@RequestBody request: QuestionRequest): Map<String, Any> {
                val startTime = System.currentTimeMillis()

                val content = chatClient.prompt(request.question).call().content() ?: ""

                val endTime = System.currentTimeMillis()

                return mapOf(
                        "method" to "ChatClient",
                        "content" to content,
                        "linesOfCode" to 1,
                        "executionTimeMs" to (endTime - startTime)
                )
        }

        @GetMapping("/system-message/chatmodel")
        fun systemMessageChatModel(): Map<String, Any> {
                val messages =
                        listOf(
                                org.springframework.ai.chat.messages.SystemMessage(
                                        "You are a poet"
                                ),
                                UserMessage("Write a haiku about spring")
                        )
                val prompt = Prompt(messages)
                val response = chatModel.call(prompt)
                val content = response.result.output.text

                return mapOf("method" to "ChatModel", "content" to content, "linesOfCode" to 5)
        }

        @GetMapping("/system-message/chatclient")
        fun systemMessageChatClient(): Map<String, Any> {
                val content =
                        chatClient
                                .prompt()
                                .system("You are a poet")
                                .user("Write a haiku about spring")
                                .call()
                                .content()
                                ?: ""

                return mapOf("method" to "ChatClient", "content" to content, "linesOfCode" to 1)
        }

        @GetMapping("/complexity")
        fun compareComplexity(): Map<String, Any> {
                return mapOf(
                        "scenarios" to
                                listOf(
                                        mapOf(
                                                "scenario" to "Basic Chat",
                                                "chatModel" to "3 lines",
                                                "chatClient" to "1 line",
                                                "winner" to "ChatClient"
                                        ),
                                        mapOf(
                                                "scenario" to "With System Message",
                                                "chatModel" to "5 lines",
                                                "chatClient" to "1 line (chained)",
                                                "winner" to "ChatClient"
                                        ),
                                        mapOf(
                                                "scenario" to "Entity Mapping",
                                                "chatModel" to "7+ lines (manual parsing)",
                                                "chatClient" to "1 line (auto mapping)",
                                                "winner" to "ChatClient"
                                        )
                                ),
                        "summary" to
                                mapOf(
                                        "chatModelAdvantages" to
                                                listOf("세밀한 제어", "메타데이터 직접 접근", "복잡한 대화 흐름 관리"),
                                        "chatClientAdvantages" to
                                                listOf(
                                                        "간결한 코드",
                                                        "높은 가독성",
                                                        "자동 Entity 매핑",
                                                        "낮은 학습 곡선"
                                                )
                                )
                )
        }

        @GetMapping("/recommendations")
        fun getRecommendations(): Map<String, Any> {
                return mapOf(
                        "useChatModel" to
                                listOf(
                                        "복잡한 대화 히스토리를 직접 관리해야 할 때",
                                        "토큰 사용량을 정밀하게 모니터링해야 할 때",
                                        "커스텀 메시지 타입이 필요할 때"
                                ),
                        "useChatClient" to
                                listOf(
                                        "일반적인 질의응답 시스템",
                                        "빠른 프로토타이핑",
                                        "Entity 매핑이 필요한 경우",
                                        "템플릿 기반 프롬프트 사용",
                                        "코드 가독성이 중요한 경우"
                                ),
                        "generalAdvice" to
                                "대부분의 경우 ChatClient를 사용하고, 특별한 요구사항이 있을 때만 ChatModel을 사용하세요."
                )
        }
}
