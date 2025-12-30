# 9.1: Function Calling 개념과 활용

## 📖 학습 목표

이 강의를 마친 후 다음을 달성할 수 있습니다:
- **Function Calling**의 개념과 필요성을 이해할 수 있습니다
- LLM이 **Kotlin 함수(Spring Bean)**를 호출하도록 설정할 수 있습니다
- **java.util.function.Function**을 사용하여 함수를 정의할 수 있습니다
- **@Description** 어노테이션으로 함수 설명을 추가할 수 있습니다
- 함수를 **@Bean**으로 등록하면 Spring AI가 자동으로 감지하여 사용할 수 있습니다
- LLM이 상황에 맞게 적절한 함수를 선택하고 호출하는 과정을 이해할 수 있습니다

---

## 🔑 핵심 키워드

이 장에서 다루는 핵심 키워드들:

1. **Function Calling** - LLM이 개발자가 정의한 함수를 호출하는 기능
2. **Tool** - LLM이 사용할 수 있는 함수를 의미하는 용어
3. **@Bean 등록** - Spring Bean으로 함수를 등록하여 자동 감지되게 만들기
4. **자동 감지** - Spring AI가 Bean으로 등록된 함수를 자동으로 감지하여 사용
5. **java.util.function.Function** - 함수형 인터페이스로 함수 정의
6. **@Description** - 함수의 목적과 사용법을 설명하는 어노테이션

---

## 1. Function Calling이란?

### 1.1 Function Calling의 개념

**Function Calling**은 LLM(Large Language Model)이 사용자의 요청을 분석하여, 개발자가 미리 정의한 함수를 자동으로 호출할 수 있게 해주는 기능입니다.

#### 전통적인 방식의 한계

```kotlin
// ❌ 전통적인 방식: LLM이 직접 처리할 수 없는 작업
val userRequest = "오늘 서울 날씨 어때?"
val response = chatModel.call(Prompt(UserMessage(userRequest)))
// LLM은 날씨 정보를 모르므로 환각(Hallucination) 발생 가능
```

#### Function Calling 방식

```kotlin
// ✅ Function Calling: LLM이 함수를 호출하여 실제 데이터 사용
val userRequest = "오늘 서울 날씨 어때?"
// 1. LLM이 "날씨 조회 함수"를 호출해야 한다고 판단
// 2. 실제 날씨 API를 호출
// 3. 결과를 바탕으로 자연어 응답 생성
```

### 1.2 Function Calling의 동작 원리

```
사용자 요청
    ↓
LLM 분석 
    ↓
함수 호출 필요 여부 판단
    ↓
함수 선택 및 호출 (개발자가 정의한 함수)
    ↓
함수 실행 결과 반환
    ↓
LLM이 결과를 바탕으로 최종 응답 생성
```

### 1.3 Function Calling의 장점

1. **실시간 데이터 접근**: LLM이 최신 정보를 활용할 수 있음
2. **환각 방지**: 실제 데이터를 기반으로 응답하므로 오류 감소
3. **외부 시스템 연동**: 데이터베이스, API, 서비스와 통합 가능
4. **확장성**: 새로운 기능을 함수로 추가하여 LLM이 사용 가능

---

## 2. Spring AI에서 Function Calling 구현하기

### 2.1 기본 구조

Spring AI에서 Function Calling을 구현하려면:

1. **함수 정의**: `java.util.function.Function` 인터페이스 구현
2. **함수 설명**: `@Description` 어노테이션으로 함수 설명 추가
3. **Bean 등록**: `@Bean` 어노테이션으로 Spring Bean으로 등록
4. **자동 감지**: Spring AI가 Bean으로 등록된 함수를 자동으로 감지하여 사용

### 2.2 함수 정의 예제

#### 간단한 계산 함수

