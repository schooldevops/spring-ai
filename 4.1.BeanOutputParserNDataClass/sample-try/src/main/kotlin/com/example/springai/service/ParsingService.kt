package com.example.springai.service

import com.example.springai.util.BeanOutputParser
import com.example.springai.util.JsonCleaner
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service

/** 범용 파싱 서비스 ChatClient를 사용하여 더 간결하고 유창한 API 제공 */
@Service
class ParsingService(private val chatClientBuilder: ChatClient.Builder) {
    /**
     * 제네릭을 사용한 범용 파싱 메서드
     *
     * 사용 예: val userInfo: UserInfo = parsingService.parseResponse(
     * ```
     *     UserInfo::class.java,
     *     "사용자 정보를 추출해주세요.",
     *     "사용자 정보 텍스트"
     * ```
     * )
     */
    fun <T : Any> parseResponse(clazz: Class<T>, systemMessage: String, userMessage: String): T {
        val parser = BeanOutputParser(clazz)
        val format = parser.format

        // ChatClient의 fluent API 사용
        val text =
                chatClientBuilder
                        .build()
                        .prompt()
                        .system(
                                """
                $systemMessage
                
                응답 형식:
                $format
                
                반드시 위 형식에 맞춰 JSON만 응답해주세요.
                """.trimIndent()
                        )
                        .user(userMessage)
                        .call()
                        .content()
                        ?: throw IllegalStateException("응답 없음")

        // JSON 정리
        val cleanedJson = JsonCleaner.cleanJsonText(text)

        return parser.parse(cleanedJson)
    }
}
