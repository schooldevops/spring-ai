package com.example.springai.controller

import com.example.springai.model.ParseRequest
import com.example.springai.util.ListOutputParser
import org.springframework.ai.chat.client.ChatClient
import org.springframework.web.bind.annotation.*

/** ListOutputParser의 기본 사용법을 보여주는 컨트롤러 ChatClient를 사용하여 더 간결하고 유연한 API 호출 */
@RestController
@RequestMapping("/api/list-parser")
class ListParserController(private val chatClient: ChatClient.Builder) {

    /**
     * 기본 리스트 파싱 예제 POST http://localhost:8080/api/list-parser/parse Body: {"question": "5가지 프로그래밍
     * 언어를 나열해주세요"}
     */
    @PostMapping("/parse")
    fun parseList(@RequestBody request: ParseRequest): Map<String, Any> {
        val parser = ListOutputParser()
        val format = parser.format

        val text =
                chatClient
                        .build()
                        .prompt()
                        .system(
                                """
                다음 형식으로 응답해주세요:
                $format
                """.trimIndent()
                        )
                        .user(request.question)
                        .call()
                        .content()
                        ?: throw IllegalStateException("응답 없음")

        val items = parser.parse(text)

        return mapOf("items" to items, "count" to items.size)
    }

    /** CSV 형식 파싱 (쉼표 구분) POST http://localhost:8080/api/list-parser/csv */
    @PostMapping("/csv")
    fun parseCsv(@RequestBody request: ParseRequest): Map<String, Any> {
        val parser = ListOutputParser(separator = ",")
        val format = parser.format

        val text =
                chatClient
                        .build()
                        .prompt()
                        .system(
                                """
                다음 CSV 형식으로 응답해주세요:
                $format
                
                각 항목은 쉼표(,)로 구분해주세요.
                """.trimIndent()
                        )
                        .user(request.question)
                        .call()
                        .content()
                        ?: throw IllegalStateException("응답 없음")

        val items = parser.parse(text)

        return mapOf("items" to items, "count" to items.size)
    }

    /** 안전한 리스트 파싱 (에러 처리 포함) POST http://localhost:8080/api/list-parser/safe */
    @PostMapping("/safe")
    fun safeParseList(@RequestBody request: ParseRequest): Map<String, Any> {
        val parser = ListOutputParser()
        val format = parser.format

        return try {
            val text =
                    chatClient
                            .build()
                            .prompt()
                            .system("다음 형식으로 응답해주세요:\n$format")
                            .user(request.question)
                            .call()
                            .content()
                            ?: throw IllegalStateException("응답 없음")

            val items = parser.parse(text)

            mapOf("success" to true, "items" to items, "count" to items.size)
        } catch (e: Exception) {
            mapOf("success" to false, "error" to (e.message ?: "알 수 없는 오류"))
        }
    }
}
