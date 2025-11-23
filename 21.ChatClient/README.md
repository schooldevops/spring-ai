# 21. ChatClient API

## 📖 학습 목표

이 강의를 마친 후 다음을 달성할 수 있습니다:
- **ChatClient**의 개념과 사용 목적을 이해할 수 있습니다
- **Fluent API**를 사용하여 프롬프트를 구성할 수 있습니다
- **Entity Mapping**을 통해 AI 응답을 Kotlin 데이터 클래스로 변환할 수 있습니다
- **Streaming**을 사용하여 실시간 응답을 처리할 수 있습니다
- **Prompt Templates**를 활용하여 동적 프롬프트를 생성할 수 있습니다
- **Defaults와 Advisors**를 설정하여 고급 기능을 활용할 수 있습니다

---

## 🔑 핵심 키워드

이 장에서 다루는 핵심 키워드들:

1. **ChatClient** - Spring AI의 fluent API를 제공하는 채팅 클라이언트
2. **Fluent API** - 메서드 체이닝을 통한 직관적인 API 사용법
3. **Entity Mapping** - AI 응답을 Java/Kotlin 객체로 자동 변환
4. **Streaming** - 실시간으로 AI 응답을 받는 비동기 처리 방식
5. **Prompt Templates** - 변수를 포함한 동적 프롬프트 생성
6. **Advisors** - 프롬프트를 가로채고 수정하는 인터셉터 패턴

---

## 1. ChatClient 개요

### 1.1 ChatClient란?

**ChatClient**는 Spring AI에서 제공하는 fluent API 스타일의 채팅 인터페이스입니다. 기존의 `ChatModel`보다 더 직관적이고 사용하기 쉬운 API를 제공합니다.

#### ChatModel vs ChatClient

```kotlin
// ❌ ChatModel: 더 복잡한 API
val prompt = Prompt(
    listOf(
        SystemMessage("You are a helpful assistant"),
        UserMessage("Tell me a joke")
    )
)
val response = chatModel.call(prompt)
val text = response.results.firstOrNull()?.output?.text ?: ""

// ✅ ChatClient: 간결하고 직관적
val text = chatClient.prompt()
    .system("You are a helpful assistant")
    .user("Tell me a joke")
    .call()
    .content()
```

### 1.2 주요 특징

1. **Fluent API**: 메서드 체이닝으로 읽기 쉬운 코드 작성
2. **동기/비동기 지원**: `call()`과 `stream()` 모두 지원
3. **자동 변환**: 응답을 자동으로 데이터 클래스로 변환
4. **템플릿 지원**: 변수를 포함한 프롬프트 작성 가능
5. **확장 가능**: Advisors를 통한 기능 확장

### 1.3 기본 사용 패턴

```kotlin
val response = chatClient.prompt()
    .user("Your question here")
    .call()
    .content()
```

---

## 2. 샘플 구성

이 튜토리얼은 6개의 점진적인 샘플로 구성되어 있습니다. 각 샘플은 TDD(Test-Driven Development) 방식으로 작성되었습니다.

### Sample 01: Basic ChatClient Usage
**학습 내용:**
- ChatClient 생성 방법
- 기본 prompt() 메서드 사용
- call()과 content() 이해
- ChatResponse와 메타데이터 접근

**디렉토리:** [sample01-basic-chatclient](./sample01-basic-chatclient/)

**핵심 코드:**
```kotlin
val chatClient = ChatClient.create(chatModel)
val response = chatClient.prompt("Tell me a joke")
    .call()
    .content()
```

---

### Sample 02: Fluent API Deep Dive
**학습 내용:**
- prompt()의 3가지 오버로드 메서드
- user()와 system() 메서드 체이닝
- 다양한 응답 형식
- 메서드 조합 패턴

**디렉토리:** [sample02-fluent-api](./sample02-fluent-api/)

**핵심 코드:**
```kotlin
val response = chatClient.prompt()
    .system("You are a helpful assistant")
    .user("Explain quantum computing")
    .call()
    .content()
```

---