```kotlin
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Function

@Configuration
class FunctionConfiguration {
    
    @Bean
    fun calculatorFunction(): Function<CalculatorRequest, CalculatorResponse> {
        return Function { request ->
            val result = when (request.operation) {
                "add" -> request.a + request.b
                "subtract" -> request.a - request.b
                "multiply" -> request.a * request.b
                "divide" -> if (request.b != 0) request.a / request.b else 0
                else -> throw IllegalArgumentException("Unknown operation")
            }
            CalculatorResponse(result)
        }
    }
}

data class CalculatorRequest(
    val operation: String,
    val a: Double,
    val b: Double
)

data class CalculatorResponse(
    val result: Double
)
```

> 💡 **참고**: Spring AI 1.0.0-M6에서는 함수를 `java.util.function.Function`으로 정의합니다.

---

## 3. @Description 어노테이션 사용하기

### 3.1 @Description의 역할

`@Description` 어노테이션은 LLM이 함수를 이해하고 적절히 사용할 수 있도록 함수의 목적과 사용법을 설명합니다.

### 3.2 @Description 사용 예제

```kotlin
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Description
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.function.Function

@Configuration
class FunctionConfiguration {
    
    @Bean
    @Description("두 숫자에 대해 사칙연산(덧셈, 뺄셈, 곱셈, 나눗셈)을 수행합니다. operation은 'add', 'subtract', 'multiply', 'divide' 중 하나여야 합니다.")
    fun calculatorFunction(): Function<CalculatorRequest, CalculatorResponse> {
        return Function { request ->
            val result = when (request.operation.lowercase()) {
                "add", "+" -> request.a + request.b
                "subtract", "-" -> request.a - request.b
                "multiply", "*" -> request.a * request.b
                "divide", "/" -> {
                    if (request.b == 0.0) {
                        throw IllegalArgumentException("Division by zero is not allowed")
                    }
                    request.a / request.b
                }
                else -> throw IllegalArgumentException("Unknown operation: ${request.operation}")
            }
            CalculatorResponse(result)
        }
    }
    
    @Bean
    @Description("현재 시간을 반환합니다. timezone이 제공되면 해당 시간대로, 없으면 시스템 기본 시간대를 사용합니다.")
    fun getCurrentTimeFunction(): Function<TimeRequest, TimeResponse> {
        return Function { request ->
            val zoneId = request.timezone?.let { 
                try {
                    ZoneId.of(it)
                } catch (e: Exception) {
                    ZoneId.systemDefault()
                }
            } ?: ZoneId.systemDefault()
            
            val now = LocalDateTime.now(zoneId)
            val formatted = now.format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            )
            
            TimeResponse(
                time = formatted,
                timezone = zoneId.id
            )
        }
    }
}

data class TimeRequest(
    val timezone: String? = null
)

data class TimeResponse(
    val time: String
)
```

### 3.3 @Description 작성 가이드

좋은 설명은 다음을 포함합니다:
- **함수의 목적**: 무엇을 하는 함수인가?
- **파라미터 설명**: 각 파라미터의 의미와 형식
- **반환값 설명**: 어떤 값을 반환하는가?
- **사용 예시**: 언제 이 함수를 사용해야 하는가?

---

## 4. 함수 사용하기

### 4.1 함수 자동 감지

Spring AI 1.0.0-M6에서는 함수를 `@Bean`과 `@Description`으로 등록하면, Spring AI가 자동으로 감지하여 사용할 수 있습니다. 별도의 래핑이나 옵션 설정 없이 일반 `Prompt`로 호출하면 됩니다.

> ⚠️ **주의**: Ollama는 Function Calling을 완전히 지원하지 않을 수 있습니다. Function Calling을 제대로 테스트하려면 OpenAI GPT-4나 GPT-3.5-turbo를 사용하는 것을 권장합니다.

### 4.2 함수를 사용하여 호출하기

