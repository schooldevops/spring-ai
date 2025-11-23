# 23. EmbeddingModel API

## 📖 학습 목표

이 강의를 마친 후 다음을 달성할 수 있습니다:
- **EmbeddingModel**의 개념과 사용법을 이해할 수 있습니다
- **벡터 임베딩**을 생성하고 활용할 수 있습니다
- **배치 처리**를 통해 여러 텍스트를 효율적으로 임베딩할 수 있습니다
- **코사인 유사도**를 계산하여 시맨틱 검색을 구현할 수 있습니다

---

## 🔑 핵심 키워드

1. **EmbeddingModel** - 텍스트를 벡터로 변환하는 AI 모델 인터페이스
2. **Vector Embedding** - 텍스트의 의미를 숫자 배열로 표현
3. **Cosine Similarity** - 벡터 간 유사도 측정 방법
4. **Semantic Search** - 의미 기반 검색
5. **Batch Processing** - 여러 텍스트를 한 번에 임베딩

---

## 1. EmbeddingModel이란?

### 1.1 임베딩의 개념

**임베딩(Embedding)**은 텍스트, 이미지, 비디오를 숫자 벡터로 변환하는 기술입니다. 이 벡터는 입력 데이터의 의미를 수치적으로 표현합니다.

```kotlin
// 텍스트
"Spring AI is awesome"

// 임베딩 벡터 (예시)
[0.123, -0.456, 0.789, ..., 0.321]  // 1536 dimensions
```

### 1.2 임베딩의 활용

- **시맨틱 검색**: 의미가 유사한 문서 찾기
- **추천 시스템**: 유사한 콘텐츠 추천
- **분류**: 텍스트 카테고리 분류
- **클러스터링**: 유사한 문서 그룹화

---

## 2. EmbeddingModel 인터페이스

```kotlin
interface EmbeddingModel {
    // 단일 텍스트 임베딩
    fun embed(text: String): FloatArray
    
    // Document 임베딩
    fun embed(document: Document): FloatArray
    
    // 배치 임베딩
    fun embed(texts: List<String>): List<FloatArray>
    
    // EmbeddingResponse 반환
    fun embedForResponse(texts: List<String>): EmbeddingResponse
    
    // 벡터 차원 수
    fun dimensions(): Int
}
```

---

## 3. 샘플 구성

### Sample 01: Basic Embedding
**학습 내용:**
- EmbeddingModel 기본 사용법
- `embed(String)` 메서드
- `embed(Document)` 메서드
- 벡터 차원 확인

**디렉토리:** [sample01-basic-embedding](./sample01-basic-embedding/)

---

### Sample 02: Batch Embedding and Response
**학습 내용:**
- `embed(List<String>)` 배치 처리
- `embedForResponse()` 메서드
- EmbeddingRequest와 EmbeddingResponse
- 메타데이터 접근

**디렉토리:** [sample02-batch-embedding](./sample02-batch-embedding/)

---

### Sample 03: Similarity Calculation
**학습 내용:**
- 코사인 유사도 계산
- 벡터 비교
- 시맨틱 검색 구현
- 실용적인 활용 예제

**디렉토리:** [sample03-similarity-calculation](./sample03-similarity-calculation/)

---

## 4. 코사인 유사도

두 벡터 간의 유사도를 측정하는 방법:

```kotlin
fun cosineSimilarity(vec1: FloatArray, vec2: FloatArray): Double {
    val dotProduct = vec1.zip(vec2).sumOf { (a, b) -> (a * b).toDouble() }
    val magnitude1 = sqrt(vec1.sumOf { (it * it).toDouble() })
    val magnitude2 = sqrt(vec2.sumOf { (it * it).toDouble() })
    return dotProduct / (magnitude1 * magnitude2)
}
```

**값의 의미:**
- `1.0`: 완전히 동일
- `0.0`: 관련 없음
- `-1.0`: 완전히 반대

---

## 5. 공통 설정

### 필수 의존성

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
```

### application.yml

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      embedding:
        options:
          model: text-embedding-3-small
```

---

## 6. 실용적인 활용 예제

### 시맨틱 검색

```kotlin
// 1. 문서들을 임베딩
val documents = listOf(
    "Spring AI provides AI integration",
    "Kotlin is a modern programming language",
    "Machine learning is fascinating"
)
val embeddings = embeddingModel.embed(documents)

// 2. 쿼리 임베딩
val query = "Tell me about Spring AI"
val queryEmbedding = embeddingModel.embed(query)

// 3. 유사도 계산 및 정렬
val results = embeddings.mapIndexed { index, embedding ->
    Pair(documents[index], cosineSimilarity(queryEmbedding, embedding))
}.sortedByDescending { it.second }

// 4. 가장 유사한 문서 반환
println(results.first())  // "Spring AI provides AI integration"
```

---

## 7. 벡터 차원

OpenAI 임베딩 모델별 차원:
- **text-embedding-3-small**: 1536 dimensions
- **text-embedding-3-large**: 3072 dimensions
- **text-embedding-ada-002**: 1536 dimensions

```kotlin
val dimensions = embeddingModel.dimensions()
println("Vector dimensions: $dimensions")
```

---

## 8. 베스트 프랙티스

### ✅ 권장사항

1. **배치 처리 사용**
```kotlin
// ✅ 효율적
val embeddings = embeddingModel.embed(listOf("text1", "text2", "text3"))

// ❌ 비효율적
val embedding1 = embeddingModel.embed("text1")
val embedding2 = embeddingModel.embed("text2")
val embedding3 = embeddingModel.embed("text3")
```

2. **임베딩 캐싱**
```kotlin
// ✅ 동일한 텍스트는 캐시
val cache = mutableMapOf<String, FloatArray>()
fun getEmbedding(text: String): FloatArray {
    return cache.getOrPut(text) { embeddingModel.embed(text) }
}
```

3. **적절한 모델 선택**
```kotlin
// 일반적인 용도: text-embedding-3-small (빠르고 저렴)
// 고품질 필요: text-embedding-3-large (느리지만 정확)
```

---

## 9. 활용 사례

### 문서 검색
```kotlin
// 문서 DB를 임베딩으로 인덱싱
// 쿼리를 임베딩으로 변환
// 코사인 유사도로 관련 문서 찾기
```

### 추천 시스템
```kotlin
// 사용자 선호도를 임베딩
// 아이템을 임베딩
// 유사도 기반 추천
```

### 중복 탐지
```kotlin
// 문서들을 임베딩
// 유사도가 높은 쌍 찾기
// 중복 제거
```

---

## 10. 참고 자료

- [Spring AI Embedding 공식 문서](https://docs.spring.io/spring-ai/reference/api/embeddings.html)
- [OpenAI Embeddings Guide](https://platform.openai.com/docs/guides/embeddings)
- [Vector Similarity](https://en.wikipedia.org/wiki/Cosine_similarity)

---

**시작하기**: [Sample 01: Basic Embedding](./sample01-basic-embedding/)
