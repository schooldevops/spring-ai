# Sample 01: Basic ChatClient Usage

## 학습 목표

이 샘플을 완료하면 다음을 할 수 있습니다:
- ChatClient를 생성하고 Bean으로 등록하는 방법 이해
- 가장 간단한 prompt() 메서드 사용법 습득
- call()과 content()를 사용한 응답 처리
- ChatResponse를 통한 메타데이터 접근 방법 학습

---

## 핵심 개념

### 1. ChatClient 생성

ChatClient는 `ChatClient.Builder`를 통해 생성됩니다:

```kotlin
@Configuration
class ChatClientConfig {
    @Bean
    fun chatClient(builder: ChatClient.Builder): ChatClient {
        return builder.build()
    }
}
```

Spring AI가 자동으로 `ChatClient.Builder`를 제공하므로, 이를 주입받아 사용하면 됩니다.

### 2. 기본 사용 패턴

**패턴 1: 간단한 문자열 프롬프트**
```kotlin
val response = chatClient.prompt("Your question")
    .call()
    .content()
```

**패턴 2: Fluent API**
```kotlin
val response = chatClient.prompt()
    .user("Your question")
    .call()
    .content()
```

**패턴 3: System + User 메시지**
```kotlin
val response = chatClient.prompt()
    .system("You are a helpful assistant")
    .user("Your question")
    .call()
    .content()
```

---

## 구현된 엔드포인트

### 1. Simple Prompt
```http
POST /api/basic/simple
Content-Type: application/json

{
    "message": "What is 2+2?"
}
```

가장 간단한 형태의 ChatClient 사용법입니다.

### 2. Fluent API
```http
POST /api/basic/fluent
Content-Type: application/json

{
    "message": "Tell me a joke"
}
```

Fluent API 스타일로 프롬프트를 구성합니다.

### 3. With Metadata
```http
POST /api/basic/metadata
Content-Type: application/json

{
    "message": "Explain Spring AI"
}
```

ChatResponse를 통해 메타데이터(모델명, 토큰 사용량 등)에 접근합니다.

**응답 예시:**
```json
{
    "content": "Spring AI is...",
    "model": "gpt-4o-mini",
    "tokensUsed": 150
}
```

### 4. With System Message
```http
POST /api/basic/with-system
Content-Type: application/json

{
    "message": "What is quantum computing?"
}
```

System 메시지로 AI의 역할을 정의하고, User 메시지로 질문합니다.

---

## TDD 접근 방식

이 샘플은 Test-Driven Development 방식으로 작성되었습니다:

### 1. 테스트 먼저 작성
```kotlin
@Test
fun `should return response from simple string prompt`() {
    // Given
    val question = "What is 2+2?"
    
    // When
    val response = chatClient.prompt(question)
        .call()
        .content()
    
    // Then
    assertThat(response).contains("4")
}
```

### 2. 구현
테스트를 통과하도록 최소한의 코드를 작성합니다.

### 3. 리팩토링
코드 품질을 개선합니다.

---

## 실행 방법

### 1. 환경 변수 설정
```bash
export OPENAI_API_KEY=your-api-key-here
```

### 2. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 3. 테스트 실행
```bash
./gradlew test
```

### 4. HTTP 요청 테스트
`test.http` 파일을 사용하거나 curl 명령어로 테스트:

```bash
curl -X POST http://localhost:9100/api/basic/simple \
  -H "Content-Type: application/json" \
  -d '{"message": "What is 2+2?"}'
```

---

## 코드 구조

```
sample01-basic-chatclient/
├── src/
│   ├── main/kotlin/com/example/chatclient/
│   │   ├── ChatClientBasicApplication.kt
│   │   ├── config/
│   │   │   └── ChatClientConfig.kt
│   │   ├── controller/
│   │   │   └── BasicChatClientController.kt
│   │   └── model/
│   │       └── CommonModels.kt
│   └── test/kotlin/com/example/chatclient/
│       └── controller/
│           └── BasicChatClientControllerTest.kt
├── build.gradle.kts
├── test.http
└── README.md
```

---

## 주요 학습 포인트

### ✅ ChatClient 생성
- `ChatClient.Builder`를 주입받아 사용
- Bean으로 등록하여 재사용

### ✅ 세 가지 prompt() 오버로드
1. `prompt()` - 빈 시작점
2. `prompt(String)` - 편의 메서드
3. `prompt(Prompt)` - Prompt 객체 전달

### ✅ 응답 처리
- `call().content()` - String 응답
- `call().chatResponse()` - 메타데이터 포함 응답

### ✅ 메타데이터 접근
```kotlin
val chatResponse = chatClient.prompt("...")
    .call()
    .chatResponse()

val model = chatResponse.metadata?.model
val tokens = chatResponse.metadata?.usage?.totalTokens
```

---

## 다음 단계

Sample 01을 완료했다면 다음으로 진행하세요:
- **[Sample 02: Fluent API Deep Dive](../sample02-fluent-api/)** - Fluent API의 고급 사용법

---

## 참고 자료

- [Spring AI ChatClient 문서](https://docs.spring.io/spring-ai/reference/api/chatclient.html)
- [메인 튜토리얼](../README.md)
