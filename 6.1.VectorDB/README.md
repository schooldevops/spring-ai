# 6.1: 벡터 저장소의 필요성

## 📖 학습 목표

이 강의를 마친 후 다음을 달성할 수 있습니다:
- **벡터 저장소(Vector Database)**의 필요성을 이해할 수 있습니다
- **대규모 벡터 데이터 관리**의 어려움과 해결 방안을 이해할 수 있습니다
- **HNSW, ANN** 등의 벡터 검색 알고리즘 개념을 이해할 수 있습니다
- **VectorStore 인터페이스**의 기본 개념을 이해할 수 있습니다
- **SimpleVectorStore (In-Memory)**를 사용하여 기본적인 벡터 저장 및 검색을 구현할 수 있습니다

---

## 🔑 핵심 키워드

이 장에서 다루는 핵심 키워드들:

1. **Vector Database (벡터 데이터베이스)** - 벡터 데이터를 효율적으로 저장하고 검색하는 전용 데이터베이스
2. **HNSW (Hierarchical Navigable Small World)** - 고속 벡터 검색을 위한 그래프 기반 알고리즘
3. **ANN (Approximate Nearest Neighbor)** - 근사 최근접 이웃 검색 알고리즘
4. **VectorStore** - Spring AI의 벡터 저장소 추상화 인터페이스
5. **SimpleVectorStore** - 메모리 기반 간단한 벡터 저장소 구현체

---

## 1. 벡터 저장소가 필요한 이유

### 1.1 문제 상황: 대규모 벡터 데이터 관리

#### 시나리오: 10,000개의 문서 검색

```
5.1장에서 배운 방법:
- 모든 문서를 메모리에 저장
- 검색 시 모든 문서와 유사도 계산
- O(n) 시간 복잡도

문제점:
- 10,000개 문서 × 1536차원 = 수백만 개의 숫자
- 메모리 부족
- 검색 속도 저하 (모든 문서와 비교)
- 서버 재시작 시 데이터 손실
```

#### 실제 계산 예시

```kotlin
// 10,000개 문서 검색 시
val documents = mutableListOf<Document>() // 10,000개 저장

fun search(query: String): List<SearchResult> {
    val queryVector = embeddingModel.embed(query) // 1536차원
    
    // 모든 문서와 비교 (10,000번 계산)
    val results = documents.map { doc ->
        cosineSimilarity(queryVector, doc.embedding)
    }
    
    // 문제:
    // - 시간: O(n) - 모든 문서와 비교
    // - 메모리: 모든 벡터를 메모리에 유지
    // - 확장성: 문서 수가 증가하면 선형적으로 느려짐
}
```

### 1.2 벡터 저장소의 해결책

#### 벡터 저장소의 역할

```
벡터 저장소:
1. 영구 저장: 디스크에 벡터 저장 (서버 재시작해도 유지)
2. 인덱싱: HNSW, ANN 등으로 빠른 검색
3. 확장성: 수백만 개의 벡터도 효율적으로 처리
4. 최적화: 벡터 검색에 특화된 알고리즘
```

#### 성능 비교

| 방법 | 문서 수 | 검색 시간 | 메모리 |
|------|--------|----------|--------|
| **메모리 리스트** | 1,000 | ~100ms | 높음 |
| **메모리 리스트** | 10,000 | ~1초 | 매우 높음 |
| **메모리 리스트** | 100,000 | ~10초 | 불가능 |
| **벡터 저장소** | 1,000 | ~10ms | 중간 |
| **벡터 저장소** | 10,000 | ~20ms | 중간 |
| **벡터 저장소** | 100,000 | ~50ms | 중간 |
| **벡터 저장소** | 1,000,000 | ~100ms | 중간 |

---

## 2. 벡터 저장소란?

### 2.1 벡터 저장소의 정의

**벡터 저장소(Vector Database)**는 벡터 데이터를 효율적으로 저장하고 검색하기 위해 특별히 설계된 데이터베이스입니다.

#### 일반 데이터베이스 vs 벡터 저장소

