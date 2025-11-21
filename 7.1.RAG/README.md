# 7.1: RAG 패턴의 이해

## 📖 학습 목표

이 강의를 마친 후 다음을 달성할 수 있습니다:
- **RAG (Retrieval-Augmented Generation)**의 개념과 필요성을 이해할 수 있습니다
- **LLM의 환각(Hallucination) 문제**를 이해하고 RAG가 이를 해결하는 방법을 알 수 있습니다
- **Grounding**의 개념과 중요성을 이해할 수 있습니다
- **Context**의 역할과 RAG에서의 활용 방법을 이해할 수 있습니다
- **RAG의 동작 원리**를 단계별로 설명할 수 있습니다
- **RAG의 장단점**을 이해하고 언제 사용해야 하는지 판단할 수 있습니다

---

## 🔑 핵심 키워드

이 장에서 다루는 핵심 키워드들:

1. **RAG (Retrieval-Augmented Generation)** - 검색 증강 생성, LLM에 외부 지식을 제공하는 패턴
2. **Grounding** - LLM의 응답을 실제 데이터에 기반하게 만드는 것
3. **Context** - LLM에게 제공하는 추가 정보/문맥
4. **Hallucination (환각)** - LLM이 잘못된 정보를 생성하는 현상
5. **Retrieval** - 관련 문서를 검색하는 과정
6. **Augmentation** - 검색된 문서를 프롬프트에 추가하는 과정

---

## 1. RAG란 무엇인가?

### 1.1 RAG의 정의

**RAG (Retrieval-Augmented Generation)**는 LLM의 응답을 외부 지식 소스에서 검색한 정보로 보강하는 패턴입니다.

#### RAG의 핵심 아이디어

```
기존 LLM 방식:
사용자 질문 → LLM → 응답 (학습 데이터에만 의존)

RAG 방식:
사용자 질문 → 벡터 검색 → 관련 문서 검색 → LLM (문서 + 질문) → 응답 (실제 데이터 기반)
```

### 1.2 RAG의 등장 배경

#### 문제점: LLM의 한계

1. **학습 데이터의 한계**
   - 학습 시점 이후의 정보를 모름
   - 특정 도메인 지식 부족
   - 내부 문서/데이터 접근 불가

2. **환각(Hallucination) 문제**
   - 존재하지 않는 정보를 생성
   - 잘못된 사실을 확신 있게 말함
   - 출처 없는 정보 제공

3. **컨텍스트 부족**
   - 특정 회사/제품 정보 부족
   - 최신 정책/규정 미반영
   - 개인화된 정보 제공 불가

#### 해결책: RAG

RAG는 LLM에게 **실제 데이터를 제공**하여 이러한 문제를 해결합니다.

---

## 2. 환각(Hallucination) 문제 이해하기

### 2.1 환각이란?

**환각(Hallucination)**은 LLM이 학습 데이터에 없는 정보를 생성하거나, 잘못된 정보를 확신 있게 제공하는 현상입니다.

#### 환각의 예시

```
사용자: "우리 회사의 환불 정책은 무엇인가요?"

LLM (환각 발생):
"귀하의 회사는 30일 이내 무조건 환불을 보장합니다. 
환불 신청은 고객센터로 연락하시면 됩니다."

→ 실제로는 7일 이내, 조건부 환불인데 잘못된 정보 제공
```

### 2.2 환각이 발생하는 이유

1. **학습 데이터 부족**
   - 특정 회사/제품 정보가 학습 데이터에 없음
   - 최신 정보가 학습 시점 이후의 것

2. **확률적 생성**
   - LLM은 확률에 기반하여 다음 토큰을 생성
   - 가장 가능성 높은 답을 생성하지만 정확하지 않을 수 있음

3. **컨텍스트 부족**
   - 질문에 대한 충분한 정보가 제공되지 않음
   - LLM이 추측으로 답변

### 2.3 RAG가 환각을 줄이는 방법

RAG는 **실제 문서를 LLM에게 제공**하여 환각을 줄입니다:

```
기존 방식 (환각 발생 가능):
질문 → LLM → 추측 기반 응답

RAG 방식 (환각 감소):
질문 → 벡터 검색 → 실제 문서 검색 → LLM (문서 + 질문) → 문서 기반 응답
```

---

## 3. Grounding의 개념

### 3.1 Grounding이란?

**Grounding**은 LLM의 응답을 실제 데이터나 문서에 기반하게 만드는 것을 의미합니다.

#### Grounding의 목적

- **정확성 향상**: 실제 데이터 기반 응답
- **신뢰성 향상**: 출처가 있는 정보 제공
- **투명성**: 어떤 문서를 참조했는지 명시

### 3.2 Grounding의 방법

#### 1. 문서 기반 Grounding

