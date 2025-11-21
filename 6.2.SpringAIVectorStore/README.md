# 6.2: Spring AI VectorStore 추상화 (In-Memory)

## 📖 학습 목표

이 강의를 마친 후 다음을 달성할 수 있습니다:
- **VectorStore 인터페이스**의 구조와 메서드를 이해할 수 있습니다
- **SimpleVectorStore (In-Memory)**를 설정하고 사용할 수 있습니다
- **Document 클래스**를 사용하여 문서를 생성하고 저장할 수 있습니다
- **add() 메서드**를 사용하여 문서를 벡터 저장소에 추가할 수 있습니다
- **similaritySearch() 메서드**를 사용하여 유사도 기반 검색을 수행할 수 있습니다
- **메타데이터**를 활용하여 문서를 관리할 수 있습니다
- **실전 예제**를 통해 VectorStore를 완전히 활용할 수 있습니다

---

## 🔑 핵심 키워드

이 장에서 다루는 핵심 키워드들:

1. **VectorStore** - Spring AI의 벡터 저장소 추상화 인터페이스
2. **Document** - 벡터 저장소에 저장되는 기본 단위 (텍스트 + 메타데이터)
3. **add()** - 문서를 벡터 저장소에 추가하는 메서드
4. **similaritySearch()** - 유사도 기반 검색을 수행하는 메서드
5. **SimpleVectorStore** - 메모리 기반 간단한 VectorStore 구현체

---

## 1. VectorStore 인터페이스 이해하기

### 1.1 VectorStore의 역할

**VectorStore**는 Spring AI에서 제공하는 벡터 저장소 추상화 인터페이스로, 다양한 벡터 저장소 구현체를 동일한 방식으로 사용할 수 있게 해줍니다.

#### 추상화의 장점

```
VectorStore 인터페이스
    ↓
여러 구현체
- SimpleVectorStore (In-Memory)
- PGVectorStore (PostgreSQL)
- PineconeVectorStore (클라우드)
- ChromaVectorStore (ChromaDB)
- WeaviateVectorStore (Weaviate)

→ 코드 변경 없이 구현체만 교체 가능
```

### 1.2 VectorStore 인터페이스 구조

```kotlin
interface VectorStore {
    /**
     * 문서를 벡터 저장소에 추가
     * @param documents 추가할 문서 리스트
     */
    fun add(documents: List<Document>)
    
    /**
     * 유사도 기반 검색
     * @param query 검색 쿼리 (문자열)
     * @return 유사한 문서 리스트 (nullable)
     */
    fun similaritySearch(query: String): List<Document>?
    
    /**
     * 고급 검색 (SearchRequest 사용)
     * @param request 검색 요청 객체
     * @return 유사한 문서 리스트 (nullable)
     */
    fun similaritySearch(request: SearchRequest): List<Document>?
    
    /**
     * 문서 삭제
     * @param ids 삭제할 문서 ID 리스트
     */
    fun delete(ids: List<String>)
}
```

---

## 2. SimpleVectorStore 설정

### 2.1 SimpleVectorStore란?

**SimpleVectorStore**는 Spring AI에서 제공하는 메모리 기반 벡터 저장소입니다.

#### 특징

- **메모리 기반**: 모든 데이터를 메모리에 저장
- **빠른 시작**: 별도 인프라 설정 불필요
- **개발/테스트용**: 로컬 개발 및 테스트에 적합
- **제한사항**: 서버 재시작 시 데이터 손실

### 2.2 SimpleVectorStore Bean 생성

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

#### 단계별 설명

1. **@Configuration**: Spring 설정 클래스로 등록
2. **EmbeddingModel 주입**: 벡터 생성을 위해 필요
3. **SimpleVectorStore.builder()**: 빌더 패턴으로 생성
4. **@Bean**: Spring 컨테이너에 VectorStore Bean으로 등록

---

## 3. Document 클래스 상세

### 3.1 Document 구조

**Document**는 벡터 저장소에 저장되는 기본 단위입니다.

```kotlin
// Spring AI 1.0.0-M6의 Document 생성자
Document(
    text: String,                  // 문서 내용 (필수)
    metadata: Map<String, Any> = emptyMap()  // 메타데이터 (선택)
)

// 또는 ID 포함
Document(
    id: String,                    // 문서 ID (필수)
    text: String,                  // 문서 내용 (필수)
    metadata: Map<String, Any> = emptyMap()  // 메타데이터 (선택)
)
```

