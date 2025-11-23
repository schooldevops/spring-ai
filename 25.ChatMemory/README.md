# 25. Chat Memory

## 📖 학습 목표

- **Chat Memory**의 개념과 필요성을 이해합니다
- **다양한 저장소**를 활용한 대화 기록 관리를 학습합니다
- **ChatClient**와 **Advisor**를 통한 메모리 통합을 구현합니다
- **대화 컨텍스트 유지**로 자연스러운 대화를 만듭니다

---

## 🔑 핵심 키워드

1. **ChatMemory** - 대화 기록 저장 및 관리
2. **MessageWindowChatMemory** - 최근 N개 메시지 유지
3. **ChatMemoryRepository** - 다양한 저장소 지원
4. **PromptChatMemoryAdvisor** - ChatClient 메모리 통합
5. **Conversation ID** - 대화 세션 관리

---

## 1. Chat Memory란?

**Chat Memory**는 AI와의 대화 내용을 기억하여 문맥을 유지하는 기능입니다.

### 왜 필요한가?
```kotlin
// 메모리 없이
User: "내 이름은 철수야"
AI: "안녕하세요!"
User: "내 이름이 뭐지?"
AI: "죄송하지만 모르겠습니다" ❌

// 메모리 있음
User: "내 이름은 철수야"
AI: "안녕하세요 철수님!"
User: "내 이름이 뭐지?"
AI: "철수님이십니다" ✅
```

---

## 2. 샘플 구성

### Sample 01: In-Memory Chat Memory
- 기본 메모리 사용법
- 대화 기록 저장 및 조회
- **포트:** 9500

### Sample 02: Message Window Memory
- 최근 N개 메시지만 유지
- 토큰 제한 관리
- **포트:** 9501

### Sample 03: ChatClient with Memory
- ChatClient + PromptChatMemoryAdvisor
- 자동 메모리 관리
- **포트:** 9502

### Sample 04: Conversation Management
- 여러 대화 세션 관리
- Conversation ID 활용
- **포트:** 9503

---

## 3. ChatMemory 구조

```kotlin
interface ChatMemory {
    fun add(conversationId: String, messages: List<Message>)
    fun get(conversationId: String, lastN: Int): List<Message>
    fun clear(conversationId: String)
}
```

---

## 4. 저장소 옵션

| 저장소 | 특징 | 사용 시나리오 |
|--------|------|---------------|
| **In-Memory** | 빠름, 휘발성 | 개발/테스트 |
| **JDBC** | 관계형 DB | 일반적인 운영 |
| **Cassandra** | 분산, 확장성 | 대규모 서비스 |
| **Redis** | 빠른 캐시 | 실시간 채팅 |
| **MongoDB** | 문서 DB | 유연한 스키마 |

---

## 5. PromptChatMemoryAdvisor

ChatClient와 자동 통합:

```kotlin
val chatClient = ChatClient.builder(chatModel)
    .defaultAdvisors(
        PromptChatMemoryAdvisor(chatMemory)
    )
    .build()

// 자동으로 메모리 관리
chatClient.prompt()
    .user("내 이름은 철수야")
    .call()
    .content()
```

---

## 6. 공통 설정

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
```

---

**시작하기**: [Sample 01: In-Memory Chat Memory](./sample01-inmemory-chatmemory/)