```kotlin
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.stereotype.Service

@Service
class FunctionCallService(
    private val chatModel: ChatModel
) {
    
    fun callWithFunction(userMessage: String): String {
        // 함수는 Bean으로 등록되어 있으므로 일반 Prompt로 호출
        // LLM이 필요에 따라 함수를 호출할 수 있습니다
        val prompt = Prompt(UserMessage(userMessage))
        val response = chatModel.call(prompt)
        return response.results.firstOrNull()?.output?.text ?: "응답 없음"
    }
}
```

### 4.3 여러 함수 함께 사용하기

여러 함수를 Bean으로 등록하면, LLM이 상황에 맞게 적절한 함수를 선택하여 호출합니다:

```kotlin
@Service
class MultiFunctionService(
    private val chatModel: ChatModel
) {
    
    fun callWithMultipleFunctions(userMessage: String): String {
        // 모든 함수가 Bean으로 등록되어 있으므로,
        // LLM이 필요에 따라 적절한 함수를 선택하여 호출합니다
        val prompt = Prompt(UserMessage(userMessage))
        val response = chatModel.call(prompt)
        return response.results.firstOrNull()?.output?.text ?: "응답 없음"
    }
}
```

---

## 5. 실제 사용 예제

### 5.1 계산기 함수 예제

#### 함수 정의

```kotlin
@Configuration
class CalculatorFunctionConfig {
    
    @Bean
    @Description("두 숫자에 대해 사칙연산(덧셈, 뺄셈, 곱셈, 나눗셈)을 수행합니다.")
    fun calculatorFunction(): Function<CalculatorRequest, CalculatorResponse> {
        return Function { request ->
            val result = when (request.operation.lowercase()) {
                "add", "+" -> request.a + request.b
                "subtract", "-" -> request.a - request.b
                "multiply", "*" -> request.a * request.b
                "divide", "/" -> {
                    if (request.b == 0.0) {
                        throw IllegalArgumentException("Division by zero is not allowed")
                    }
                    request.a / request.b
                }
                else -> throw IllegalArgumentException("Unknown operation: ${request.operation}")
            }
            CalculatorResponse(result)
        }
    }
}

data class CalculatorRequest(
    @Description("연산 종류: 'add', 'subtract', 'multiply', 'divide'")
    val operation: String,
    
    @Description("첫 번째 숫자")
    val a: Double,
    
    @Description("두 번째 숫자")
    val b: Double
)

data class CalculatorResponse(
    @Description("계산 결과")
    val result: Double
)
```

#### 사용 예제

```kotlin
@RestController
class CalculatorController(
    private val functionCallService: FunctionCallService
) {
    
    @PostMapping("/api/calculate")
    fun calculate(@RequestBody request: Map<String, String>): Map<String, Any> {
        val userMessage = request["message"] ?: return mapOf("error" to "Message is required")
        
        val response = functionCallService.callWithCalculator(userMessage)
        
        return mapOf(
            "userMessage" to userMessage,
            "aiResponse" to response
        )
    }
}
```

#### 테스트

```bash
curl -X POST http://localhost:8080/api/calculate \
  -H "Content-Type: application/json" \
  -d '{"message": "10과 5를 더해줘"}'
```

**예상 응답:**
```json
{
  "userMessage": "10과 5를 더해줘",
  "aiResponse": "10과 5를 더하면 15입니다."
}
```

### 5.2 시간 조회 함수 예제

```kotlin
@Configuration
class TimeFunctionConfig {
    
    @Bean
    @Description("현재 시간을 반환합니다. timezone이 제공되면 해당 시간대로, 없으면 시스템 기본 시간대를 사용합니다.")
    fun getCurrentTimeFunction(): Function<TimeRequest, TimeResponse> {
        return Function { request ->
            val zoneId = request.timezone?.let { 
                try {
                    java.time.ZoneId.of(it)
                } catch (e: Exception) {
                    java.time.ZoneId.systemDefault()
                }
            } ?: java.time.ZoneId.systemDefault()
            
            val now = java.time.LocalDateTime.now(zoneId)
            val formatted = now.format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            )
            
            TimeResponse(
                time = formatted,
                timezone = zoneId.id
            )
        }
    }
}

data class TimeRequest(
    @Description("시간대 (예: 'Asia/Seoul', 'America/New_York'). 선택사항입니다.")
    val timezone: String? = null
)

data class TimeResponse(
    @Description("현재 시간 (형식: yyyy-MM-dd HH:mm:ss)")
    val time: String,
    
    @Description("사용된 시간대")
    val timezone: String
)
```