### 3.2 Document 생성 예제

```kotlin
// 1. 기본 문서 (ID 자동 생성)
val doc1 = Document("Spring AI는 프레임워크입니다.")

// 2. 메타데이터 포함
val doc2 = Document(
    "Kotlin 프로그래밍 언어입니다.",
    mapOf(
        "category" to "programming",
        "author" to "JetBrains",
        "year" to 2011
    )
)

// 3. ID 명시
val doc3 = Document(
    "doc-001",
    "Spring Boot는 자바 프레임워크입니다.",
    mapOf("category" to "framework")
)

// 4. 복잡한 메타데이터
val doc4 = Document(
    "문서 내용",
    mapOf(
        "category" to "tutorial",
        "tags" to listOf("spring", "ai", "kotlin"),
        "difficulty" to "beginner",
        "createdAt" to System.currentTimeMillis()
    )
)
```

### 3.3 Document 속성 접근

```kotlin
val document = Document("텍스트 내용", mapOf("key" to "value"))

// 속성 접근
document.text        // "텍스트 내용"
document.metadata    // Map<String, Any>
document.id          // String? (nullable)

// 주의: content가 아닌 text 사용
// document.content  // ❌ 오류
```

---

## 4. add() 메서드 - 문서 추가

### 4.1 기본 사용법

```kotlin
@RestController
class DocumentController(
    private val vectorStore: VectorStore
) {
    @PostMapping("/documents")
    fun addDocument(@RequestBody request: AddDocumentRequest): Map<String, Any> {
        val document = Document(
            request.text,
            request.metadata ?: emptyMap()
        )
        
        vectorStore.add(listOf(document))
        
        return mapOf(
            "status" to "success",
            "message" to "문서가 추가되었습니다.",
            "documentId" to (document.id ?: "auto-generated")
        )
    }
}
```

### 4.2 배치 문서 추가

```kotlin
@PostMapping("/documents/batch")
fun addDocuments(@RequestBody request: BatchAddRequest): Map<String, Any> {
    val documents = request.texts.mapIndexed { index, text ->
        Document(
            text,
            mapOf(
                "index" to index,
                "source" to (request.source ?: "unknown"),
                "addedAt" to System.currentTimeMillis()
            )
        )
    }
    
    vectorStore.add(documents)
    
    return mapOf(
        "status" to "success",
        "count" to documents.size,
        "message" to "${documents.size}개의 문서가 추가되었습니다."
    )
}
```

### 4.3 문서 추가 흐름

```
1. Document 생성
   - 텍스트 + 메타데이터
   ↓
2. Embedding 생성
   - EmbeddingModel이 텍스트를 벡터로 변환
   ↓
3. VectorStore에 저장
   - 벡터와 메타데이터를 함께 저장
   ↓
4. 검색 가능 상태
   - similaritySearch()로 검색 가능
```

---

## 5. similaritySearch() 메서드 - 유사도 검색

### 5.1 기본 사용법

```kotlin
@GetMapping("/search")
fun search(
    @RequestParam query: String,
    @RequestParam(defaultValue = "5") topK: Int
): Map<String, Any> {
    // 검색 수행
    val documents = vectorStore.similaritySearch(query) ?: emptyList()
    
    // topK 제한
    val limitedResults = documents.take(topK)
    
    return mapOf(
        "query" to query,
        "topK" to topK,
        "resultCount" to limitedResults.size,
        "results" to limitedResults.mapIndexed { index, doc ->
            mapOf(
                "rank" to (index + 1),
                "content" to doc.text,
                "metadata" to doc.metadata,
                "id" to (doc.id ?: "unknown")
            )
        }
    )
}
```

### 5.2 검색 흐름

```
1. 쿼리 텍스트
   ↓
2. 쿼리 임베딩 생성
   - EmbeddingModel이 쿼리를 벡터로 변환
   ↓
3. 벡터 유사도 계산
   - 저장된 모든 문서 벡터와 비교
   ↓
4. 유사도 순 정렬
   - 코사인 유사도 높은 순으로 정렬
   ↓
5. 상위 K개 반환
   - take(topK)로 제한
```