### Sample 03: Entity Mapping
**학습 내용:**
- entity() 메서드로 데이터 클래스 매핑
- ParameterizedTypeReference 사용
- List와 복잡한 타입 처리
- 에러 처리

**디렉토리:** [sample03-entity-mapping](./sample03-entity-mapping/)

**핵심 코드:**
```kotlin
data class ActorFilms(val actor: String, val movies: List<String>)

val actorFilms = chatClient.prompt()
    .user("Generate the filmography for Tom Hanks")
    .call()
    .entity(ActorFilms::class.java)
```

---

### Sample 04: Streaming Responses
**학습 내용:**
- stream() 메서드 사용
- Flux<String> 처리
- Flux<ChatResponse> 처리
- 스트림 집계 및 변환

**디렉토리:** [sample04-streaming](./sample04-streaming/)

**핵심 코드:**
```kotlin
val flux: Flux<String> = chatClient.prompt()
    .user("Write a long story")
    .stream()
    .content()
```

---

### Sample 05: Prompt Templates
**학습 내용:**
- 템플릿 변수 {variable} 사용
- param() 메서드로 변수 전달
- 커스텀 구분자 설정
- JSON과 함께 사용하기

**디렉토리:** [sample05-prompt-templates](./sample05-prompt-templates/)

**핵심 코드:**
```kotlin
val response = chatClient.prompt()
    .user { u -> u
        .text("Tell me about {topic}")
        .param("topic", "Spring AI")
    }
    .call()
    .content()
```

---

### Sample 06: Defaults and Advisors
**학습 내용:**
- 기본 시스템 텍스트 설정
- 기본 파라미터 설정
- Advisor API 기초
- QuestionAnswerAdvisor (RAG)
- Chat Memory 개념

**디렉토리:** [sample06-defaults-and-advisors](./sample06-defaults-and-advisors/)

**핵심 코드:**
```kotlin
// Configuration
val chatClient = ChatClient.builder(chatModel)
    .defaultSystem("You are a helpful assistant")
    .build()

// Runtime - system text already set
val response = chatClient.prompt()
    .user("Your question")
    .call()
    .content()
```

---

## 3. 학습 순서

각 샘플을 순서대로 학습하는 것을 권장합니다:

1. **Sample 01** - ChatClient의 기본 개념과 생성 방법 이해
2. **Sample 02** - Fluent API의 다양한 사용법 숙지
3. **Sample 03** - 실용적인 데이터 변환 방법 학습
4. **Sample 04** - 비동기 스트리밍 처리 이해
5. **Sample 05** - 동적 프롬프트 생성 기법 습득
6. **Sample 06** - 고급 기능과 확장 방법 탐구

---

## 4. 공통 설정

모든 샘플은 다음과 같은 공통 설정을 사용합니다:

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

### 환경 변수 설정

```bash
export OPENAI_API_KEY=your-api-key-here
```

---

## 5. TDD 접근 방식

모든 샘플은 Test-Driven Development 방식으로 작성되었습니다:

1. **테스트 먼저 작성** - 기대하는 동작을 테스트로 정의
2. **최소 구현** - 테스트를 통과하는 최소한의 코드 작성
3. **리팩토링** - 코드 품질 개선
4. **반복** - 다음 기능으로 진행

### 테스트 예시

```kotlin
@SpringBootTest
class BasicChatClientControllerTest {
    
    @Autowired
    lateinit var chatClient: ChatClient
    
    @Test
    fun `should return response from simple prompt`() {
        // Given
        val question = "What is 2+2?"
        
        // When
        val response = chatClient.prompt(question)
            .call()
            .content()
        
        // Then
        assertThat(response).isNotEmpty()
        assertThat(response).contains("4")
    }
}
```

---

## 6. ChatClient vs ChatModel 비교

| 특징 | ChatModel | ChatClient |
|------|-----------|------------|
| **API 스타일** | 명시적, 상세함 | Fluent, 간결함 |
| **프롬프트 구성** | Prompt 객체 생성 필요 | 메서드 체이닝 |
| **응답 처리** | 수동 추출 | 자동 변환 지원 |
| **템플릿** | 별도 처리 필요 | 내장 지원 |
| **학습 곡선** | 높음 | 낮음 |
| **유연성** | 매우 높음 | 높음 |
| **권장 사용** | 복잡한 시나리오 | 일반적인 사용 |

