package com.example.springai

import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt

/** ChatModel의 확장 함수 예제 Kotlin의 확장 함수를 활용하여 더 간편하게 사용할 수 있음 */

/** 간단한 문자열 메시지를 받아서 응답을 반환하는 확장 함수 */
fun ChatModel.simpleCall(message: String): String {
    val prompt = Prompt(UserMessage(message))
    return this.call(prompt).results.firstOrNull()?.output?.text ?: "응답을 생성할 수 없습니다."
}

/**
 * 안전한 호출 (예외 처리 포함)
 *
 * 이 함수가 안전한 이유:
 * 1. runCatching을 사용하여 모든 예외를 자동으로 캡처
 * 2. Result<String> 타입으로 성공/실패를 명시적으로 표현
 * 3. 호출자가 예외 처리 방법을 선택할 수 있음 (getOrNull, getOrDefault, fold 등)
 *
 * runCatching에 대한 설명:
 * - Kotlin 표준 라이브러리의 함수로, try-catch를 간결하게 표현
 * - 람다 블록 내의 모든 예외를 자동으로 캡처하여 Result 객체로 래핑
 * - 성공 시: Result.success(value) 반환
 * - 실패 시: Result.failure(exception) 반환
 *
 * Result 타입의 장점:
 * - 예외를 값으로 다룰 수 있어 함수형 프로그래밍 스타일 지원
 * - 호출자에게 실패 가능성을 타입 시스템으로 명시
 * - null 대신 실패 원인(예외)을 함께 전달 가능
 *
 * 사용 예시:
 * ```
 * val result = chatModel.safeCall("안녕하세요")
 *
 * // 방법 1: getOrNull() - 실패 시 null 반환
 * val response = result.getOrNull()
 *
 * // 방법 2: getOrDefault() - 실패 시 기본값 반환
 * val response = result.getOrDefault("기본 응답")
 *
 * // 방법 3: fold() - 성공/실패 각각 처리
 * result.fold(
 *     onSuccess = { response -> println("성공: $response") },
 *     onFailure = { error -> println("실패: ${error.message}") }
 * )
 *
 * // 방법 4: getOrThrow() - 실패 시 예외 재발생
 * val response = result.getOrThrow()
 * ```
 */
fun ChatModel.safeCall(message: String): Result<String> {
    return runCatching {
        val prompt = Prompt(UserMessage(message))
        // ChatModel 호출, 네트워크 오류, API 오류 등 모든 예외가 자동으로 캡처됨
        this.call(prompt).results.firstOrNull()?.output?.text
                ?: throw IllegalStateException("응답 없음")
    }
}

/** 여러 메시지를 받아서 응답을 반환하는 확장 함수 */
fun ChatModel.multiCall(vararg messages: String): String {
    val userMessages = messages.map { UserMessage(it) }
    val prompt = Prompt(userMessages)
    return this.call(prompt).results.firstOrNull()?.output?.text ?: "응답을 생성할 수 없습니다."
}
