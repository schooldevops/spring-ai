package com.example.springai.controller

import com.example.springai.model.ParseRequest
import com.example.springai.util.MapOutputParser
import org.springframework.ai.chat.client.ChatClient
import org.springframework.web.bind.annotation.*

/** MapOutputParser의 기본 사용법을 보여주는 컨트롤러 ChatClient를 사용하여 더 간결하고 유연한 API 호출 */
@RestController
@RequestMapping("/api/map-parser")
class MapParserController(private val chatClient: ChatClient.Builder) {

    /**
     * 기본 맵 파싱 예제 POST http://localhost:8080/api/map-parser/parse Body: {"question": "프로그래밍 언어별 특징을
     * Key-Value 형식으로 제공해주세요"}
     */
    @PostMapping("/parse")
    fun parseMap(@RequestBody request: ParseRequest): Map<String, Any> {
        val parser = MapOutputParser()
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

        val resultMap = parser.parse(text)

        return mapOf("data" to resultMap, "count" to resultMap.size)
    }

    /** 안전한 맵 파싱 (에러 처리 포함) POST http://localhost:8080/api/map-parser/safe */
    @PostMapping("/safe")
    fun safeParseMap(@RequestBody request: ParseRequest): Map<String, Any> {
        val parser = MapOutputParser()
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

            val resultMap = parser.parse(text)

            mapOf("success" to true, "data" to resultMap, "count" to resultMap.size)
        } catch (e: Exception) {
            mapOf("success" to false, "error" to (e.message ?: "알 수 없는 오류"))
        }
    }
}
