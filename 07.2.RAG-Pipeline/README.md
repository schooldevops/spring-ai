# 7.2: 간단한 RAG 파이프라인 구현

## 📖 학습 목표

이 강의를 마친 후 다음을 달성할 수 있습니다:
- **VectorStore.similaritySearch()**를 사용하여 관련 문서를 검색할 수 있습니다
- **검색된 문서를 Context로 변환**하는 방법을 이해하고 구현할 수 있습니다
- **PromptTemplate**을 사용하여 동적으로 프롬프트를 생성할 수 있습니다
- **Context를 PromptTemplate에 주입**하여 RAG 파이프라인을 구현할 수 있습니다
- **ChatModel**에 Context와 질문을 함께 전달하는 방법을 구현할 수 있습니다
- **완전한 RAG 파이프라인**을 단계별로 구현할 수 있습니다

---

## 🔑 핵심 키워드

이 장에서 다루는 핵심 키워드들:

1. **VectorStore.similaritySearch()** - 벡터 유사도 기반 문서 검색 메서드
2. **Context 주입** - 검색된 문서를 프롬프트에 포함시키는 과정
3. **PromptTemplate** - 동적 값을 주입할 수 있는 프롬프트 템플릿
4. **ChatModel** - LLM과 상호작용하는 인터페이스
5. **RAG 파이프라인** - Retrieval → Augmentation → Generation의 전체 흐름

---

## 1. RAG 파이프라인 개요

### 1.1 RAG 파이프라인의 3단계

RAG 파이프라인은 다음 3단계로 구성됩니다:

```
1. Retrieval (검색)
   질문 → VectorStore.similaritySearch() → 관련 문서 검색
   
2. Augmentation (증강)
   검색된 문서 → Context 변환 → PromptTemplate에 주입
   
3. Generation (생성)
   Context + 질문 → ChatModel → 응답 생성
```

### 1.2 각 단계의 역할

#### Retrieval (검색)
- **목적**: 질문과 관련된 문서를 찾기
- **도구**: `VectorStore.similaritySearch()`
- **결과**: 관련 문서 리스트 (Top-K)

#### Augmentation (증강)
- **목적**: 검색된 문서를 LLM이 이해할 수 있는 Context로 변환
- **도구**: 문자열 변환, `PromptTemplate`
- **결과**: Context 문자열

#### Generation (생성)
- **목적**: Context와 질문을 LLM에 전달하여 답변 생성
- **도구**: `ChatModel`, `PromptTemplate`
- **결과**: 최종 답변

---

## 2. Retrieval: 문서 검색

### 2.1 VectorStore.similaritySearch() 사용법

`VectorStore.similaritySearch()`는 질문과 유사한 문서를 검색합니다.

#### 기본 사용법

```kotlin
@Service
class RAGService(
    private val vectorStore: VectorStore
) {
    fun searchDocuments(question: String, topK: Int = 5): List<Document> {
        // 질문과 유사한 문서 검색
        val documents = vectorStore.similaritySearch(question) ?: emptyList()
        
        // 상위 K개 문서 반환
        return documents.take(topK)
    }
}
```

### 2.2 검색 결과 처리

```kotlin
fun searchAndProcess(question: String, topK: Int = 5): List<Document> {
    val documents = vectorStore.similaritySearch(question) ?: emptyList()
    
    // 검색 결과가 없는 경우 처리
    if (documents.isEmpty()) {
        throw NoDocumentsFoundException("관련 문서를 찾을 수 없습니다.")
    }
    
    // 상위 K개 문서 반환
    return documents.take(topK)
}
```

### 2.3 검색 품질 개선

#### topK 값 조정

```kotlin
// 적은 문서 (빠르지만 정보 부족 가능)
val documents = vectorStore.similaritySearch(question)?.take(3) ?: emptyList()

// 많은 문서 (느리지만 정보 풍부)
val documents = vectorStore.similaritySearch(question)?.take(10) ?: emptyList()
```

#### 검색 결과 필터링

```kotlin
fun searchWithFilter(question: String, category: String, topK: Int = 5): List<Document> {
    val documents = vectorStore.similaritySearch(question) ?: emptyList()
    
    // 카테고리 필터링
    val filtered = documents.filter { doc ->
        doc.metadata["category"] == category
    }
    
    return filtered.take(topK)
}
```

---

