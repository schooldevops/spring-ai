# 04.3. ChatModel vs ChatClient 비교

## 📖 학습 목표

이 강의를 마친 후 다음을 달성할 수 있습니다:
- **ChatModel**과 **ChatClient**의 핵심 차이점을 이해할 수 있습니다
- 각 접근 방식의 **장단점**을 파악할 수 있습니다
- 상황에 맞는 **적절한 선택**을 할 수 있습니다
- 두 방식을 **실제 코드로 비교**할 수 있습니다
- **마이그레이션 전략**을 수립할 수 있습니다

---

## 🔑 핵심 개념

### ChatModel이란?

**ChatModel**은 Spring AI의 **저수준(Low-Level) API**로, AI 모델과의 직접적인 상호작용을 제공합니다.

```kotlin
// ChatModel 방식: 명시적이고 상세한 제어
val prompt = Prompt(
    listOf(
        SystemMessage("You are a helpful assistant"),
        UserMessage("What is Spring AI?")
    ),
    ChatOptions.builder()
        .withModel("gpt-4")
        .withTemperature(0.7)
        .build()
)
val response: ChatResponse = chatModel.call(prompt)
val content = response.result.output.content
```

### ChatClient란?

**ChatClient**는 Spring AI의 **고수준(High-Level) API**로, Fluent API 스타일의 간결한 인터페이스를 제공합니다.

```kotlin
// ChatClient 방식: 간결하고 직관적
val content = chatClient.prompt()
    .system("You are a helpful assistant")
    .user("What is Spring AI?")
    .options(ChatOptions.builder()
        .withModel("gpt-4")
        .withTemperature(0.7)
        .build())
    .call()
    .content()
```

---

## 📊 핵심 차이점 비교

| 비교 항목 | ChatModel | ChatClient |
|---------|-----------|------------|
| **API 레벨** | Low-Level | High-Level |
| **API 스타일** | 명시적, 상세함 | Fluent, 간결함 |
| **코드 가독성** | 보통 | 우수 |
| **학습 곡선** | 높음 | 낮음 |
| **유연성** | 매우 높음 | 높음 |
| **타입 안전성** | 높음 | 매우 높음 |
| **프롬프트 구성** | Prompt 객체 생성 | 메서드 체이닝 |
| **메시지 관리** | 수동 (List\<Message\>) | 자동 (메서드) |
| **응답 처리** | 수동 추출 | 자동 변환 |
| **템플릿 지원** | 별도 처리 | 내장 지원 |
| **Entity 매핑** | 수동 파싱 | 자동 변환 |
| **스트리밍** | Flux\<ChatResponse\> | Flux\<String\> 또는 Flux\<ChatResponse\> |
| **기본값 설정** | 매번 지정 | Builder로 설정 가능 |
| **Advisor 지원** | 없음 | 있음 (RAG, Memory 등) |
| **보일러플레이트** | 많음 | 적음 |
| **권장 사용 사례** | 복잡한 커스터마이징 | 일반적인 사용 |

---

## ✅ ChatModel의 장점

### 1. **세밀한 제어**
```kotlin
// 메시지별 세밀한 제어 가능
val messages = mutableListOf<Message>()
messages.add(SystemMessage("You are an expert"))
messages.add(UserMessage("Question 1"))
messages.add(AssistantMessage("Answer 1"))
messages.add(UserMessage("Question 2"))

val prompt = Prompt(messages, options)
```

### 2. **명시적인 구조**
```kotlin
// 모든 것이 명시적으로 표현됨
val chatOptions = ChatOptions.builder()
    .withModel("gpt-4")
    .withTemperature(0.8)
    .withMaxTokens(1000)
    .withTopP(0.9)
    .build()

val prompt = Prompt(userMessage, chatOptions)
val response = chatModel.call(prompt)
```

### 3. **저수준 접근**
```kotlin
// ChatResponse의 모든 메타데이터 접근
val response: ChatResponse = chatModel.call(prompt)
val generation: Generation = response.result
val metadata: ChatResponseMetadata = response.metadata
val usage: Usage = metadata.usage
val model: String = metadata.model

println("Tokens used: ${usage.totalTokens}")
println("Model: $model")
```

### 4. **복잡한 시나리오에 적합**
- 대화 히스토리 직접 관리
- 커스텀 메시지 타입 구현
- 세밀한 에러 처리
- 성능 최적화가 필요한 경우

