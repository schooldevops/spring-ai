# 22. ChatModel API

## 📖 학습 목표

이 강의를 마친 후 다음을 달성할 수 있습니다:
- **ChatModel** 인터페이스의 개념과 사용법을 이해할 수 있습니다
- **StreamingChatModel**을 사용하여 실시간 응답을 처리할 수 있습니다
- **Message** 타입들을 활용하여 다양한 프롬프트를 구성할 수 있습니다
- **ChatOptions**를 설정하여 AI 모델의 동작을 제어할 수 있습니다
- **ChatResponse**와 **Generation** 구조를 이해하고 메타데이터에 접근할 수 있습니다

---

## 🔑 핵심 키워드

1. **ChatModel** - AI 모델과 통신하는 핵심 인터페이스
2. **StreamingChatModel** - 실시간 스트리밍 응답 인터페이스
3. **Prompt** - 메시지 리스트와 옵션을 캡슐화하는 요청 객체
4. **Message** - UserMessage, SystemMessage, AssistantMessage 등
5. **ChatOptions** - Temperature, MaxTokens 등 모델 옵션
6. **ChatResponse** - AI 모델의 응답 객체
7. **Generation** - 개별 응답 생성 결과

---

## 1. ChatModel API 개요

### 1.1 ChatModel이란?

**ChatModel**은 Spring AI에서 AI 모델과 통신하기 위한 핵심 인터페이스입니다. ChatClient보다 더 저수준의 API로, 더 많은 제어권을 제공합니다.

```kotlin
interface ChatModel {
    fun call(message: String): String
    fun call(prompt: Prompt): ChatResponse
}
```

### 1.2 ChatModel vs ChatClient

| 특징 | ChatModel | ChatClient |
|------|-----------|------------|
| **추상화 수준** | 낮음 (저수준) | 높음 (고수준) |
| **API 스타일** | 명시적 | Fluent |
| **제어 수준** | 높음 | 중간 |
| **사용 편의성** | 중간 | 높음 |
| **권장 사용** | 세밀한 제어 필요 시 | 일반적인 사용 |

---

## 2. 샘플 구성

### Sample 01: Basic ChatModel Usage
**학습 내용:**
- ChatModel 인터페이스 기본 사용법
- `call(String)` 간단한 호출
- `call(Prompt)` 상세 호출
- ChatResponse 구조 이해

**디렉토리:** [sample01-basic-chatmodel](./sample01-basic-chatmodel/)

---

### Sample 02: StreamingChatModel
**학습 내용:**
- StreamingChatModel 인터페이스
- `stream(String)` 메서드
- `stream(Prompt)` 메서드
- Flux<String>과 Flux<ChatResponse> 처리

**디렉토리:** [sample02-streaming](./sample02-streaming/)

---

### Sample 03: Messages and Prompt
**학습 내용:**
- Message 인터페이스와 구현체들
- UserMessage, SystemMessage, AssistantMessage
- Prompt 구성 방법
- Message 메타데이터

**디렉토리:** [sample03-messages-prompt](./sample03-messages-prompt/)

---

### Sample 04: ChatOptions
**학습 내용:**
- ChatOptions 인터페이스
- Temperature, MaxTokens, TopP 설정
- 모델별 특화 옵션
- Prompt에 옵션 적용

**디렉토리:** [sample04-chat-options](./sample04-chat-options/)

---

### Sample 05: ChatResponse and Generation
**학습 내용:**
- ChatResponse 구조 상세
- Generation 객체 접근
- Usage 메타데이터 (토큰 사용량)
- Finish Reason 이해

**디렉토리:** [sample05-chatresponse-generation](./sample05-chatresponse-generation/)

---

## 3. 공통 설정

### 필수 의존성

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}
```

### application.yml

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o-mini
          temperature: 0.7
```

---

## 4. 주요 인터페이스

### ChatModel

```kotlin
val chatModel: ChatModel

// 간단한 호출
val response: String = chatModel.call("Your question")

// 상세 호출
val prompt = Prompt("Your question")
val chatResponse: ChatResponse = chatModel.call(prompt)
```