## 3. Augmentation: Context 생성

### 3.1 문서를 Context로 변환

검색된 문서를 LLM이 이해할 수 있는 Context 문자열로 변환합니다.

#### 기본 변환

```kotlin
fun createContext(documents: List<Document>): String {
    return documents.joinToString("\n\n---\n\n") { doc ->
        doc.text ?: ""
    }
}
```

#### 메타데이터 포함 변환

```kotlin
fun createContextWithMetadata(documents: List<Document>): String {
    return documents.joinToString("\n\n---\n\n") { doc ->
        val title = doc.metadata["title"] as? String ?: "문서"
        val source = doc.metadata["source"] as? String ?: "알 수 없음"
        
        """
        [문서: $title]
        출처: $source
        
        ${doc.text}
        """.trimIndent()
    }
}
```

### 3.2 Context 포맷팅

#### 구조화된 Context

```kotlin
fun createStructuredContext(documents: List<Document>): String {
    return documents.mapIndexed { index, doc ->
        """
        === 문서 ${index + 1} ===
        제목: ${doc.metadata["title"] ?: "제목 없음"}
        내용: ${doc.text}
        출처: ${doc.metadata["source"] ?: "알 수 없음"}
        """.trimIndent()
    }.joinToString("\n\n")
}
```

#### 간결한 Context

```kotlin
fun createConciseContext(documents: List<Document>): String {
    return documents.mapIndexed { index, doc ->
        "[${index + 1}] ${doc.text}"
    }.joinToString("\n\n")
}
```

### 3.3 Context 길이 제한

LLM의 토큰 제한을 고려하여 Context 길이를 제한합니다.

```kotlin
fun createLimitedContext(documents: List<Document>, maxLength: Int = 2000): String {
    var context = ""
    val contextBuilder = StringBuilder()
    
    for (doc in documents) {
        val docText = doc.text ?: ""
        val newContext = if (context.isEmpty()) {
            docText
        } else {
            "$context\n\n---\n\n$docText"
        }
        
        // 길이 제한 확인
        if (newContext.length <= maxLength) {
            context = newContext
            contextBuilder.append(if (contextBuilder.isEmpty()) docText else "\n\n---\n\n$docText")
        } else {
            break
        }
    }
    
    return contextBuilder.toString()
}
```

---

## 4. PromptTemplate을 사용한 Context 주입

### 4.1 PromptTemplate이란?

**PromptTemplate**은 동적 값을 주입할 수 있는 프롬프트 템플릿입니다.

#### PromptTemplate의 장점

- **재사용성**: 템플릿을 한 번 정의하고 여러 번 사용
- **유지보수성**: 프롬프트 구조 변경이 쉬움
- **가독성**: 프롬프트 구조가 명확함

### 4.2 기본 RAG PromptTemplate

```kotlin
@Service
class RAGService(
    private val vectorStore: VectorStore,
    private val chatModel: ChatModel
) {
    // RAG용 PromptTemplate 정의
    private val ragTemplate = PromptTemplate("""
        당신은 도움이 되는 AI 어시스턴트입니다.
        제공된 문서를 참고하여 질문에 정확하게 답변해주세요.
        문서에 없는 내용은 추측하지 말고, "문서에 해당 정보가 없습니다"라고 답변해주세요.
        
        참고 문서:
        {context}
        
        질문: {question}
    """.trimIndent())
    
    fun ask(question: String, topK: Int = 3): String {
        // 1. Retrieval: 문서 검색
        val documents = vectorStore.similaritySearch(question) ?: emptyList()
        val topDocuments = documents.take(topK)
        
        // 2. Augmentation: Context 생성
        val context = topDocuments.joinToString("\n\n---\n\n") { it.text }
        
        // 3. PromptTemplate에 Context와 질문 주입
        val prompt = ragTemplate.create(mapOf(
            "context" to context,
            "question" to question
        ))
        
        // 4. Generation: LLM 호출
        val response = chatModel.call(prompt)
        return response.results.firstOrNull()?.output?.text ?: "답변을 생성할 수 없습니다."
    }
}
```

### 4.3 고급 PromptTemplate

#### 여러 변수를 가진 템플릿