---

## ✅ ChatClient의 장점

### 1. **간결한 코드**
```kotlin
// ChatModel: 6줄
val prompt = Prompt(
    UserMessage("Tell me a joke")
)
val response = chatModel.call(prompt)
val content = response.result.output.content

// ChatClient: 1줄
val content = chatClient.prompt("Tell me a joke").call().content()
```

### 2. **Fluent API**
```kotlin
// 읽기 쉬운 메서드 체이닝
val response = chatClient.prompt()
    .system("You are a helpful assistant")
    .user("Explain {topic}")
    .param("topic", "Spring AI")
    .options(options)
    .call()
    .content()
```

### 3. **자동 Entity 매핑**
```kotlin
data class MovieRecommendation(
    val title: String,
    val genre: String,
    val year: Int,
    val rating: Double
)

// ChatClient: 자동 변환
val movie = chatClient.prompt()
    .user("Recommend a sci-fi movie")
    .call()
    .entity(MovieRecommendation::class.java)

// ChatModel: 수동 파싱 필요
val response = chatModel.call(prompt)
val json = response.result.output.content
val movie = objectMapper.readValue<MovieRecommendation>(json)
```

### 4. **내장 템플릿 지원**
```kotlin
// 변수 치환이 내장되어 있음
chatClient.prompt()
    .user { u -> u
        .text("Translate '{text}' to {language}")
        .param("text", "Hello")
        .param("language", "Korean")
    }
    .call()
    .content()
```

### 5. **Advisor 패턴**
```kotlin
// RAG, Memory 등을 쉽게 추가
val chatClient = ChatClient.builder(chatModel)
    .defaultAdvisors(
        QuestionAnswerAdvisor(vectorStore),
        MessageChatMemoryAdvisor(chatMemory)
    )
    .build()
```

---

## ❌ ChatModel의 단점

### 1. **보일러플레이트 코드**
```kotlin
// 매번 Prompt 객체 생성 필요
val prompt = Prompt(
    listOf(UserMessage(question))
)
val response = chatModel.call(prompt)
val content = response.result.output.content
```

### 2. **응답 추출의 복잡성**
```kotlin
// 여러 단계의 추출 과정
val response: ChatResponse = chatModel.call(prompt)
val result: Generation = response.result
val output: AssistantMessage = result.output
val content: String = output.content
```

### 3. **템플릿 처리의 번거로움**
```kotlin
// 수동으로 템플릿 처리
val template = "Tell me about {topic}"
val promptTemplate = PromptTemplate(template)
promptTemplate.add("topic", "Spring AI")
val prompt = promptTemplate.create()
val response = chatModel.call(prompt)
```

### 4. **Entity 매핑의 수동 처리**
```kotlin
// 수동 파싱 및 에러 처리
try {
    val json = response.result.output.content
    val entity = objectMapper.readValue<MyClass>(json)
} catch (e: JsonProcessingException) {
    // 에러 처리
}
```

---

## ❌ ChatClient의 단점

### 1. **제한된 저수준 제어**
```kotlin
// 메시지 리스트를 직접 조작하기 어려움
// ChatModel처럼 messages.add()를 할 수 없음
```

### 2. **추상화로 인한 숨겨진 동작**
```kotlin
// 내부적으로 어떤 일이 일어나는지 파악하기 어려울 수 있음
val content = chatClient.prompt("question").call().content()
// 내부에서 Prompt 생성, 호출, 응답 추출이 자동으로 일어남
```

### 3. **복잡한 대화 히스토리 관리**
```kotlin
// 대화 히스토리를 직접 관리하려면 Advisor 사용 필요
// ChatModel처럼 messages 리스트를 직접 다루는 것보다 복잡할 수 있음
```

---

## 🎯 사용 시나리오별 권장사항

### ChatModel을 사용해야 하는 경우

#### 1. **복잡한 대화 흐름 관리**
```kotlin
// 대화 히스토리를 직접 관리하고 조작해야 할 때
class ConversationManager(private val chatModel: ChatModel) {
    private val messages = mutableListOf<Message>()
    
    fun addSystemMessage(content: String) {
        messages.add(SystemMessage(content))
    }
    
    fun addUserMessage(content: String) {
        messages.add(UserMessage(content))
    }
    
    fun addAssistantMessage(content: String) {
        messages.add(AssistantMessage(content))
    }
    
    fun chat(): String {
        val response = chatModel.call(Prompt(messages))
        val assistantMessage = response.result.output.content
        messages.add(AssistantMessage(assistantMessage))
        return assistantMessage
    }
    
    fun clearHistory() {
        messages.clear()
    }
}
```