**일반 데이터베이스 (PostgreSQL, MySQL 등):**
- 정확한 매칭 (WHERE name = 'value')
- 키워드 검색 (LIKE '%keyword%')
- 인덱싱: B-Tree, Hash 등
- **문제**: 벡터 유사도 검색에 비효율적

**벡터 저장소 (Pinecone, Weaviate, PGVector 등):**
- 유사도 기반 검색 (nearest neighbor)
- 벡터 인덱싱: HNSW, IVF 등
- **장점**: 수백만 개 벡터도 빠르게 검색

### 2.2 벡터 저장소의 주요 기능

1. **벡터 저장**: 임베딩 벡터를 영구적으로 저장
2. **유사도 검색**: 쿼리 벡터와 가장 유사한 벡터 찾기
3. **인덱싱**: HNSW, ANN 등으로 빠른 검색
4. **메타데이터 필터링**: 벡터와 함께 메타데이터 저장/검색
5. **확장성**: 대규모 데이터 처리

---

## 3. HNSW와 ANN 알고리즘

### 3.1 ANN (Approximate Nearest Neighbor)

**ANN**은 정확한 최근접 이웃을 찾는 대신, **근사적으로** 가장 가까운 이웃을 빠르게 찾는 알고리즘입니다.

#### 정확한 검색 vs 근사 검색

```
정확한 검색 (Exact Search):
- 모든 벡터와 거리 계산
- O(n) 시간 복잡도
- 정확하지만 느림

근사 검색 (Approximate Search):
- 일부 벡터만 검사
- O(log n) 시간 복잡도
- 빠르지만 약간의 정확도 손실
```

### 3.2 HNSW (Hierarchical Navigable Small World)

**HNSW**는 그래프 기반의 벡터 검색 알고리즘으로, **계층적 그래프 구조**를 사용합니다.

#### HNSW의 동작 원리

```
1. 그래프 구조 생성
   - 각 벡터를 노드로 표현
   - 유사한 벡터끼리 연결

2. 계층적 탐색
   - 상위 계층: 빠른 탐색 (적은 노드)
   - 하위 계층: 정확한 탐색 (모든 노드)

3. 결과
   - O(log n) 시간 복잡도
   - 매우 빠른 검색 속도
```

#### HNSW의 장점

- **빠른 검색**: O(log n) 시간 복잡도
- **정확도**: 높은 정확도 유지
- **확장성**: 대규모 데이터에 적합

---

## 4. Spring AI의 VectorStore

### 4.1 VectorStore 인터페이스

**VectorStore**는 Spring AI에서 제공하는 벡터 저장소 추상화 인터페이스입니다.

#### 주요 메서드

```kotlin
interface VectorStore {
    // 문서 추가
    fun add(List<Document> documents)
    
    // 유사도 검색 (Spring AI 1.0.0-M6)
    fun similaritySearch(query: String): List<Document>?
    
    // 고급 검색 (SearchRequest 사용)
    fun similaritySearch(request: SearchRequest): List<Document>?
    
    // 문서 삭제
    fun delete(List<String> ids)
}
```

**참고**: Spring AI 1.0.0-M6에서는 `similaritySearch(query: String)`가 기본 메서드이며, topK는 결과를 `take(topK)`로 제한합니다.

### 4.2 VectorStore 구현체

Spring AI는 다양한 VectorStore 구현체를 제공합니다:

1. **SimpleVectorStore** (In-Memory)
   - 메모리 기반
   - 테스트 및 개발용
   - 서버 재시작 시 데이터 손실

2. **PineconeVectorStore**
   - Pinecone 클라우드 서비스 연동

3. **PGVectorStore**
   - PostgreSQL + PGVector 확장 연동

4. **ChromaVectorStore**
   - ChromaDB 연동

5. **WeaviateVectorStore**
   - Weaviate 연동

---

## 5. SimpleVectorStore (In-Memory)

### 5.1 SimpleVectorStore의 특징

**SimpleVectorStore**는 Spring AI에서 제공하는 메모리 기반 벡터 저장소입니다.

#### 특징