```kotlin
private val advancedRAGTemplate = PromptTemplate("""
    역할: {role}
    
    다음 문서를 참고하여 질문에 답변해주세요:
    
    {context}
    
    질문: {question}
    
    추가 지시사항:
    - 답변은 {tone}한 톤으로 작성해주세요.
    - 답변 길이는 {length} 정도로 작성해주세요.
""".trimIndent())

fun askAdvanced(
    question: String,
    role: String = "고객 지원 담당자",
    tone: String = "친절",
    length: String = "간결하게"
): String {
    val documents = vectorStore.similaritySearch(question) ?: emptyList()
    val context = documents.take(3).joinToString("\n\n") { it.text }
    
    val prompt = advancedRAGTemplate.create(mapOf(
        "role" to role,
        "context" to context,
        "question" to question,
        "tone" to tone,
        "length" to length
    ))
    
    val response = chatModel.call(prompt)
    return response.results.firstOrNull()?.output?.text ?: ""
}
```

### 4.4 조건부 Context 주입

```kotlin
fun askWithConditionalContext(question: String, includeMetadata: Boolean = false): String {
    val documents = vectorStore.similaritySearch(question) ?: emptyList()
    val topDocuments = documents.take(3)
    
    // 조건에 따라 다른 Context 생성
    val context = if (includeMetadata) {
        topDocuments.joinToString("\n\n---\n\n") { doc ->
            """
            [문서: ${doc.metadata["title"]}]
            출처: ${doc.metadata["source"]}
            ${doc.text}
            """.trimIndent()
        }
    } else {
        topDocuments.joinToString("\n\n---\n\n") { it.text }
    }
    
    val prompt = ragTemplate.create(mapOf(
        "context" to context,
        "question" to question
    ))
    
    val response = chatModel.call(prompt)
    return response.results.firstOrNull()?.output?.text ?: ""
}
```

---

## 5. Generation: ChatModel 호출

### 5.1 기본 ChatModel 호출

```kotlin
fun generateAnswer(prompt: Prompt): String {
    val response = chatModel.call(prompt)
    return response.results.firstOrNull()?.output?.text ?: "답변을 생성할 수 없습니다."
}
```

### 5.2 응답 처리

```kotlin
fun generateAnswerWithMetadata(prompt: Prompt): RAGResponse {
    val response = chatModel.call(prompt)
    val answer = response.results.firstOrNull()?.output?.text ?: ""
    
    // 메타데이터 추출
    val usage = response.result?.metadata?.usage
    
    return RAGResponse(
        answer = answer,
        tokenUsage = usage,
        model = response.result?.metadata?.model
    )
}
```

### 5.3 에러 처리

