# 간단한 RAG 파이프라인 구현 샘플 프로젝트

이 프로젝트는 PromptTemplate을 사용하여 Context를 주입하는 RAG 파이프라인을 구현합니다.

## 📁 프로젝트 구조

```
sample/
├── src/main/kotlin/com/example/springai/
│   ├── RAGPipelineApplication.kt              # 메인 애플리케이션
│   ├── config/
│   │   └── VectorStoreConfig.kt                # VectorStore Bean 설정
│   ├── controller/
│   │   ├── BasicRAGController.kt               # 기본 RAG API
│   │   ├── AdvancedRAGController.kt            # 고급 RAG API
│   │   ├── ConversationRAGController.kt        # 대화 이력 지원 RAG API
│   │   └── DemoController.kt                   # 샘플 데이터 초기화
│   ├── service/
│   │   ├── PromptTemplateRAGService.kt         # PromptTemplate 기반 RAG
│   │   ├── AdvancedRAGService.kt                # 고급 RAG (재랭킹, 길이 제한)
│   │   ├── ConversationRAGService.kt            # 대화 이력 지원 RAG
│   │   └── KnowledgeBaseService.kt              # 지식베이스 관리
│   └── model/
│       └── CommonModels.kt                     # 공통 모델
└── src/main/resources/
    └── application.yml                         # 설정 파일
```

## 🚀 빠른 시작

### 1. 환경 변수 설정

```bash
export OPENAI_API_KEY="sk-your-api-key-here"
```

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

### 3. 샘플 데이터 초기화

```bash
curl -X POST http://localhost:8080/api/demo/init
```

### 4. RAG 질문 답변 테스트

```bash
curl -X POST http://localhost:8080/api/rag/ask \
  -H "Content-Type: application/json" \
  -d '{
    "question": "환불 정책은 무엇인가요?",
    "topK": 3
  }'
```

## 📝 주요 예제 설명

### 1. PromptTemplateRAGService (핵심 RAG 파이프라인)

**RAG 파이프라인의 3단계:**

1. **Retrieval**: `VectorStore.similaritySearch()`로 문서 검색
2. **Augmentation**: 검색된 문서를 Context로 변환
3. **Generation**: `PromptTemplate.create()`로 Context 주입 후 LLM 호출

```kotlin
fun ask(question: String, topK: Int = 3): RAGResult {
    // 1. Retrieval: 문서 검색
    val documents = vectorStore.similaritySearch(question) ?: emptyList()
    val topDocuments = documents.take(topK)
    
    // 2. Augmentation: Context 생성
    val context = topDocuments.joinToString("\n\n---\n\n") { it.text }
    
    // 3. PromptTemplate에 주입
    val prompt = basicRAGTemplate.create(mapOf(
        "context" to context,
        "question" to question
    ))
    
    // 4. Generation: LLM 호출
    val response = chatModel.call(prompt)
    val answer = response.results.firstOrNull()?.output?.text ?: ""
    
    return RAGResult(...)
}
```

### 2. BasicRAGController

**API 엔드포인트:**
- `POST /api/rag/ask`: 기본 RAG 질문 답변
- `GET /api/rag/ask?question=...&topK=3`: GET 방식 질문 답변
- `POST /api/rag/ask-structured`: 구조화된 RAG 질문 답변
- `POST /api/rag/ask-with-role`: 역할 기반 RAG 질문 답변
- `POST /api/rag/documents`: 문서 추가

### 3. AdvancedRAGController

**고급 기능:**
- `POST /api/rag/advanced/ask-with-reranking`: 재랭킹 포함 RAG
- `POST /api/rag/advanced/ask-with-length-limit`: Context 길이 제한 RAG
- `POST /api/rag/advanced/ask-with-category`: 카테고리 필터링 RAG

### 4. ConversationRAGController

**대화 이력 지원:**
- `POST /api/rag/conversation/ask`: 대화 이력 포함 RAG
- `GET /api/rag/conversation/history`: 대화 이력 조회
- `DELETE /api/rag/conversation/history`: 대화 이력 초기화

## 💡 학습 포인트

이 샘플 프로젝트를 통해 학습할 수 있는 내용:

1. **VectorStore.similaritySearch()**
   - 관련 문서 검색
   - topK로 결과 제한

2. **Context 생성**
   - 문서를 Context 문자열로 변환
   - 메타데이터 포함/제외 선택

3. **PromptTemplate 사용**
   - 동적 프롬프트 생성
   - Context와 질문 주입
   - 여러 변수 사용

4. **ChatModel 호출**
   - Context가 포함된 프롬프트 전달
   - 응답 처리

5. **고급 기능**
   - 재랭킹
   - Context 길이 제한
   - 카테고리 필터링
   - 대화 이력 관리

## 🔧 핵심 패턴

```kotlin
// 1. PromptTemplate 정의
private val ragTemplate = PromptTemplate("""
    다음 문서를 참고하여 질문에 답변해주세요:
    
    {context}
    
    질문: {question}
""".trimIndent())

// 2. Retrieval: 문서 검색
val documents = vectorStore.similaritySearch(question) ?: emptyList()
val topK = documents.take(3)

// 3. Augmentation: Context 생성
val context = topK.joinToString("\n\n---\n\n") { it.text }

// 4. PromptTemplate에 주입
val prompt = ragTemplate.create(mapOf(
    "context" to context,
    "question" to question
))

// 5. Generation: LLM 호출
val response = chatModel.call(prompt)
val answer = response.results.firstOrNull()?.output?.text ?: ""
```

## 📚 참고사항

### PromptTemplate의 장점

- **재사용성**: 템플릿을 한 번 정의하고 여러 번 사용
- **유지보수성**: 프롬프트 구조 변경이 쉬움
- **가독성**: 프롬프트 구조가 명확함

### Context 주입 방법

```kotlin
// 방법 1: 기본 주입
val prompt = ragTemplate.create(mapOf(
    "context" to context,
    "question" to question
))

// 방법 2: 여러 변수 주입
val prompt = advancedTemplate.create(mapOf(
    "role" to "고객 지원 담당자",
    "context" to context,
    "question" to question,
    "tone" to "친절"
))
```

### RAG 파이프라인 최적화

- **topK 조정**: 검색 문서 수 조정
- **Context 길이 제한**: 토큰 제한 고려
- **재랭킹**: 관련성 재평가
- **필터링**: 메타데이터 기반 필터링

---

**다음 학습**: [8.1: 문서 로딩 (Document Loaders)](../../README.md#81-문서-로딩-document-loaders)

