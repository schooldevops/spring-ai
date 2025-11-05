# 벡터 저장소 필요성 샘플 프로젝트

이 프로젝트는 Spring AI에서 VectorStore를 사용하여 벡터 데이터를 저장하고 검색하는 방법을 보여줍니다.

## 📁 프로젝트 구조

```
sample/
├── src/main/kotlin/com/example/springai/
│   ├── VectorDBApplication.kt              # 메인 애플리케이션
│   ├── config/
│   │   └── VectorStoreConfig.kt           # VectorStore Bean 설정
│   ├── controller/
│   │   ├── BasicVectorStoreController.kt          # 기본 VectorStore 사용
│   │   ├── BatchVectorStoreController.kt         # 배치 문서 추가
│   │   ├── AdvancedVectorStoreController.kt      # 고급 기능
│   │   └── ServiceBasedVectorStoreController.kt  # 서비스 기반 사용
│   ├── service/
│   │   └── DocumentManagementService.kt         # 문서 관리 서비스
│   └── model/
│       └── CommonModels.kt                        # 공통 모델
└── src/main/resources/
    └── application.yml                           # 설정 파일
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
curl -X POST http://localhost:8080/api/vectordb/add \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Spring AI는 프레임워크입니다.",
    "metadata": {"category": "framework"}
  }'
```

#### 유사도 검색

```bash
curl -X POST http://localhost:8080/api/vectordb/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "프로그래밍",
    "topK": 5
  }'
```

## 📝 주요 예제 설명

### 1. BasicVectorStoreController

**기본 VectorStore 사용:**
- `/api/vectordb/add`: 단일 문서 추가
- `/api/vectordb/search`: 유사도 검색 (POST)
- `/api/vectordb/search?query=...&topK=5`: 유사도 검색 (GET)

### 2. BatchVectorStoreController

**배치 문서 추가:**
- `/api/vectordb/batch/add`: 여러 문서를 한 번에 추가

### 3. AdvancedVectorStoreController

**고급 기능:**
- `/api/vectordb/advanced/add-with-metadata`: 메타데이터와 함께 문서 추가
- `/api/vectordb/advanced/search-with-filter`: 메타데이터 필터링 검색
- `/api/vectordb/advanced/search-with-threshold`: 임계값 기반 검색

### 4. ServiceBasedVectorStoreController

**서비스 기반 사용:**
- `/api/vectordb/service/add`: 서비스를 통한 문서 추가
- `/api/vectordb/service/search`: 서비스를 통한 검색
- `/api/vectordb/service/stats`: 문서 통계 조회

## 💡 학습 포인트

이 샘플 프로젝트를 통해 학습할 수 있는 내용:

1. **VectorStore의 필요성**
   - 대규모 벡터 데이터 관리
   - 효율적인 검색 성능

2. **SimpleVectorStore 사용**
   - 메모리 기반 벡터 저장소
   - 테스트 및 개발 환경에 적합

3. **Document 클래스**
   - 벡터 저장소의 기본 저장 단위
   - 메타데이터 포함

4. **유사도 검색**
   - 의미 기반 검색
   - Top-K 결과 반환

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

**다음 학습**: [6.2: Spring AI VectorStore 추상화 (In-Memory)](../../README.md#62-spring-ai-vectordb-추상화-in-memory)