### 5.3 검색 결과 활용

```kotlin
fun searchAndProcess(query: String, topK: Int = 5): List<String> {
    val results = vectorStore.similaritySearch(query) ?: emptyList()
    
    return results.take(topK).map { doc ->
        // 문서 내용 활용
        doc.text
    }
}
```

---

## 6. 실전 활용 예제

### 6.1 문서 관리 시스템

```kotlin
@Service
class DocumentService(
    private val vectorStore: VectorStore
) {
    /**
     * 문서 추가
     */
    fun addDocument(text: String, category: String): String {
        val document = Document(
            text,
            mapOf(
                "category" to category,
                "createdAt" to System.currentTimeMillis()
            )
        )
        
        vectorStore.add(listOf(document))
        return document.id ?: "unknown"
    }
    
    /**
     * 카테고리별 문서 검색
     */
    fun searchByCategory(query: String, category: String, topK: Int = 5): List<Document> {
        val results = vectorStore.similaritySearch(query) ?: emptyList()
        
        // 카테고리 필터링
        val filtered = results.filter { doc ->
            doc.metadata["category"] == category
        }
        
        return filtered.take(topK)
    }
    
    /**
     * 문서 검색
     */
    fun search(query: String, topK: Int = 5): List<Document> {
        val results = vectorStore.similaritySearch(query) ?: emptyList()
        return results.take(topK)
    }
}
```

### 6.2 지식베이스 시스템

```kotlin
@Service
class KnowledgeBaseService(
    private val vectorStore: VectorStore
) {
    /**
     * 지식 추가
     */
    fun addKnowledge(title: String, content: String, topic: String) {
        val document = Document(
            "$title\n\n$content",
            mapOf(
                "title" to title,
                "topic" to topic,
                "type" to "knowledge"
            )
        )
        
        vectorStore.add(listOf(document))
    }
    
    /**
     * 주제별 지식 검색
     */
    fun searchKnowledge(query: String, topic: String? = null, topK: Int = 3): List<Document> {
        val results = vectorStore.similaritySearch(query) ?: emptyList()
        
        val filtered = if (topic != null) {
            results.filter { doc ->
                doc.metadata["topic"] == topic && doc.metadata["type"] == "knowledge"
            }
        } else {
            results.filter { doc ->
                doc.metadata["type"] == "knowledge"
            }
        }
        
        return filtered.take(topK)
    }
}
```

### 6.3 FAQ 시스템

```kotlin
@Service
class FAQService(
    private val vectorStore: VectorStore
) {
    /**
     * FAQ 추가
     */
    fun addFAQ(question: String, answer: String, category: String) {
        val document = Document(
            "Q: $question\nA: $answer",
            mapOf(
                "question" to question,
                "answer" to answer,
                "category" to category,
                "type" to "faq"
            )
        )
        
        vectorStore.add(listOf(document))
    }
    
    /**
     * FAQ 검색
     */
    fun searchFAQ(query: String, topK: Int = 3): List<FAQResult> {
        val results = vectorStore.similaritySearch(query) ?: emptyList()
        
        return results
            .filter { it.metadata["type"] == "faq" }
            .take(topK)
            .map { doc ->
                FAQResult(
                    question = doc.metadata["question"] as? String ?: "",
                    answer = doc.metadata["answer"] as? String ?: "",
                    category = doc.metadata["category"] as? String ?: ""
                )
            }
    }
}

data class FAQResult(
    val question: String,
    val answer: String,
    val category: String
)
```

---

## 7. 메타데이터 활용

### 7.1 메타데이터의 역할

**메타데이터**는 문서와 함께 저장되는 추가 정보로, 검색 후 필터링이나 그룹화에 활용할 수 있습니다.

#### 활용 사례

- **카테고리 필터링**: 특정 카테고리의 문서만 검색
- **날짜 필터링**: 특정 기간의 문서만 검색
- **소스 추적**: 문서의 출처 기록
- **버전 관리**: 문서 버전 정보

### 7.2 메타데이터 필터링 예제

