# Spring AI VectorStore 추상화 (In-Memory) 샘플 프로젝트

이 프로젝트는 Spring AI에서 SimpleVectorStore를 사용하여 문서를 저장하고 검색하는 방법을 보여줍니다.

## 📁 프로젝트 구조

```
sample/
├── src/main/kotlin/com/example/springai/
│   ├── VectorStoreApplication.kt              # 메인 애플리케이션
│   ├── config/
│   │   └── VectorStoreConfig.kt                # VectorStore Bean 설정
│   ├── controller/
│   │   ├── BasicDocumentController.kt         # 기본 문서 추가 및 검색
│   │   ├── BatchDocumentController.kt         # 배치 문서 추가
│   │   └── ServiceBasedController.kt          # 서비스 기반 사용
│   ├── service/
│   │   ├── DocumentService.kt                 # 문서 관리 서비스
│   │   ├── KnowledgeBaseService.kt            # 지식베이스 서비스
│   │   └── FAQService.kt                       # FAQ 서비스
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

### 2. 실행

```bash
./gradlew bootRun
```

### 3. 테스트

#### 기본 문서 추가

```bash
curl -X POST http://localhost:8080/api/documents/add \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Spring AI는 프레임워크입니다.",
    "metadata": {"category": "framework"}
  }'
```

#### 유사도 검색

```bash
curl -X POST http://localhost:8080/api/documents/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "프로그래밍",
    "topK": 5
  }'
```

## 📝 주요 예제 설명

### 1. BasicDocumentController

**기본 VectorStore 사용:**
- `/api/documents/add`: 단일 문서 추가
- `/api/documents/search`: 유사도 검색 (POST)
- `/api/documents/search?query=...&topK=5`: 유사도 검색 (GET)

### 2. BatchDocumentController

**배치 문서 추가:**
- `/api/documents/batch/add`: 여러 문서를 한 번에 추가

### 3. ServiceBasedController

**서비스 기반 사용:**
- `/api/service/document/*`: 문서 관리 서비스
- `/api/service/knowledge/*`: 지식베이스 서비스
- `/api/service/faq/*`: FAQ 서비스

## 💡 학습 포인트

이 샘플 프로젝트를 통해 학습할 수 있는 내용:

1. **VectorStore 인터페이스**
   - add() 메서드로 문서 추가
   - similaritySearch() 메서드로 검색

2. **SimpleVectorStore 설정**
   - EmbeddingModel을 사용한 Bean 생성
   - 메모리 기반 저장소

3. **Document 클래스**
   - 텍스트와 메타데이터로 구성
   - ID 자동 생성 또는 명시

4. **메타데이터 활용**
   - 필터링 및 통계
   - 문서 분류

5. **실전 활용**
   - 문서 관리 시스템
   - 지식베이스 시스템
   - FAQ 시스템

## 🔧 핵심 패턴

```kotlin
// 1. VectorStore Bean 생성
@Configuration
class VectorStoreConfig(
    private val embeddingModel: EmbeddingModel
) {
    @Bean
    fun vectorStore(): VectorStore {
        return SimpleVectorStore.builder(embeddingModel).build()
    }
}

// 2. 문서 추가
val document = Document("문서 내용", metadata)
vectorStore.add(listOf(document))

// 3. 유사도 검색
val results = vectorStore.similaritySearch("쿼리") ?: emptyList()
val limitedResults = results.take(5)  // topK 제한
```

## 📚 참고사항

### VectorStore 인터페이스

Spring AI의 VectorStore는 벡터 저장소를 추상화한 인터페이스입니다:
- `add(List<Document>)`: 문서 추가
- `similaritySearch(query: String)`: 유사도 검색 (Spring AI 1.0.0-M6)
- `similaritySearch(request: SearchRequest)`: 고급 검색 옵션

**참고**: Spring AI 1.0.0-M6에서는 `similaritySearch(query: String)`가 기본이며, topK는 결과를 `take(topK)`로 제한합니다.

### Document 클래스

```kotlin
// Spring AI 1.0.0-M6의 Document 생성자
Document(
    text: String,                  // 문서 내용
    metadata: Map<String, Any> = emptyMap()  // 메타데이터
)

// 또는 ID 포함
Document(
    id: String,                    // 문서 ID
    text: String,                  // 문서 내용
    metadata: Map<String, Any> = emptyMap()  // 메타데이터
)

// 접근: doc.text (content가 아님)
```

### SimpleVectorStore 특징

- **메모리 기반**: 모든 데이터를 메모리에 저장
- **간단함**: 별도 설정 없이 사용 가능
- **테스트용**: 개발 및 테스트에 적합
- **제한사항**: 서버 재시작 시 데이터 손실

---

**다음 학습**: [6.3: 외부 벡터 저장소 연동 (PostgreSQL/PGVector)](../../README.md#63-외부-벡터-저장소-연동-postgresqlpgvector)