```kotlin
fun generateAnswerSafely(prompt: Prompt): Result<String> {
    return try {
        val response = chatModel.call(prompt)
        val answer = response.results.firstOrNull()?.output?.text
            ?: return Result.failure(Exception("응답이 비어있습니다."))
        Result.success(answer)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

---

## 6. 완전한 RAG 파이프라인 구현

### 6.1 기본 RAG 파이프라인

```kotlin
@Service
class CompleteRAGService(
    private val vectorStore: VectorStore,
    private val chatModel: ChatModel
) {
    private val ragTemplate = PromptTemplate("""
        다음 문서를 참고하여 질문에 답변해주세요:
        
        {context}
        
        질문: {question}
        
        답변:
    """.trimIndent())
    
    fun ask(question: String, topK: Int = 3): RAGResult {
        // 1. Retrieval: 문서 검색
        val documents = vectorStore.similaritySearch(question) ?: emptyList()
        val topDocuments = documents.take(topK)
        
        // 2. Augmentation: Context 생성
        val context = topDocuments.joinToString("\n\n---\n\n") { doc ->
            val title = doc.metadata["title"] as? String ?: "문서"
            "[$title]\n${doc.text}"
        }
        
        // 3. PromptTemplate에 주입
        val prompt = ragTemplate.create(mapOf(
            "context" to context,
            "question" to question
        ))
        
        // 4. Generation: LLM 호출
        val response = chatModel.call(prompt)
        val answer = response.results.firstOrNull()?.output?.text ?: ""
        
        // 5. 결과 반환
        return RAGResult(
            question = question,
            answer = answer,
            sources = topDocuments.map { doc ->
                DocumentSource(
                    content = doc.text ?: "",
                    metadata = doc.metadata
                )
            },
            context = context
        )
    }
}
```

### 6.2 고급 RAG 파이프라인

#### 재랭킹(Re-ranking) 포함

```kotlin
fun askWithReranking(question: String, topK: Int = 5, finalK: Int = 3): RAGResult {
    // 1. Retrieval: 더 많은 문서 검색
    val documents = vectorStore.similaritySearch(question) ?: emptyList()
    val topDocuments = documents.take(topK)
    
    // 2. Re-ranking: 질문과의 관련성 재평가
    val reranked = topDocuments.sortedByDescending { doc ->
        // 간단한 키워드 매칭으로 재랭킹 (실제로는 더 정교한 방법 사용)
        val questionWords = question.lowercase().split(" ")
        val docText = (doc.text ?: "").lowercase()
        questionWords.count { it in docText }
    }
    
    // 3. Augmentation: 상위 finalK개만 Context로 변환
    val finalDocuments = reranked.take(finalK)
    val context = finalDocuments.joinToString("\n\n---\n\n") { it.text }
    
    // 4. Generation
    val prompt = ragTemplate.create(mapOf(
        "context" to context,
        "question" to question
    ))
    val response = chatModel.call(prompt)
    val answer = response.results.firstOrNull()?.output?.text ?: ""
    
    return RAGResult(
        question = question,
        answer = answer,
        sources = finalDocuments.map { doc ->
            DocumentSource(
                content = doc.text ?: "",
                metadata = doc.metadata
            )
        },
        context = context
    )
}
```

#### 멀티 턴 대화 지원

```kotlin
class ConversationRAGService(
    private val vectorStore: VectorStore,
    private val chatModel: ChatModel
) {
    private val conversationHistory = mutableListOf<Pair<String, String>>()
    
    private val conversationRAGTemplate = PromptTemplate("""
        다음 문서를 참고하여 질문에 답변해주세요:
        
        {context}
        
        이전 대화:
        {history}
        
        현재 질문: {question}
        
        답변:
    """.trimIndent())
    
    fun ask(question: String, topK: Int = 3): String {
        // 1. Retrieval
        val documents = vectorStore.similaritySearch(question) ?: emptyList()
        val context = documents.take(topK).joinToString("\n\n") { it.text }
        
        // 2. 대화 이력 생성
        val history = conversationHistory.takeLast(3).joinToString("\n") { (q, a) ->
            "Q: $q\nA: $a"
        }
        
        // 3. PromptTemplate에 주입
        val prompt = conversationRAGTemplate.create(mapOf(
            "context" to context,
            "history" to history,
            "question" to question
        ))
        
        // 4. Generation
        val response = chatModel.call(prompt)
        val answer = response.results.firstOrNull()?.output?.text ?: ""
        
        // 5. 대화 이력 업데이트
        conversationHistory.add(question to answer)
        
        return answer
    }
}
```

---

## 7. PromptTemplate 패턴

### 7.1 다양한 PromptTemplate 패턴

#### 패턴 1: 간단한 Context 주입

```kotlin
private val simpleTemplate = PromptTemplate("""
    문서:
    {context}
    
    질문: {question}
""".trimIndent())
```

#### 패턴 2: 구조화된 Context

```kotlin
private val structuredTemplate = PromptTemplate("""
    당신은 전문가입니다. 다음 문서를 참고하여 질문에 답변해주세요.
    
    === 참고 문서 ===
    {context}
    ================
    
    질문: {question}
    
    답변 형식:
    1. 문서에서 찾은 정보
    2. 추가 설명
    3. 관련 정보
""".trimIndent())
```

#### 패턴 3: 역할 기반 Template

```kotlin
private val roleBasedTemplate = PromptTemplate("""
    역할: {role}
    
    다음 문서를 참고하여 {tone}한 톤으로 답변해주세요:
    
    {context}
    
    질문: {question}
""".trimIndent())
```

### 7.2 Template 재사용

```kotlin
@Service
class TemplateRAGService(
    private val vectorStore: VectorStore,
    private val chatModel: ChatModel
) {
    // 여러 템플릿을 미리 정의
    private val qaTemplate = PromptTemplate("""
        문서: {context}
        질문: {question}
        답변:
    """.trimIndent())
    
    private val summaryTemplate = PromptTemplate("""
        다음 문서를 요약해주세요:
        {context}
    """.trimIndent())
    
    private val translationTemplate = PromptTemplate("""
        다음 문서를 {targetLanguage}로 번역해주세요:
        {context}
    """.trimIndent())
    
    fun ask(question: String): String {
        val documents = vectorStore.similaritySearch(question) ?: emptyList()
        val context = documents.take(3).joinToString("\n\n") { it.text }
        
        val prompt = qaTemplate.create(mapOf(
            "context" to context,
            "question" to question
        ))
        
        val response = chatModel.call(prompt)
        return response.results.firstOrNull()?.output?.text ?: ""
    }
    
    fun summarize(context: String): String {
        val prompt = summaryTemplate.create(mapOf("context" to context))
        val response = chatModel.call(prompt)
        return response.results.firstOrNull()?.output?.text ?: ""
    }
}
```

---

## 8. 실전 활용 예제

### 8.1 고객 지원 챗봇

```kotlin
@Service
class CustomerSupportRAGService(
    private val vectorStore: VectorStore,
    private val chatModel: ChatModel
) {
    private val supportTemplate = PromptTemplate("""
        당신은 고객 지원 담당자입니다.
        다음 정책 문서를 참고하여 고객의 질문에 친절하고 정확하게 답변해주세요.
        
        정책 문서:
        {context}
        
        고객 질문: {question}
        
        답변 시 주의사항:
        - 정책 문서에 명시된 내용만 답변하세요
        - 불확실한 내용은 "확인 후 답변드리겠습니다"라고 말하세요
        - 친절하고 전문적인 톤을 유지하세요
    """.trimIndent())
    
    fun answerCustomerQuestion(question: String): CustomerSupportResponse {
        val documents = vectorStore.similaritySearch(question) ?: emptyList()
        val context = documents.take(3).joinToString("\n\n---\n\n") { doc ->
            "[${doc.metadata["title"]}]\n${doc.text}"
        }
        
        val prompt = supportTemplate.create(mapOf(
            "context" to context,
            "question" to question
        ))
        
        val response = chatModel.call(prompt)
        val answer = response.results.firstOrNull()?.output?.text ?: ""
        
        return CustomerSupportResponse(
            answer = answer,
            referencedPolicies = documents.take(3).map { it.metadata["title"] as? String ?: "" }
        )
    }
}
```

### 8.2 지식베이스 Q&A

```kotlin
@Service
class KnowledgeBaseRAGService(
    private val vectorStore: VectorStore,
    private val chatModel: ChatModel
) {
    private val kbTemplate = PromptTemplate("""
        다음 지식베이스 문서를 참고하여 질문에 답변해주세요.
        
        지식베이스:
        {context}
        
        질문: {question}
        
        답변 형식:
        1. 핵심 답변
        2. 상세 설명
        3. 관련 정보
    """.trimIndent())
    
    fun askKnowledgeBase(question: String, topic: String? = null): KnowledgeBaseResponse {
        val documents = if (topic != null) {
            val allDocs = vectorStore.similaritySearch(question) ?: emptyList()
            allDocs.filter { it.metadata["topic"] == topic }.take(5)
        } else {
            vectorStore.similaritySearch(question)?.take(5) ?: emptyList()
        }
        
        val context = documents.joinToString("\n\n---\n\n") { doc ->
            """
            [${doc.metadata["title"]}]
            주제: ${doc.metadata["topic"]}
            ${doc.text}
            """.trimIndent()
        }
        
        val prompt = kbTemplate.create(mapOf(
            "context" to context,
            "question" to question
        ))
        
        val response = chatModel.call(prompt)
        val answer = response.results.firstOrNull()?.output?.text ?: ""
        
        return KnowledgeBaseResponse(
            answer = answer,
            sources = documents.map { it.metadata["title"] as? String ?: "" }
        )
    }
}
```

---

## 9. 최적화 및 베스트 프랙티스

### 9.1 Context 품질 개선

#### ✅ 좋은 Context 생성

```kotlin
// 1. 관련성 높은 문서만 선택
val documents = vectorStore.similaritySearch(question) ?: emptyList()
val relevantDocs = documents.filter { doc ->
    // 관련성 점수 계산 (예시)
    calculateRelevance(question, doc.text) > 0.7
}.take(5)

