# RAG 패턴의 이해 샘플 프로젝트

이 프로젝트는 RAG (Retrieval-Augmented Generation) 패턴의 기본 개념을 이해하고 간단한 RAG 구현을 보여줍니다.

## 📁 프로젝트 구조

```
sample/
├── src/main/kotlin/com/example/springai/
│   ├── RAGApplication.kt                    # 메인 애플리케이션
│   ├── config/
│   │   └── VectorStoreConfig.kt             # VectorStore Bean 설정
│   ├── controller/
│   │   ├── RAGController.kt                # RAG 질문 답변 API
│   │   └── DemoController.kt                # 샘플 데이터 초기화
│   ├── service/
│   │   ├── SimpleRAGService.kt              # RAG 서비스 (핵심 로직)
│   │   └── KnowledgeBaseService.kt           # 지식베이스 관리
│   └── model/
│       └── CommonModels.kt                  # 공통 모델
└── src/main/resources/
    └── application.yml                      # 설정 파일
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

### 1. SimpleRAGService (핵심 RAG 로직)

**RAG 패턴의 4단계:**

1. **Retrieval (검색)**: 질문에 대한 관련 문서 검색
2. **Augmentation (증강)**: 검색된 문서를 Context로 변환
3. **Generation (생성)**: Context와 질문을 LLM에 전달
4. **Response (응답)**: 답변과 출처 정보 반환

```kotlin
fun ask(question: String, topK: Int = 3): RAGResult {
    // 1. Retrieval: 관련 문서 검색
    val documents = vectorStore.similaritySearch(question) ?: emptyList()
    val topDocuments = documents.take(topK)
    
    // 2. Augmentation: Context 생성
    val context = topDocuments.joinToString("\n\n---\n\n") { doc ->
        "[문서: ${doc.metadata["title"]}]\n${doc.text}"
    }
    
    // 3. Generation: LLM 호출
    val prompt = Prompt(
        listOf(
            SystemMessage("문서를 참고하여 정확하게 답변해주세요."),
            UserMessage("참고 문서:\n$context\n\n질문: $question")
        )
    )
    val response = chatModel.call(prompt)
    
    // 4. Response: 결과 반환
    return RAGResult(...)
}
```

### 2. RAGController

**API 엔드포인트:**
- `POST /api/rag/ask`: RAG 질문 답변
- `GET /api/rag/ask?question=...&topK=3`: GET 방식 질문 답변
- `POST /api/rag/documents`: 문서 추가

### 3. DemoController

**샘플 데이터 초기화:**
- `POST /api/demo/init`: RAG 테스트를 위한 샘플 문서 추가

## 💡 학습 포인트

이 샘플 프로젝트를 통해 학습할 수 있는 내용:

1. **RAG 패턴의 이해**
   - Retrieval, Augmentation, Generation의 개념
   - 각 단계의 역할과 중요성

2. **VectorStore 활용**
   - similaritySearch()로 관련 문서 검색
   - 검색 결과를 Context로 변환

3. **ChatModel 활용**
   - SystemMessage와 UserMessage 조합
   - Context를 포함한 프롬프트 생성

4. **Grounding**
   - 실제 문서 기반 응답 생성
   - 출처 정보 제공

## 🔧 핵심 패턴

```kotlin
// RAG 패턴의 기본 흐름
fun ask(question: String): String {
    // 1. Retrieval: 관련 문서 검색
    val documents = vectorStore.similaritySearch(question) ?: emptyList()
    val topK = documents.take(3)
    
    // 2. Augmentation: Context 생성
    val context = topK.joinToString("\n\n") { it.text }
    
    // 3. Generation: LLM 호출
    val prompt = Prompt(
        listOf(
            SystemMessage("문서를 참고하여 답변해주세요."),
            UserMessage("문서:\n$context\n\n질문: $question")
        )
    )
    val response = chatModel.call(prompt)
    
    // 4. Response: 답변 반환
    return response.results.firstOrNull()?.output?.text ?: ""
}
```

## 📚 참고사항

### RAG의 장점

- ✅ **정확성**: 실제 문서 기반 응답
- ✅ **최신성**: 최신 문서 추가 가능
- ✅ **신뢰성**: 출처 명시로 검증 가능
- ✅ **환각 감소**: 문서에 없는 내용은 추측하지 않음

### RAG의 단점

- ⚠️ **검색 품질 의존**: 관련 문서를 찾지 못하면 답변 품질 저하
- ⚠️ **지연 시간**: 검색 + LLM 호출 시간
- ⚠️ **Context 길이 제한**: 토큰 제한으로 인한 문서 수 제한

---

**다음 학습**: [7.2: 간단한 RAG 파이프라인 구현](../../README.md#72-간단한-rag-파이프라인-구현)