---

## 7. 주요 메서드 레퍼런스

### ChatClient 생성

```kotlin
// Autoconfigured Builder 사용
@Bean
fun chatClient(builder: ChatClient.Builder): ChatClient {
    return builder.build()
}

// ChatModel로부터 직접 생성
val chatClient = ChatClient.create(chatModel)
```

### Prompt 메서드

```kotlin
// 1. 인자 없음 - Fluent API 시작
chatClient.prompt()

// 2. String - 편의 메서드
chatClient.prompt("Your question")

// 3. Prompt 객체
chatClient.prompt(Prompt(...))
```

### 응답 메서드

```kotlin
// String 응답
.call().content()

// ChatResponse (메타데이터 포함)
.call().chatResponse()

// Entity 매핑
.call().entity(DataClass::class.java)

// 스트리밍
.stream().content()  // Flux<String>
.stream().chatResponse()  // Flux<ChatResponse>
```

---

## 8. 베스트 프랙티스

### ✅ 권장사항

1. **ChatClient를 Bean으로 등록**
```kotlin
@Configuration
class ChatClientConfig {
    @Bean
    fun chatClient(builder: ChatClient.Builder): ChatClient {
        return builder
            .defaultSystem("You are a helpful assistant")
            .build()
    }
}
```

2. **Entity 매핑 활용**
```kotlin
// ✅ 타입 안전한 응답
val data = chatClient.prompt()
    .user("Generate data")
    .call()
    .entity(MyData::class.java)
```

3. **템플릿 변수 사용**
```kotlin
// ✅ 재사용 가능한 프롬프트
.user { u -> u
    .text("Explain {topic} in {language}")
    .param("topic", topic)
    .param("language", "Korean")
}
```

### ❌ 피해야 할 패턴

1. **매번 ChatClient 생성**
```kotlin
// ❌ 비효율적
fun process() {
    val chatClient = ChatClient.create(chatModel)
    // ...
}
```

2. **하드코딩된 프롬프트**
```kotlin
// ❌ 유연성 부족
.user("Tell me about Spring AI")
```

3. **에러 처리 누락**
```kotlin
// ❌ 예외 처리 없음
val entity = chatClient.prompt()
    .user("...")
    .call()
    .entity(MyClass::class.java)  // 파싱 실패 가능
```

---

## 9. 트러블슈팅

### 문제 1: ChatClient Bean을 찾을 수 없음

**증상:**
```
No qualifying bean of type 'ChatClient.Builder'
```

**해결:**
```kotlin
// application.yml에 AI 설정 추가
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
```

### 문제 2: Entity 매핑 실패

**증상:**
```
Cannot deserialize value of type...
```

**해결:**
```kotlin
// 명확한 프롬프트 제공
.system("Return response in JSON format matching the schema")
.user("Generate data")
```

### 문제 3: 스트리밍 응답 처리

**증상:**
```
Flux가 비어있거나 완료되지 않음
```

**해결:**
```kotlin
// 적절한 구독 및 블로킹
val content = flux
    .collectList()
    .block()
    ?.joinToString("")
```

---

## 10. 참고 자료

- [Spring AI ChatClient 공식 문서](https://docs.spring.io/spring-ai/reference/api/chatclient.html)
- [Spring AI Reference](https://docs.spring.io/spring-ai/reference/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Project Reactor](https://projectreactor.io/)

---

## 11. 다음 단계

ChatClient를 마스터한 후 다음 주제로 진행하세요:

- **Function Calling** - AI가 함수를 호출하도록 하기
- **RAG (Retrieval Augmented Generation)** - 문서 기반 응답 생성
- **Chat Memory** - 대화 기록 유지
- **Multimodal** - 이미지와 텍스트 함께 처리

---

**시작하기**: [Sample 01: Basic ChatClient Usage](./sample01-basic-chatclient/)