---

## 6. Function Calling 동작 흐름

### 6.1 전체 흐름도

```
1. 사용자 요청
   "10과 5를 더해줘"
        ↓
2. LLM이 요청 분석
   - 계산이 필요함을 인식
   - "calculator" 함수 사용 결정
        ↓
3. 함수 호출
   CalculatorRequest(operation="add", a=10.0, b=5.0)
        ↓
4. 함수 실행
   CalculatorResponse(result=15.0)
        ↓
5. LLM이 결과를 자연어로 변환
   "10과 5를 더하면 15입니다."
        ↓
6. 최종 응답 반환
```

### 6.2 LLM의 함수 선택 로직

LLM은 다음을 고려하여 함수를 선택합니다:
- **사용자 요청의 의도**: 무엇을 하고 싶은가?
- **함수 설명**: 각 함수가 무엇을 하는가?
- **파라미터 매칭**: 요청을 함수 파라미터로 변환할 수 있는가?

---

## 7. 고급 활용

### 7.1 조건부 함수 호출

LLM이 여러 함수 중에서 선택할 수 있도록 설정:

```kotlin
@Service
class SmartFunctionService(
    private val chatModel: ChatModel
) {
    
    fun smartCall(userMessage: String): String {
        // 모든 함수가 Bean으로 등록되어 있으므로,
        // LLM이 필요에 따라 적절한 함수를 선택하여 호출합니다
        val prompt = Prompt(UserMessage(userMessage))
        val response = chatModel.call(prompt)
        
        return response.results.firstOrNull()?.output?.text ?: "응답 없음"
    }
}
```

### 7.2 함수 체이닝

한 함수의 결과를 다른 함수에 전달:

```kotlin
// LLM이 여러 함수를 순차적으로 호출할 수 있음
// 예: "계산 결과를 저장하고, 저장된 결과를 조회해줘"
```

---

## 8. 주의사항 및 모범 사례

### 8.1 주의사항

1. **함수 설명의 중요성**: LLM이 함수를 올바르게 사용하려면 명확한 설명이 필요합니다
2. **에러 처리**: 함수 내부에서 예외를 적절히 처리해야 합니다
3. **보안**: 함수는 외부 시스템에 접근할 수 있으므로 보안을 고려해야 합니다
4. **비용**: 함수 호출은 추가 비용이 발생할 수 있습니다
5. **모델 지원**: Ollama는 Function Calling을 완전히 지원하지 않을 수 있으므로, OpenAI GPT-4나 GPT-3.5-turbo 사용을 권장합니다

### 8.2 모범 사례

1. **명확한 함수 이름**: 함수 이름은 그 목적을 명확히 나타내야 합니다
2. **상세한 설명**: @Description에 함수의 목적, 파라미터, 반환값을 명확히 설명합니다
3. **타입 안전성**: Kotlin의 data class를 활용하여 타입 안전성을 보장합니다
4. **에러 처리**: 함수 내부에서 발생할 수 있는 예외를 처리합니다
5. **테스트**: 각 함수를 단위 테스트로 검증합니다

---

## 9. 트러블슈팅

### 9.1 일반적인 문제들

#### 문제 1: 함수가 호출되지 않음

**증상**: LLM이 함수를 호출하지 않고 직접 응답함

**원인**:
- 함수 설명이 불명확함
- ChatOptions에 함수가 등록되지 않음
- LLM이 함수 호출이 필요하지 않다고 판단