// 2. 구조화된 Context
val context = relevantDocs.mapIndexed { index, doc ->
    """
    [문서 ${index + 1}: ${doc.metadata["title"]}]
    ${doc.text}
    """.trimIndent()
}.joinToString("\n\n---\n\n")
```

#### ❌ 나쁜 Context 생성

```kotlin
// 1. 모든 문서 포함 (너무 길고 무관련)
val context = allDocuments.joinToString("\n\n") { it.text }

// 2. 구조 없는 Context
val context = documents.map { it.text }.joinToString(" ")
```

### 9.2 PromptTemplate 최적화

#### ✅ 좋은 Template

```kotlin
// 명확한 지시사항과 구조화된 형식
private val goodTemplate = PromptTemplate("""
    역할: {role}
    
    다음 문서를 참고하여 질문에 답변해주세요:
    
    === 참고 문서 ===
    {context}
    ================
    
    질문: {question}
    
    답변 시 다음을 준수하세요:
    1. 문서에 명시된 내용만 답변
    2. 문서에 없는 내용은 "문서에 해당 정보가 없습니다"라고 답변
    3. 친절하고 전문적인 톤 유지
""".trimIndent())
```

#### ❌ 나쁜 Template

```kotlin
// 모호한 지시사항
private val badTemplate = PromptTemplate("""
    {context}
    {question}
""".trimIndent())
```

### 9.3 성능 최적화

#### 배치 처리

```kotlin
fun askBatch(questions: List<String>): List<String> {
    // 각 질문에 대해 병렬 처리 (실제로는 적절한 동시성 제어 필요)
    return questions.map { question ->
        ask(question)
    }
}
```

#### 캐싱

```kotlin
@Service
class CachedRAGService(
    private val vectorStore: VectorStore,
    private val chatModel: ChatModel
) {
    private val cache = mutableMapOf<String, String>()
    
    fun ask(question: String): String {
        // 캐시 확인
        cache[question]?.let { return it }
        
        // RAG 파이프라인 실행
        val answer = performRAG(question)
        
        // 캐시 저장
        cache[question] = answer
        
        return answer
    }
}
```

---

## 10. 요약

### 10.1 핵심 내용 정리

1. **Retrieval**: `VectorStore.similaritySearch()`로 관련 문서 검색
2. **Augmentation**: 검색된 문서를 Context 문자열로 변환
3. **PromptTemplate**: 동적 값을 주입할 수 있는 프롬프트 템플릿
4. **Context 주입**: `PromptTemplate.create()`로 Context와 질문 주입
5. **Generation**: `ChatModel.call()`로 최종 답변 생성

### 10.2 기본 패턴

```kotlin
// 1. Retrieval: 문서 검색
val documents = vectorStore.similaritySearch(question) ?: emptyList()
val topK = documents.take(3)