- **메모리 기반**: 모든 데이터를 메모리에 저장
- **간단함**: 별도 설정 없이 사용 가능
- **테스트용**: 개발 및 테스트에 적합
- **제한사항**: 서버 재시작 시 데이터 손실

### 5.2 SimpleVectorStore 사용 예제

#### 기본 사용법

```kotlin
@Configuration
class VectorStoreConfig(
    private val embeddingModel: EmbeddingModel
) {
    @Bean
    fun vectorStore(): VectorStore {
        return SimpleVectorStore.builder(embeddingModel).build()
    }
}
```

#### 문서 추가

```kotlin
@RestController
class DocumentController(
    private val vectorStore: VectorStore
) {
    @PostMapping("/documents")
    fun addDocument(@RequestBody request: DocumentRequest): Map<String, String> {
        val document = Document(request.text, request.metadata)
        vectorStore.add(listOf(document))
        
        return mapOf("status" to "success")
    }
}
```

#### 유사도 검색

```kotlin
@GetMapping("/search")
fun search(@RequestParam query: String, @RequestParam topK: Int = 5): List<Document> {
    val results = vectorStore.similaritySearch(query) ?: emptyList()
    return results.take(topK)
}
```

**참고**: Spring AI 1.0.0-M6에서는 `similaritySearch(query: String)` 메서드만 제공하며, topK는 결과에서 제한합니다.

---

## 6. Document 클래스 이해하기

### 6.1 Document 구조

Spring AI의 `Document` 클래스는 벡터 저장소에 저장되는 기본 단위입니다.

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

### 6.2 Document 생성 예제

```kotlin
// 기본 문서
val doc1 = Document("Spring AI는 프레임워크입니다.")

// 메타데이터 포함
val doc2 = Document(
    "Kotlin 프로그래밍 언어입니다.",
    mapOf(
        "category" to "programming",
        "author" to "JetBrains",
        "year" to 2011
    )
)

// ID 포함
val doc3 = Document(
    "doc-001",
    "Spring Boot는 자바 프레임워크입니다.",
    mapOf("category" to "framework")
)
```

---

## 7. 벡터 저장소의 활용 시나리오

### 7.1 문서 검색 시스템

```
사용자 질문
    ↓
질문 임베딩 생성
    ↓
VectorStore에서 유사 문서 검색
    ↓
상위 K개 문서 반환
    ↓
LLM에 컨텍스트로 전달
```

### 7.2 추천 시스템

```
사용자 프로필
    ↓
프로필 임베딩 생성
    ↓
VectorStore에서 유사 콘텐츠 검색
    ↓
추천 콘텐츠 반환
```

### 7.3 중복 검사

```
새 문서
    ↓
문서 임베딩 생성
    ↓
VectorStore에서 유사 문서 검색
    ↓
유사도가 높으면 중복으로 판단
```

---

## 8. 벡터 저장소 선택 가이드

### 8.1 개발/테스트 환경

**SimpleVectorStore (In-Memory)**
- ✅ 설정 간단
- ✅ 빠른 시작
- ❌ 데이터 영구 보존 안 됨
- ❌ 대규모 데이터에 부적합

### 8.2 소규모 프로덕션

**PGVector (PostgreSQL 확장)**
- ✅ 오픈소스
- ✅ PostgreSQL 기존 인프라 활용
- ✅ 메타데이터 필터링 지원
- ❌ 대규모 데이터에 상대적으로 느림

### 8.3 대규모 프로덕션

**Pinecone, Weaviate 등**
- ✅ 클라우드 관리형
- ✅ 매우 빠른 검색
- ✅ 확장성 우수
- ❌ 비용 발생

---

## 9. 실전 활용 예제

### 9.1 기본 문서 저장 및 검색