### StreamingChatModel

```kotlin
val chatModel: ChatModel  // ChatModel은 StreamingChatModel도 구현

// 스트리밍
val flux: Flux<String> = chatModel.stream("Your question")
val content = flux.collectList().block()?.joinToString("")
```

### Prompt

```kotlin
val prompt = Prompt(
    listOf(
        SystemMessage("You are helpful"),
        UserMessage("Your question")
    ),
    ChatOptions.builder()
        .withTemperature(0.7)
        .build()
)
```

---

## 5. Message 타입

### UserMessage
사용자의 입력을 나타냅니다.

```kotlin
val userMessage = UserMessage("What is AI?")
```

### SystemMessage
시스템 지시사항을 나타냅니다.

```kotlin
val systemMessage = SystemMessage("You are a helpful assistant")
```

### AssistantMessage
AI 어시스턴트의 이전 응답을 나타냅니다.

```kotlin
val assistantMessage = AssistantMessage("AI stands for...")
```

---

## 6. ChatOptions

```kotlin
val options = ChatOptions.builder()
    .withModel("gpt-4o-mini")
    .withTemperature(0.7)
    .withMaxTokens(500)
    .withTopP(0.9)
    .build()

val prompt = Prompt("Your question", options)
```

### 주요 옵션

- **temperature** (0.0-2.0): 창의성 제어 (높을수록 창의적)
- **maxTokens**: 최대 생성 토큰 수
- **topP** (0.0-1.0): Nucleus sampling
- **frequencyPenalty**: 반복 억제
- **presencePenalty**: 주제 다양성

---

## 7. ChatResponse 구조

```kotlin
val chatResponse: ChatResponse = chatModel.call(prompt)

// 결과 접근
val result: Generation = chatResponse.result
val content: String = result.output.content

// 메타데이터
val metadata: ChatResponseMetadata = chatResponse.metadata
val usage: Usage = metadata.usage
val totalTokens: Int = usage.totalTokens
```

---

## 8. 학습 순서

1. **Sample 01** - ChatModel 기본 개념
2. **Sample 02** - 스트리밍 처리
3. **Sample 03** - 메시지 타입과 Prompt
4. **Sample 04** - 옵션 설정
5. **Sample 05** - 응답 구조 이해

---

## 9. ChatModel API 계층 구조

```
Model<I, O>
    ├── ChatModel
    │   ├── call(String): String
    │   └── call(Prompt): ChatResponse
    │
    └── StreamingChatModel
        ├── stream(String): Flux<String>
        └── stream(Prompt): Flux<ChatResponse>
```

---

## 10. 베스트 프랙티스

### ✅ 권장사항

1. **Prompt 객체 사용**
```kotlin
// ✅ 좋은 예
val prompt = Prompt(
    listOf(SystemMessage("..."), UserMessage("...")),
    options
)
val response = chatModel.call(prompt)
```

2. **옵션 명시**
```kotlin
// ✅ 명확한 옵션 설정
val options = ChatOptions.builder()
    .withTemperature(0.7)
    .withMaxTokens(500)
    .build()
```

3. **메타데이터 활용**
```kotlin
// ✅ 토큰 사용량 모니터링
val usage = chatResponse.metadata.usage
logger.info("Tokens used: ${usage.totalTokens}")
```

### ❌ 피해야 할 패턴

```kotlin
// ❌ 옵션 없이 사용
val response = chatModel.call("question")

// ❌ 메타데이터 무시
// 토큰 사용량을 확인하지 않으면 비용 관리 어려움
```

---

## 11. 참고 자료

- [Spring AI ChatModel 공식 문서](https://docs.spring.io/spring-ai/reference/api/chatmodel.html)
- [Spring AI Reference](https://docs.spring.io/spring-ai/reference/)
- [OpenAI API Documentation](https://platform.openai.com/docs/api-reference)

---

**시작하기**: [Sample 01: Basic ChatModel Usage](./sample01-basic-chatmodel/)