```kotlin
// 1. 관련 문서 검색
val documents = vectorStore.similaritySearch("환불 정책") ?: emptyList()

// 2. 문서 내용을 프롬프트에 포함
val context = documents.joinToString("\n\n") { it.text }

// 3. LLM에게 문서와 질문을 함께 제공
val prompt = """
다음 문서를 참고하여 질문에 답변해주세요:

문서:
$context

질문: {question}
"""
```

#### 2. 메타데이터 기반 Grounding

```kotlin
// 문서의 출처 정보 포함
val documents = vectorStore.similaritySearch(query) ?: emptyList()
val sources = documents.map { it.metadata["source"] }

// 응답에 출처 정보 포함
return RAGResponse(
    answer = llmResponse,
    sources = sources
)
```

### 3.3 Grounding의 효과

#### ✅ Grounding 적용 시

```
질문: "환불 정책은?"
응답: "환불 정책에 따르면 7일 이내, 미사용 제품에 한해 환불이 가능합니다."
출처: [회사 정책 문서 v2.1, 2024-01-15]
```

#### ❌ Grounding 미적용 시

```
질문: "환불 정책은?"
응답: "30일 이내 무조건 환불 가능합니다." (잘못된 정보)
출처: 없음
```

---

## 4. Context의 역할

### 4.1 Context란?

**Context**는 LLM에게 제공하는 추가 정보로, 질문에 대한 답변을 생성하는 데 필요한 배경 지식입니다.

#### Context의 종류

1. **검색된 문서 (Retrieved Documents)**
   - VectorStore에서 검색한 관련 문서
   - 질문과 유사한 내용을 가진 문서들

2. **대화 이력 (Conversation History)**
   - 이전 대화 내용
   - 사용자의 의도 파악에 도움

3. **시스템 메시지 (System Message)**
   - LLM의 역할 정의
   - 응답 형식 지시

### 4.2 Context를 RAG에서 활용하는 방법

#### 기본 RAG 패턴

```kotlin
// 1. 질문에 대한 관련 문서 검색
val documents = vectorStore.similaritySearch(userQuestion) ?: emptyList()
val topDocuments = documents.take(5)

// 2. 문서를 Context로 변환
val context = topDocuments.joinToString("\n\n---\n\n") { doc ->
    "문서: ${doc.text}\n출처: ${doc.metadata["source"]}"
}

// 3. Context와 질문을 함께 LLM에 전달
val prompt = Prompt(
    listOf(
        SystemMessage("""
            당신은 고객 지원 담당자입니다.
            제공된 문서를 참고하여 정확하게 답변해주세요.
            문서에 없는 내용은 추측하지 마세요.
        """),
        UserMessage("""
            참고 문서:
            $context
            
            질문: $userQuestion
        """)
    )
)

// 4. LLM 호출
val response = chatModel.call(prompt)
```

### 4.3 Context의 품질이 응답에 미치는 영향

#### ✅ 좋은 Context

- **관련성**: 질문과 직접 관련된 문서
- **정확성**: 최신이고 정확한 정보
- **충분성**: 답변에 필요한 정보가 모두 포함

#### ❌ 나쁜 Context

- **무관련성**: 질문과 관련 없는 문서
- **부정확성**: 오래되었거나 잘못된 정보
- **부족함**: 답변에 필요한 정보가 부족

---

## 5. RAG의 동작 원리

### 5.1 RAG 파이프라인

RAG는 다음 단계로 동작합니다:

```
1. 질문 입력
   ↓
2. 질문 임베딩 생성
   (EmbeddingModel)
   ↓
3. 벡터 유사도 검색
   (VectorStore.similaritySearch())
   ↓
4. 관련 문서 검색 (Top-K)
   ↓
5. 문서를 Context로 변환
   ↓
6. Context + 질문을 LLM에 전달
   (ChatModel)
   ↓
7. 응답 생성
```

### 5.2 단계별 상세 설명

#### 1단계: 질문 입력

```kotlin
val userQuestion = "환불 정책은 무엇인가요?"
```

#### 2단계: 질문 임베딩 생성

```kotlin
// 질문을 벡터로 변환
val questionEmbedding = embeddingModel.embedForResponse(
    listOf(userQuestion)
).embeddings.firstOrNull()?.embedding
```

#### 3단계: 벡터 유사도 검색

```kotlin
// VectorStore에서 유사한 문서 검색
val documents = vectorStore.similaritySearch(userQuestion) ?: emptyList()
```

#### 4단계: 관련 문서 검색 (Top-K)

```kotlin
// 상위 K개 문서 선택
val topK = 5
val relevantDocuments = documents.take(topK)
```

#### 5단계: 문서를 Context로 변환