**해결책**:
- 함수 설명을 더 명확하게 작성
- 사용자 요청을 더 구체적으로 작성
- 함수가 `@Bean`으로 올바르게 등록되었는지 확인
- OpenAI GPT-4나 GPT-3.5-turbo 사용 (Ollama는 Function Calling을 완전히 지원하지 않을 수 있음)

#### 문제 2: 함수 파라미터 매칭 실패

**증상**: 함수가 호출되지만 파라미터가 잘못됨

**원인**: LLM이 사용자 요청을 함수 파라미터로 변환하는 과정에서 오류

**해결책**:
- 파라미터에 @Description 추가
- 더 명확한 함수 설명 작성
- 사용자 요청을 더 구조화된 형식으로 작성

#### 문제 3: 함수 실행 오류

**증상**: 함수 호출 시 예외 발생

**원인**: 함수 내부 로직 오류 또는 잘못된 파라미터

**해결책**:
- 함수 내부에 에러 처리 추가
- 파라미터 검증 로직 추가
- 로깅을 통해 디버깅

---

## 10. 요약

### 10.1 핵심 내용 정리

1. **Function Calling**: LLM이 개발자가 정의한 함수를 호출하는 기능
2. **함수 정의**: `java.util.function.Function` 인터페이스 구현
3. **@Description**: 함수의 목적과 사용법을 설명하는 어노테이션
4. **@Bean 등록**: Spring Bean으로 함수를 등록하여 자동 감지
5. **자동 감지**: Spring AI가 Bean으로 등록된 함수를 자동으로 감지하여 사용

> ⚠️ **주의**: Spring AI 1.0.0-M6에서는 `FunctionCallbackWrapper`나 `ChatOptions`를 사용하지 않습니다. 함수를 `@Bean`으로 등록하면 자동으로 감지됩니다.

### 10.2 구현 패턴

```kotlin
// 1. 함수 정의 및 Bean 등록
@Configuration
class FunctionConfiguration {
    @Bean
    @Description("함수 설명")
    fun myFunction(): Function<Request, Response> {
        return Function { request ->
            // 로직 구현
            Response(...)
        }
    }
}

// 2. 함수 사용 (자동 감지)
@Service
class FunctionCallService(
    private val chatModel: ChatModel
) {
    fun callWithFunction(userMessage: String): String {
        // 함수는 Bean으로 등록되어 있으므로 일반 Prompt로 호출
        // LLM이 필요에 따라 함수를 호출할 수 있습니다
        val prompt = Prompt(UserMessage(userMessage))
        val response = chatModel.call(prompt)
        return response.results.firstOrNull()?.output?.text ?: "응답 없음"
    }
}
```

### 10.3 다음 학습 내용

이제 Function Calling의 기본 개념을 배웠으니, 다음 장에서는:
- **외부 API 연동**: 실제 외부 서비스와 연동하는 방법
- **복잡한 함수**: 여러 함수를 조합하여 사용하는 방법
- **에러 처리**: 함수 호출 시 발생할 수 있는 오류 처리

---

## 📚 참고 자료

- [Spring AI Function Calling 공식 문서](https://docs.spring.io/spring-ai/reference/api/function-calling.html)
- [Spring AI Chat API](https://docs.spring.io/spring-ai/reference/api/chat.html)

---

## ❓ 학습 확인 문제

다음 질문에 답할 수 있는지 확인해보세요:

1. Function Calling이 필요한 이유는 무엇인가요?
2. Spring AI에서 함수를 정의하는 방법은?
3. @Description 어노테이션의 역할은 무엇인가요?
4. 함수를 @Bean으로 등록하는 이유는?
5. LLM이 함수를 선택하는 기준은 무엇인가요?

---

**다음 장**: [9.2: Function Calling을 통한 외부 API 연동](../README.md#92-function-calling을-통한-외부-api-연동)