#### 2. **세밀한 성능 최적화**
```kotlin
// 토큰 사용량을 정밀하게 모니터링하고 제어
fun optimizedCall(question: String): Pair<String, Int> {
    val options = ChatOptions.builder()
        .withMaxTokens(100)  // 토큰 제한
        .build()
    
    val response = chatModel.call(Prompt(UserMessage(question), options))
    val usage = response.metadata.usage
    
    logger.info("Tokens: ${usage.totalTokens}")
    
    return response.result.output.content to usage.totalTokens
}
```

#### 3. **커스텀 메시지 타입**
```kotlin
// 특수한 메시지 타입이 필요한 경우
class CustomMessage(
    content: String,
    val metadata: Map<String, Any>
) : UserMessage(content)

val messages = listOf(
    CustomMessage("Question", mapOf("priority" to "high"))
)
val response = chatModel.call(Prompt(messages))
```

### ChatClient를 사용해야 하는 경우

#### 1. **일반적인 질의응답**
```kotlin
// 간단한 질문과 답변
@RestController
class QnAController(private val chatClient: ChatClient) {
    
    @PostMapping("/ask")
    fun ask(@RequestBody question: String): String {
        return chatClient.prompt(question)
            .call()
            .content()
    }
}
```

#### 2. **Entity 매핑이 필요한 경우**
```kotlin
// 구조화된 데이터 추출
data class Product(
    val name: String,
    val price: Double,
    val category: String,
    val features: List<String>
)

fun extractProductInfo(description: String): Product {
    return chatClient.prompt()
        .user("Extract product information from: $description")
        .call()
        .entity(Product::class.java)
}
```

#### 3. **템플릿 기반 프롬프트**
```kotlin
// 재사용 가능한 프롬프트 템플릿
fun translate(text: String, targetLanguage: String): String {
    return chatClient.prompt()
        .user { u -> u
            .text("Translate '{text}' to {language}")
            .param("text", text)
            .param("language", targetLanguage)
        }
        .call()
        .content()
}
```

#### 4. **RAG 또는 Memory가 필요한 경우**
```kotlin
// Advisor를 활용한 고급 기능
@Configuration
class ChatClientConfig {
    @Bean
    fun chatClient(
        builder: ChatClient.Builder,
        vectorStore: VectorStore,
        chatMemory: ChatMemory
    ): ChatClient {
        return builder
            .defaultAdvisors(
                QuestionAnswerAdvisor(vectorStore),
                MessageChatMemoryAdvisor(chatMemory)
            )
            .build()
    }
}
```

---

## 🔄 마이그레이션 가이드

### ChatModel → ChatClient 전환

#### Before (ChatModel)
```kotlin
@Service
class ChatService(private val chatModel: ChatModel) {
    
    fun simpleChat(question: String): String {
        val prompt = Prompt(UserMessage(question))
        val response = chatModel.call(prompt)
        return response.result.output.content
    }
    
    fun chatWithSystem(systemMessage: String, userMessage: String): String {
        val messages = listOf(
            SystemMessage(systemMessage),
            UserMessage(userMessage)
        )
        val prompt = Prompt(messages)
        val response = chatModel.call(prompt)
        return response.result.output.content
    }
    
    fun chatWithOptions(question: String, temperature: Double): String {
        val options = ChatOptions.builder()
            .withTemperature(temperature)
            .build()
        val prompt = Prompt(UserMessage(question), options)
        val response = chatModel.call(prompt)
        return response.result.output.content
    }
}
```

#### After (ChatClient)
```kotlin
@Service
class ChatService(private val chatClient: ChatClient) {
    
    fun simpleChat(question: String): String {
        return chatClient.prompt(question)
            .call()
            .content()
    }
    
    fun chatWithSystem(systemMessage: String, userMessage: String): String {
        return chatClient.prompt()
            .system(systemMessage)
            .user(userMessage)
            .call()
            .content()
    }
    
    fun chatWithOptions(question: String, temperature: Double): String {
        val options = ChatOptions.builder()
            .withTemperature(temperature)
            .build()
        
        return chatClient.prompt()
            .user(question)
            .options(options)
            .call()
            .content()
    }
}
```