```kotlin
fun searchWithMetadata(
    query: String,
    category: String? = null,
    dateFrom: Long? = null,
    topK: Int = 5
): List<Document> {
    val results = vectorStore.similaritySearch(query) ?: emptyList()
    
    val filtered = results.filter { doc ->
        val metadata = doc.metadata
        
        // 카테고리 필터
        val categoryMatch = category == null || metadata["category"] == category
        
        // 날짜 필터
        val dateMatch = dateFrom == null || 
            (metadata["createdAt"] as? Long ?: 0L) >= dateFrom
        
        categoryMatch && dateMatch
    }
    
    return filtered.take(topK)
}
```

### 7.3 메타데이터 기반 통계

```kotlin
fun getStatistics(): Map<String, Any> {
    // 모든 문서 검색 (매우 큰 topK)
    val allDocs = vectorStore.similaritySearch("test") ?: emptyList()
    
    // 카테고리별 통계
    val categoryStats = allDocs
        .mapNotNull { it.metadata["category"] as? String }
        .groupingBy { it }
        .eachCount()
    
    // 타입별 통계
    val typeStats = allDocs
        .mapNotNull { it.metadata["type"] as? String }
        .groupingBy { it }
        .eachCount()
    
    return mapOf(
        "totalDocuments" to allDocs.size,
        "categoryStats" to categoryStats,
        "typeStats" to typeStats
    )
}
```

---

## 8. 실전 패턴 및 베스트 프랙티스

### 8.1 문서 추가 패턴

#### ✅ 좋은 예

```kotlin
// 1. 메타데이터 포함
val document = Document(
    text,
    mapOf(
        "source" to "user-input",
        "timestamp" to System.currentTimeMillis(),
        "version" to "1.0"
    )
)

// 2. 배치 처리
val documents = texts.map { text ->
    Document(text, metadata)
}
vectorStore.add(documents)  // 한 번에 추가
```

#### ❌ 나쁜 예

```kotlin
// 1. 메타데이터 없음
val document = Document(text)  // 나중에 추적 불가

// 2. 개별 추가
texts.forEach { text ->
    vectorStore.add(listOf(Document(text)))  // 비효율적
}
```

### 8.2 검색 패턴

#### ✅ 좋은 예

```kotlin
// topK 제한
val results = vectorStore.similaritySearch(query) ?: emptyList()
val limited = results.take(topK)

// null 안전 처리
val documents = results ?: emptyList()
```

#### ❌ 나쁜 예

```kotlin
// null 체크 없음
val results = vectorStore.similaritySearch(query)  // NPE 가능

// topK 제한 없음
return results  // 너무 많은 결과 반환
```

### 8.3 메타데이터 관리

#### ✅ 좋은 예

```kotlin
// 일관된 메타데이터 구조
val metadata = mapOf(
    "category" to category,
    "createdAt" to System.currentTimeMillis(),
    "source" to source,
    "version" to "1.0"
)
```

#### ❌ 나쁜 예

```kotlin
// 불일치한 키 이름
doc1.metadata["category"] = "tech"
doc2.metadata["Category"] = "tech"  // 대소문자 불일치
doc3.metadata["cat"] = "tech"  // 다른 키 이름
```

---

## 9. 성능 최적화

### 9.1 배치 처리

```kotlin
// ✅ 좋은 예: 배치 처리
fun addDocumentsBatch(texts: List<String>) {
    val documents = texts.map { text ->
        Document(text, metadata)
    }
    vectorStore.add(documents)  // 한 번에 처리
}

// ❌ 나쁜 예: 개별 처리
fun addDocumentsIndividually(texts: List<String>) {
    texts.forEach { text ->
        vectorStore.add(listOf(Document(text)))  // 여러 번 호출
    }
}
```

### 9.2 검색 최적화

```kotlin
// 적절한 topK 값 사용
fun search(query: String, topK: Int = 5): List<Document> {
    // topK는 필요에 따라 조정
    // 너무 크면: 불필요한 데이터 처리
    // 너무 작으면: 관련 문서 누락
    val results = vectorStore.similaritySearch(query) ?: emptyList()
    return results.take(topK)
}
```

---

## 10. 주의사항 및 트러블슈팅

### 10.1 일반적인 문제들

#### 문제 1: SimpleVectorStore 데이터 손실

**증상:**
```
서버 재시작 후 추가했던 문서가 사라짐
```

**원인**: SimpleVectorStore는 메모리 기반