```kotlin
// 문서들을 하나의 Context 문자열로 변환
val context = relevantDocuments.joinToString("\n\n---\n\n") { doc ->
    """
    문서 제목: ${doc.metadata["title"]}
    내용: ${doc.text}
    출처: ${doc.metadata["source"]}
    """.trimIndent()
}
```

#### 6단계: Context + 질문을 LLM에 전달

```kotlin
val prompt = Prompt(
    listOf(
        SystemMessage("""
            당신은 고객 지원 담당자입니다.
            제공된 문서를 참고하여 정확하게 답변해주세요.
        """),
        UserMessage("""
            참고 문서:
            $context
            
            질문: $userQuestion
        """)
    )
)
```

#### 7단계: 응답 생성

```kotlin
val response = chatModel.call(prompt)
val answer = response.results.firstOrNull()?.output?.text ?: ""
```

### 5.3 RAG의 전체 흐름도

```
┌─────────────┐
│ 사용자 질문 │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│ EmbeddingModel  │  ← 질문을 벡터로 변환
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│  VectorStore    │  ← 유사한 문서 검색
│ similaritySearch│
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│ 관련 문서 (Top-K)│  ← 상위 K개 문서 선택
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│  Context 생성    │  ← 문서를 Context로 변환
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│   ChatModel     │  ← Context + 질문 전달
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│   최종 응답     │
└─────────────────┘
```

---

## 6. RAG의 장단점

### 6.1 RAG의 장점

#### ✅ 정확성 향상

- **실제 데이터 기반**: 학습 데이터가 아닌 실제 문서 기반 응답
- **최신 정보**: 최신 문서를 추가하면 최신 정보 제공 가능
- **도메인 특화**: 특정 도메인 지식 제공 가능

#### ✅ 환각 감소

- **출처 명시**: 어떤 문서를 참조했는지 알 수 있음
- **검증 가능**: 응답의 정확성을 문서로 검증 가능
- **투명성**: LLM이 어떻게 답변했는지 추적 가능

#### ✅ 확장성

- **새로운 정보 추가**: 문서만 추가하면 새로운 정보 제공 가능
- **학습 불필요**: LLM 재학습 없이 정보 업데이트
- **유연성**: 다양한 문서 형식 지원

### 6.2 RAG의 단점

#### ❌ 검색 품질 의존

- **검색 실패**: 관련 문서를 찾지 못하면 답변 품질 저하
- **노이즈**: 무관련 문서가 포함되면 혼란
- **임베딩 품질**: 임베딩 모델의 품질이 검색 품질에 영향

#### ❌ 지연 시간

- **검색 시간**: 벡터 검색에 시간 소요
- **임베딩 생성**: 질문 임베딩 생성 시간
- **LLM 호출**: Context가 길어지면 LLM 호출 시간 증가

#### ❌ Context 길이 제한

- **토큰 제한**: LLM의 토큰 제한으로 인한 문서 수 제한
- **비용**: 긴 Context는 더 많은 토큰 사용 = 비용 증가
- **관련성 판단**: 어떤 문서를 포함할지 판단 필요

---

## 7. RAG 사용 시나리오

### 7.1 RAG를 사용해야 하는 경우

#### ✅ 적합한 시나리오

1. **고객 지원 챗봇**
   - 회사 정책 문서 기반 답변
   - 제품 매뉴얼 참조
   - FAQ 시스템

2. **내부 지식베이스**
   - 회사 내부 문서 검색
   - 프로젝트 문서 참조
   - 기술 문서 검색

3. **최신 정보 제공**
   - 뉴스 기사 요약
   - 최신 정책 안내
   - 실시간 데이터 기반 답변

4. **도메인 특화 시스템**
   - 법률 문서 검색
   - 의학 정보 제공
   - 금융 데이터 분석

### 7.2 RAG를 사용하지 않아도 되는 경우

#### ❌ 불필요한 시나리오

1. **일반적인 대화**
   - 일반적인 질문/답변
   - 학습 데이터에 충분한 정보

2. **창의적 작업**
   - 창작, 번역, 요약
   - 특정 문서 참조 불필요

3. **간단한 작업**
   - 간단한 계산
   - 복잡한 문서 검색 불필요

---

## 8. RAG vs 기존 LLM 방식

### 8.1 비교표

| 특징 | 기존 LLM | RAG |
|------|----------|-----|
| **정보 소스** | 학습 데이터 | 학습 데이터 + 검색된 문서 |
| **최신 정보** | ❌ 학습 시점 이후 정보 없음 | ✅ 최신 문서 추가 가능 |
| **정확성** | ⚠️ 환각 가능 | ✅ 문서 기반 (높음) |
| **출처** | ❌ 없음 | ✅ 문서 출처 제공 |
| **도메인 특화** | ⚠️ 제한적 | ✅ 특정 도메인 문서 활용 |
| **응답 속도** | ✅ 빠름 | ⚠️ 검색 시간 추가 |
| **비용** | ✅ 낮음 | ⚠️ 검색 + LLM 호출 |