### 전환 시 주의사항

1. **응답 타입 변경**
   - ChatModel: `ChatResponse` → ChatClient: `String` 또는 `Entity`
   
2. **메타데이터 접근**
   - ChatModel: `response.metadata` 직접 접근
   - ChatClient: `.call().chatResponse()` 사용

3. **스트리밍**
   - ChatModel: `stream(Prompt)` → ChatClient: `.stream().content()`

---

## 📝 실전 예제 비교

### 예제 1: 기본 질의응답

<details>
<summary><b>ChatModel 방식</b></summary>

```kotlin
@RestController
@RequestMapping("/api/chatmodel")
class ChatModelController(
    private val chatModel: ChatModel
) {
    @PostMapping("/ask")
    fun ask(@RequestBody request: QuestionRequest): String {
        val prompt = Prompt(UserMessage(request.question))
        val response = chatModel.call(prompt)
        return response.result.output.content
    }
}
```
</details>

<details>
<summary><b>ChatClient 방식</b></summary>

```kotlin
@RestController
@RequestMapping("/api/chatclient")
class ChatClientController(
    private val chatClient: ChatClient
) {
    @PostMapping("/ask")
    fun ask(@RequestBody request: QuestionRequest): String {
        return chatClient.prompt(request.question)
            .call()
            .content()
    }
}
```
</details>

**차이점:**
- ChatClient가 **3줄 더 짧음**
- ChatClient가 **더 읽기 쉬움**
- 기능은 **완전히 동일**

---

### 예제 2: System Message 포함

<details>
<summary><b>ChatModel 방식</b></summary>

```kotlin
@PostMapping("/expert")
fun askExpert(@RequestBody request: QuestionRequest): String {
    val messages = listOf(
        SystemMessage("You are an expert in ${request.domain}"),
        UserMessage(request.question)
    )
    val prompt = Prompt(messages)
    val response = chatModel.call(prompt)
    return response.result.output.content
}
```
</details>

<details>
<summary><b>ChatClient 방식</b></summary>

```kotlin
@PostMapping("/expert")
fun askExpert(@RequestBody request: QuestionRequest): String {
    return chatClient.prompt()
        .system("You are an expert in ${request.domain}")
        .user(request.question)
        .call()
        .content()
}
```
</details>

**차이점:**
- ChatClient는 **메서드 체이닝**으로 더 직관적
- ChatModel은 **리스트 생성** 필요

---

### 예제 3: Entity 매핑

<details>
<summary><b>ChatModel 방식</b></summary>

```kotlin
data class BookRecommendation(
    val title: String,
    val author: String,
    val genre: String,
    val summary: String
)

@PostMapping("/recommend-book")
fun recommendBook(@RequestBody genre: String): BookRecommendation {
    val systemMessage = SystemMessage(
        "You are a book expert. Return recommendations in JSON format."
    )
    val userMessage = UserMessage("Recommend a $genre book")
    
    val prompt = Prompt(listOf(systemMessage, userMessage))
    val response = chatModel.call(prompt)
    val json = response.result.output.content
    
    return objectMapper.readValue(json, BookRecommendation::class.java)
}
```
</details>

<details>
<summary><b>ChatClient 방식</b></summary>

```kotlin
data class BookRecommendation(
    val title: String,
    val author: String,
    val genre: String,
    val summary: String
)

@PostMapping("/recommend-book")
fun recommendBook(@RequestBody genre: String): BookRecommendation {
    return chatClient.prompt()
        .system("You are a book expert")
        .user("Recommend a $genre book")
        .call()
        .entity(BookRecommendation::class.java)
}
```
</details>

**차이점:**
- ChatClient는 **자동 JSON 파싱**
- ChatModel은 **수동 ObjectMapper 사용** 필요
- ChatClient가 **에러 처리도 자동**

---

### 예제 4: 스트리밍 응답

<details>
<summary><b>ChatModel 방식</b></summary>

```kotlin
@GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
fun stream(@RequestParam question: String): Flux<String> {
    val prompt = Prompt(UserMessage(question))
    
    return chatModel.stream(prompt)
        .map { response ->
            response.result?.output?.content ?: ""
        }
}
```
</details>

<details>
<summary><b>ChatClient 방식</b></summary>