**해결책:**
- 개발 환경: 정기적으로 데이터 백업
- 프로덕션: PGVector 등 영구 저장소 사용

#### 문제 2: 검색 결과가 비어있음

**증상:**
```
검색 쿼리를 보냈지만 결과가 없음
```

**해결책:**
1. 문서가 추가되었는지 확인
2. 쿼리 텍스트 확인
3. 임베딩 모델 확인
4. 로그 확인

```kotlin
// 디버깅 코드
fun debugSearch(query: String) {
    val results = vectorStore.similaritySearch(query)
    println("Query: $query")
    println("Results count: ${results?.size ?: 0}")
    results?.forEachIndexed { index, doc ->
        println("Result $index: ${doc.text}")
    }
}
```

#### 문제 3: 메타데이터 접근 오류

**증상:**
```
ClassCastException: String cannot be cast to Integer
```

**원인**: 메타데이터 타입 불일치

**해결책:**
```kotlin
// 안전한 메타데이터 접근
val category = doc.metadata["category"] as? String ?: "unknown"
val year = (doc.metadata["year"] as? Number)?.toInt() ?: 0
```

### 10.2 성능 문제

#### 메모리 부족

**증상:**
```
OutOfMemoryError: Java heap space
```

**해결책:**
- SimpleVectorStore 대신 외부 벡터 저장소 사용
- 문서 수 제한
- JVM 힙 크기 증가

#### 검색 속도 저하

**증상:**
```
검색이 느림
```

**해결책:**
- topK 값 조정
- 문서 수 최적화
- 외부 벡터 저장소 사용 (HNSW 인덱싱)

---

## 11. 요약

### 11.1 핵심 내용 정리

1. **VectorStore 인터페이스**: 벡터 저장소 추상화
2. **SimpleVectorStore**: 메모리 기반 간단한 구현체
3. **Document 클래스**: 텍스트 + 메타데이터 구조
4. **add() 메서드**: 문서를 벡터 저장소에 추가
5. **similaritySearch() 메서드**: 유사도 기반 검색
6. **메타데이터 활용**: 필터링 및 통계

### 11.2 기본 패턴

```kotlin
// 1. VectorStore 설정
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
val limitedResults = results.take(5)
```

### 11.3 실전 활용 패턴

```kotlin
// 문서 관리 서비스
@Service
class DocumentService(
    private val vectorStore: VectorStore
) {
    fun add(text: String, metadata: Map<String, Any>) {
        vectorStore.add(listOf(Document(text, metadata)))
    }
    
    fun search(query: String, topK: Int = 5): List<Document> {
        return vectorStore.similaritySearch(query)?.take(topK) ?: emptyList()
    }
    
    fun searchWithFilter(query: String, filter: (Document) -> Boolean): List<Document> {
        val results = vectorStore.similaritySearch(query) ?: emptyList()
        return results.filter(filter)
    }
}
```

### 11.4 다음 학습 내용

이제 SimpleVectorStore를 완전히 이해했으니, 다음 장에서는:
- **외부 벡터 저장소**: PGVector 등 영구 저장소 연동
- **RAG 구현**: VectorStore를 활용한 검색 기반 생성
- **문서 로딩**: PDF, TXT 등 외부 문서 로드

---

## 📚 참고 자료

- [Spring AI VectorStore 공식 문서](https://docs.spring.io/spring-ai/reference/api/vectordb.html)
- [Document API 문서](https://docs.spring.io/spring-ai/reference/api/document.html)
- [SimpleVectorStore 소스 코드](https://github.com/spring-projects/spring-ai)

---

## ❓ 학습 확인 문제

다음 질문에 답할 수 있는지 확인해보세요:

1. VectorStore 인터페이스의 주요 메서드는 무엇인가요?
2. SimpleVectorStore를 Bean으로 등록하는 방법은?
3. Document 클래스를 생성하는 방법은?
4. add() 메서드를 사용하여 문서를 추가하는 방법은?
5. similaritySearch()를 사용하여 검색하는 방법은?
6. 메타데이터를 활용하여 필터링하는 방법은?

---

**다음 장**: [6.3: 외부 벡터 저장소 연동 (PostgreSQL/PGVector)](../README.md#63-외부-벡터-저장소-연동-postgresqlpgvector)