// 2. Augmentation: Context 생성
val context = topK.joinToString("\n\n---\n\n") { it.text }

// 3. PromptTemplate에 주입
val prompt = ragTemplate.create(mapOf(
    "context" to context,
    "question" to question
))

// 4. Generation: LLM 호출
val response = chatModel.call(prompt)
val answer = response.results.firstOrNull()?.output?.text ?: ""
```

### 10.3 다음 학습 내용

이제 RAG 파이프라인을 구현했으니, 다음 장에서는:
- **문서 로딩**: PDF, TXT 등 외부 문서를 VectorStore에 추가
- **문서 분할**: 긴 문서를 의미 있는 단위로 분할
- **고급 RAG**: 멀티 히스토리, 재랭킹 등

---

## 📚 참고 자료

- [Spring AI RAG 패턴 문서](https://docs.spring.io/spring-ai/reference/patterns/rag.html)
- [PromptTemplate API 문서](https://docs.spring.io/spring-ai/reference/api/prompt.html)
- [VectorStore API 문서](https://docs.spring.io/spring-ai/reference/api/vectordb.html)

---

## ❓ 학습 확인 문제

다음 질문에 답할 수 있는지 확인해보세요:

1. VectorStore.similaritySearch()를 사용하여 문서를 검색하는 방법은?
2. 검색된 문서를 Context로 변환하는 방법은?
3. PromptTemplate을 사용하여 Context를 주입하는 방법은?
4. ChatModel에 Context와 질문을 함께 전달하는 방법은?
5. 완전한 RAG 파이프라인을 구현하는 방법은?
6. PromptTemplate을 재사용하는 방법은?

---

**다음 장**: [8.1: 문서 로딩 (Document Loaders)](../README.md#81-문서-로딩-document-loaders)