```kotlin
@RestController
class BasicVectorStoreController(
    private val vectorStore: VectorStore
) {
    @PostMapping("/add")
    fun addDocument(@RequestBody request: AddDocumentRequest): Map<String, String> {
        val document = Document(
            request.text,
            request.metadata ?: emptyMap()
        )
        vectorStore.add(listOf(document))
        
        return mapOf("status" to "success", "message" to "문서가 추가되었습니다.")
    }
    
    @GetMapping("/search")
    fun search(
        @RequestParam query: String,
        @RequestParam(defaultValue = "5") topK: Int
    ): Map<String, Any> {
        val documents = vectorStore.similaritySearch(query) ?: emptyList()
        val limitedResults = documents.take(topK)
        
        return mapOf(
            "query" to query,
            "topK" to topK,
            "results" to limitedResults.map { doc ->
                mapOf(
                    "content" to doc.text,
                    "metadata" to doc.metadata
                )
            }
        )
    }
}
```

### 9.2 배치 문서 추가

```kotlin
@RestController
class BatchVectorStoreController(
    private val vectorStore: VectorStore
) {
    @PostMapping("/add-batch")
    fun addBatch(@RequestBody request: BatchAddRequest): Map<String, Any> {
        val documents = request.texts.mapIndexed { index, text ->
            Document(
                text,
                mapOf(
                    "index" to index,
                    "source" to (request.source ?: "unknown")
                )
            )
        }
        
        vectorStore.add(documents)
        
        return mapOf(
            "status" to "success",
            "count" to documents.size
        )
    }
}
```

---

## 10. 주의사항 및 트러블슈팅

### 10.1 일반적인 문제들

#### 문제 1: SimpleVectorStore 데이터 손실

**증상:**
```
서버 재시작 후 데이터가 사라짐
```

**원인**: SimpleVectorStore는 메모리 기반

**해결책:**
- 프로덕션에서는 PGVector 등 영구 저장소 사용
- 개발 환경에서는 정기적으로 백업

#### 문제 2: 검색 결과가 예상과 다름

**증상:**
```
유사한 문서가 검색되지 않음
```

**해결책:**
- 임베딩 모델 확인
- topK 값 조정
- 검색 쿼리 개선

#### 문제 3: 메모리 부족

**증상:**
```
대량의 문서 추가 시 OutOfMemoryError
```

**해결책:**
- SimpleVectorStore 대신 외부 벡터 저장소 사용
- 문서 분할 최적화

---

## 11. 요약

### 11.1 핵심 내용 정리

1. **벡터 저장소의 필요성**: 대규모 벡터 데이터를 효율적으로 관리
2. **HNSW/ANN 알고리즘**: 빠른 벡터 검색을 위한 알고리즘
3. **VectorStore 인터페이스**: Spring AI의 벡터 저장소 추상화
4. **SimpleVectorStore**: 메모리 기반 간단한 구현체
5. **Document 클래스**: 벡터 저장소의 기본 저장 단위

### 11.2 기본 패턴

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
val document = Document("문서 내용")
vectorStore.add(listOf(document))

// 3. 유사도 검색
val results = vectorStore.similaritySearch("쿼리") ?: emptyList()
val limitedResults = results.take(5)  // topK 제한
```

### 11.3 다음 학습 내용

이제 벡터 저장소의 필요성을 배웠으니, 다음 장에서는:
- **VectorStore 구현**: SimpleVectorStore 상세 사용법
- **외부 벡터 저장소**: PGVector 등 연동
- **RAG 구현**: VectorStore를 활용한 검색 기반 생성

---

## 📚 참고 자료

- [Spring AI VectorStore 공식 문서](https://docs.spring.io/spring-ai/reference/api/vectordb.html)
- [HNSW 알고리즘 논문](https://arxiv.org/abs/1603.09320)
- [벡터 데이터베이스 비교](https://www.pinecone.io/learn/vector-database/)

---

## ❓ 학습 확인 문제

다음 질문에 답할 수 있는지 확인해보세요:

1. 벡터 저장소가 필요한 이유는 무엇인가요?
2. HNSW 알고리즘이란 무엇이며, 왜 사용하나요?
3. SimpleVectorStore의 장단점은?
4. Document 클래스의 구조는?
5. 벡터 저장소를 선택할 때 고려사항은?

---

**다음 장**: [6.2: Spring AI VectorStore 추상화 (In-Memory)](../README.md#62-spring-ai-vectordb-추상화-in-memory)