### 8.2 언제 어떤 방식을 사용할까?

#### 기존 LLM 방식 사용

- 일반적인 대화
- 창의적 작업
- 학습 데이터에 충분한 정보가 있는 경우

#### RAG 방식 사용

- 최신 정보가 필요한 경우
- 특정 도메인 지식이 필요한 경우
- 정확성이 중요한 경우
- 출처가 필요한 경우

---

## 9. RAG의 실제 예제

### 9.1 간단한 RAG 예제

```kotlin
@Service
class SimpleRAGService(
    private val vectorStore: VectorStore,
    private val chatModel: ChatModel
) {
    fun ask(question: String): String {
        // 1. 관련 문서 검색
        val documents = vectorStore.similaritySearch(question) ?: emptyList()
        val topDocuments = documents.take(3)
        
        // 2. Context 생성
        val context = topDocuments.joinToString("\n\n") { it.text }
        
        // 3. 프롬프트 생성
        val prompt = Prompt(
            listOf(
                SystemMessage("다음 문서를 참고하여 질문에 답변해주세요."),
                UserMessage("""
                    문서:
                    $context
                    
                    질문: $question
                """.trimIndent())
            )
        )
        
        // 4. LLM 호출
        val response = chatModel.call(prompt)
        return response.results.firstOrNull()?.output?.text ?: "답변을 생성할 수 없습니다."
    }
}
```

### 9.2 RAG 응답 예시

#### 질문
```
"우리 회사의 환불 정책은 무엇인가요?"
```

#### 검색된 문서
```
문서 1: "환불 정책: 구매 후 7일 이내, 미사용 제품에 한해 환불 가능..."
문서 2: "환불 절차: 고객센터로 연락 → 환불 신청서 작성 → 검토 후 환불..."
```

#### RAG 응답
```
우리 회사의 환불 정책은 다음과 같습니다:

1. 환불 기간: 구매 후 7일 이내
2. 조건: 미사용 제품에 한해 환불 가능
3. 절차: 고객센터로 연락하여 환불 신청서를 작성하시면 검토 후 환불 처리됩니다.

[출처: 회사 정책 문서 v2.1]
```

---

## 10. 요약

### 10.1 핵심 내용 정리

1. **RAG (Retrieval-Augmented Generation)**: 검색 증강 생성, LLM에 외부 지식을 제공하는 패턴
2. **환각(Hallucination)**: LLM이 잘못된 정보를 생성하는 현상
3. **Grounding**: LLM의 응답을 실제 데이터에 기반하게 만드는 것
4. **Context**: LLM에게 제공하는 추가 정보/문맥
5. **RAG 동작 원리**: 질문 → 임베딩 → 검색 → Context 생성 → LLM 호출 → 응답

### 10.2 RAG의 가치

- ✅ **정확성**: 실제 문서 기반 응답
- ✅ **최신성**: 최신 정보 제공 가능
- ✅ **신뢰성**: 출처 명시로 검증 가능
- ✅ **확장성**: 문서 추가만으로 정보 업데이트

### 10.3 다음 학습 내용

이제 RAG 패턴을 이해했으니, 다음 장에서는:
- **RAG 파이프라인 구현**: 실제 코드로 RAG 구현
- **PromptTemplate 활용**: 동적 프롬프트 생성
- **문서 로딩**: 외부 문서를 VectorStore에 추가
- **문서 분할**: 긴 문서를 의미 있는 단위로 분할

---

## 📚 참고 자료

- [RAG 논문 (Retrieval-Augmented Generation)](https://arxiv.org/abs/2005.11401)
- [Spring AI RAG 패턴 문서](https://docs.spring.io/spring-ai/reference/patterns/rag.html)
- [LLM Hallucination 문제](https://en.wikipedia.org/wiki/Hallucination_(artificial_intelligence))

---

## ❓ 학습 확인 문제

다음 질문에 답할 수 있는지 확인해보세요:

1. RAG란 무엇이고, 왜 필요한가요?
2. 환각(Hallucination)이란 무엇이고, RAG가 이를 어떻게 해결하나요?
3. Grounding이란 무엇이고, 왜 중요한가요?
4. Context의 역할은 무엇인가요?
5. RAG의 동작 원리를 단계별로 설명할 수 있나요?
6. RAG의 장단점은 무엇인가요?
7. 언제 RAG를 사용해야 하고, 언제 사용하지 않아도 되나요?

---

**다음 장**: [7.2: 간단한 RAG 파이프라인 구현](../README.md#72-간단한-rag-파이프라인-구현)