```kotlin
@GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
fun stream(@RequestParam question: String): Flux<String> {
    return chatClient.prompt(question)
        .stream()
        .content()
}
```
</details>

**차이점:**
- ChatClient는 **직접 Flux\<String\> 반환**
- ChatModel은 **map 변환 필요**

---

## 🏗️ 아키텍처 패턴

### 패턴 1: Service Layer 분리

```kotlin
// ChatModel 기반 서비스
@Service
class ChatModelService(private val chatModel: ChatModel) {
    
    fun chat(question: String): String {
        val prompt = Prompt(UserMessage(question))
        val response = chatModel.call(prompt)
        return response.result.output.content
    }
    
    fun chatWithContext(system: String, user: String): String {
        val messages = listOf(
            SystemMessage(system),
            UserMessage(user)
        )
        val response = chatModel.call(Prompt(messages))
        return response.result.output.content
    }
}

// ChatClient 기반 서비스
@Service
class ChatClientService(private val chatClient: ChatClient) {
    
    fun chat(question: String): String {
        return chatClient.prompt(question).call().content()
    }
    
    fun chatWithContext(system: String, user: String): String {
        return chatClient.prompt()
            .system(system)
            .user(user)
            .call()
            .content()
    }
}
```

### 패턴 2: 추상화 레이어

```kotlin
// 공통 인터페이스
interface AIChatService {
    fun chat(question: String): String
    fun chatWithSystem(system: String, user: String): String
}

// ChatModel 구현
@Service
@Primary
class ChatModelServiceImpl(
    private val chatModel: ChatModel
) : AIChatService {
    
    override fun chat(question: String): String {
        val prompt = Prompt(UserMessage(question))
        return chatModel.call(prompt).result.output.content
    }
    
    override fun chatWithSystem(system: String, user: String): String {
        val messages = listOf(SystemMessage(system), UserMessage(user))
        return chatModel.call(Prompt(messages)).result.output.content
    }
}

// ChatClient 구현
@Service
class ChatClientServiceImpl(
    private val chatClient: ChatClient
) : AIChatService {
    
    override fun chat(question: String): String {
        return chatClient.prompt(question).call().content()
    }
    
    override fun chatWithSystem(system: String, user: String): String {
        return chatClient.prompt()
            .system(system)
            .user(user)
            .call()
            .content()
    }
}
```

---

## 🎓 학습 로드맵

### 1단계: ChatModel 이해
- Prompt와 Message 개념
- ChatResponse 구조
- 기본 호출 패턴

### 2단계: ChatClient 기초
- Fluent API 사용법
- 메서드 체이닝
- 간단한 질의응답

### 3단계: 고급 기능
- Entity 매핑
- 스트리밍
- 템플릿 사용

### 4단계: 실전 적용
- 프로젝트에 적용
- 성능 최적화
- 에러 처리

---

## 📚 참고 자료

- [Spring AI ChatModel 공식 문서](https://docs.spring.io/spring-ai/reference/api/chatmodel.html)
- [Spring AI ChatClient 공식 문서](https://docs.spring.io/spring-ai/reference/api/chatclient.html)
- [Spring AI Reference](https://docs.spring.io/spring-ai/reference/)

---

## 🚀 다음 단계

샘플 코드를 통해 실제로 두 방식을 비교해보세요:

- [Sample 프로젝트](./sample/) - 동일한 기능을 두 가지 방식으로 구현한 예제

---

## 💡 결론

### 언제 ChatModel을 사용할까?
- ✅ 복잡한 대화 흐름 관리가 필요할 때
- ✅ 세밀한 제어와 최적화가 필요할 때
- ✅ 메타데이터에 자주 접근해야 할 때
- ✅ 커스텀 메시지 타입이 필요할 때

### 언제 ChatClient를 사용할까?
- ✅ 일반적인 질의응답 시스템
- ✅ 빠른 프로토타이핑
- ✅ Entity 매핑이 필요할 때
- ✅ RAG, Memory 등 Advisor 사용 시
- ✅ 코드 가독성이 중요할 때

### 권장사항
**대부분의 경우 ChatClient를 사용하고, 특별한 요구사항이 있을 때만 ChatModel을 사용하세요.**

ChatClient는 ChatModel을 내부적으로 사용하므로, 필요시 언제든 ChatModel로 전환할 수 있습니다.
