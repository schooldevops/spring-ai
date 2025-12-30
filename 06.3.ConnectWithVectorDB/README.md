# 6.3: 외부 벡터 저장소 연동 (PostgreSQL/PGVector)

## 📖 학습 목표

이 강의를 마친 후 다음을 달성할 수 있습니다:
- **PGVector**의 개념과 필요성을 이해할 수 있습니다
- **Docker**를 사용하여 PostgreSQL/PGVector 환경을 구축할 수 있습니다
- **spring-ai-pgvector-starter**를 사용하여 Spring AI와 PGVector를 연동할 수 있습니다
- **PGVectorStore**를 설정하고 사용할 수 있습니다
- **SimpleVectorStore와 PGVectorStore의 차이**를 이해하고 선택할 수 있습니다
- **프로덕션 환경**에서 벡터 저장소를 활용할 수 있습니다

---

## 🔑 핵심 키워드

이 장에서 다루는 핵심 키워드들:

1. **PGVector** - PostgreSQL의 벡터 확장 기능
2. **Docker** - 컨테이너 기반 PostgreSQL/PGVector 실행 환경
3. **spring-ai-pgvector-starter** - Spring AI의 PGVector 연동 스타터
4. **PGVectorStore** - Spring AI의 PGVector 구현체
5. **벡터 인덱싱** - HNSW, IVFFlat 등 벡터 검색 최적화

---

## 1. PGVector란?

### 1.1 PGVector의 개념

**PGVector**는 PostgreSQL 데이터베이스에 벡터 검색 기능을 추가하는 오픈소스 확장(Extension)입니다.

#### 특징

- **PostgreSQL 확장**: 기존 PostgreSQL 인프라 활용
- **벡터 타입**: `vector` 타입으로 고차원 벡터 저장
- **유사도 검색**: 코사인 유사도, L2 거리 등 지원
- **인덱싱**: HNSW, IVFFlat 인덱스로 빠른 검색
- **메타데이터 필터링**: SQL WHERE 절로 필터링 가능

### 1.2 PGVector의 장점

#### SimpleVectorStore vs PGVectorStore

| 특징 | SimpleVectorStore | PGVectorStore |
|------|------------------|---------------|
| **데이터 영구성** | ❌ 메모리 기반 (재시작 시 손실) | ✅ 디스크 저장 (영구 보존) |
| **확장성** | ❌ 제한적 | ✅ 대규모 데이터 지원 |
| **메타데이터 필터링** | ⚠️ 클라이언트 측 필터링 | ✅ SQL WHERE 절 필터링 |
| **설정 복잡도** | ✅ 매우 간단 | ⚠️ PostgreSQL 필요 |
| **성능** | ✅ 작은 데이터에 빠름 | ✅ 대규모 데이터에 최적화 |
| **프로덕션 사용** | ❌ 개발/테스트용 | ✅ 프로덕션 적합 |

### 1.3 언제 PGVector를 사용해야 할까?

#### ✅ PGVector를 사용해야 하는 경우

- **프로덕션 환경**: 데이터 영구 보존 필요
- **대규모 데이터**: 수만 개 이상의 문서
- **메타데이터 필터링**: 복잡한 필터링 요구사항
- **기존 PostgreSQL 인프라**: 이미 PostgreSQL 사용 중
- **다중 인스턴스**: 여러 서버에서 공유 데이터 필요

#### ❌ SimpleVectorStore를 사용해야 하는 경우

- **개발/테스트**: 빠른 프로토타이핑
- **소규모 데이터**: 수백 개 이하의 문서
- **단순한 요구사항**: 복잡한 설정 불필요
- **임시 데이터**: 영구 보존 불필요

---

## 2. Docker를 사용한 PostgreSQL/PGVector 설정

### 2.1 Docker Compose 설정

PostgreSQL과 PGVector를 Docker로 실행하기 위한 `docker-compose.yml` 파일:

```yaml
version: '3.8'

services:
  postgres:
    image: pgvector/pgvector:pg16  # PostgreSQL 16 버전 사용 (안정적)
    container_name: spring-ai-postgres
    environment:
      POSTGRES_USER: springai
      POSTGRES_PASSWORD: springai123
      POSTGRES_DB: vectordb
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init-scripts:/docker-entrypoint-initdb.d  # PGVector 확장 자동 활성화
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U springai"]
      interval: 10s
      timeout: 5s
      retries: 10
    networks:
      - spring-ai-network

volumes:
  postgres_data:

networks:
  spring-ai-network:
    driver: bridge
```

**중요**: `init-scripts` 디렉토리를 볼륨으로 마운트하면 PostgreSQL 컨테이너가 처음 시작될 때 자동으로 SQL 스크립트가 실행되어 PGVector 확장이 활성화됩니다.

### 2.2 Docker 실행

```bash
# Docker Compose로 PostgreSQL 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f postgres

# 컨테이너 상태 확인
docker-compose ps

# PostgreSQL 접속 테스트
docker exec -it spring-ai-postgres psql -U springai -d vectordb
```

### 2.3 PGVector 확장 자동 활성화

Docker Compose를 사용하면 `init-scripts` 디렉토리의 SQL 파일이 자동으로 실행됩니다.

#### init-scripts/01-init-pgvector.sql 파일 생성

```sql
-- PGVector 확장 활성화
CREATE EXTENSION IF NOT EXISTS vector;
```

#### docker-compose.yml에 볼륨 마운트 추가

```yaml
volumes:
  - postgres_data:/var/lib/postgresql/data
  - ./init-scripts:/docker-entrypoint-initdb.d  # 자동 초기화
```

이렇게 설정하면 PostgreSQL 컨테이너가 처음 시작될 때 자동으로 PGVector 확장이 활성화됩니다.

#### 수동으로 확장 활성화 (선택사항)

PostgreSQL에 접속하여 수동으로 확장을 활성화할 수도 있습니다:

```sql
-- PGVector 확장 활성화
CREATE EXTENSION IF NOT EXISTS vector;

-- 확장 확인
SELECT * FROM pg_extension WHERE extname = 'vector';

-- 벡터 타입 테스트
SELECT '[1,2,3]'::vector;
```

---

## 3. Spring AI PGVector 연동

### 3.1 의존성 추가

`build.gradle.kts`에 PGVector 스타터 추가:

```kotlin
dependencies {
    // Spring Boot Web
    implementation("org.springframework.boot:spring-boot-starter-web")
    
    // Spring AI OpenAI
    implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter:1.0.0-M6")
    
    // Spring AI PGVector
    implementation("org.springframework.ai:spring-ai-pgvector-store:1.0.0-M6")
    
    // PostgreSQL Driver
    implementation("org.postgresql:postgresql")
    
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    
    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
}
```

### 3.2 application.yml 설정

```yaml
spring:
  application:
    name: pgvector-demo
  
  # PostgreSQL 설정
  datasource:
    url: jdbc:postgresql://localhost:5432/vectordb
    username: springai
    password: springai123
    driver-class-name: org.postgresql.Driver
  
  # JPA 설정 (선택사항)
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  
  # OpenAI 설정
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:your-api-key-here}
      chat:
        options:
          model: gpt-4
          temperature: 0.7
      embedding:
        options:
          model: text-embedding-ada-002

# PGVector 설정
spring:
  ai:
    vectorstore:
      pgvector:
        # 인덱스 타입 (HNSW 또는 IVFFlat)
        index-type: HNSW
        # HNSW 파라미터
        distance-type: COSINE_DISTANCE
        # 벡터 차원 (OpenAI text-embedding-ada-002는 1536)
        dimensions: 1536
        # 인덱스 생성 여부
        initialize-schema: true

server:
  port: 8080
```

### 3.3 PGVectorStore Bean 설정

```kotlin
@Configuration
class PGVectorStoreConfig(
    private val embeddingModel: EmbeddingModel,
    private val dataSource: DataSource
) {
    @Bean
    fun jdbcTemplate(): JdbcTemplate {
        return JdbcTemplate(dataSource)
    }
    
    @Bean
    fun pgVectorStore(jdbcTemplate: JdbcTemplate): VectorStore {
        val pgVectorStore = PgVectorStore.builder(
            jdbcTemplate,
            embeddingModel
        )
            .dimensions(1536)  // OpenAI text-embedding-ada-002
            .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
            .indexType(PgVectorStore.PgIndexType.HNSW)
            .removeExistingVectorStoreTable(false)
            .initializeSchema(true)
            .build()
        
        return pgVectorStore
    }
}
```

**중요**: PgVectorStore.Builder는 `JdbcTemplate`과 `EmbeddingModel`을 받습니다. `DataSource`가 아닙니다.

---

## 4. PGVectorStore 사용법

### 4.1 기본 사용 패턴

PGVectorStore는 VectorStore 인터페이스를 구현하므로, SimpleVectorStore와 동일한 방식으로 사용할 수 있습니다:

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
            "documentId" to (document.id ?: "unknown")
        )
    }
    
    @PostMapping("/search")
    fun search(@RequestBody request: SearchRequest): Map<String, Any> {
        val documents = vectorStore.similaritySearch(request.query) ?: emptyList()
        val limitedResults = documents.take(request.topK)
        
        return mapOf(
            "query" to request.query,
            "topK" to request.topK,
            "resultCount" to limitedResults.size,
            "results" to limitedResults.mapIndexed { index, doc ->
                mapOf(
                    "rank" to (index + 1),
                    "content" to doc.text,
                    "metadata" to doc.metadata
                )
            }
        )
    }
}
```

### 4.2 메타데이터 필터링 (고급)

PGVectorStore는 SQL WHERE 절을 사용한 메타데이터 필터링을 지원합니다:

```kotlin
@Service
class DocumentService(
    private val vectorStore: VectorStore
) {
    fun searchWithMetadataFilter(
        query: String,
        category: String? = null,
        topK: Int = 5
    ): List<Document> {
        // SearchRequest를 사용한 고급 검색
        val searchRequest = SearchRequest.builder()
            .withQuery(query)
            .withTopK(topK)
            .withSimilarityThreshold(0.0)
            .build()
        
        // 메타데이터 필터링은 SearchRequest의 필터 옵션 사용
        // (Spring AI 1.0.0-M6에서는 클라이언트 측 필터링 필요)
        val results = vectorStore.similaritySearch(searchRequest) ?: emptyList()
        
        // 클라이언트 측 필터링
        val filtered = if (category != null) {
            results.filter { doc ->
                doc.metadata["category"] == category
            }
        } else {
            results
        }
        
        return filtered.take(topK)
    }
}
```

---

## 5. 벡터 인덱싱

### 5.1 인덱스 타입

PGVector는 두 가지 인덱스 타입을 지원합니다:

#### HNSW (Hierarchical Navigable Small World)

- **특징**: 매우 빠른 검색 속도
- **용도**: 대규모 데이터, 빠른 검색이 중요한 경우
- **메모리**: 상대적으로 많은 메모리 사용

```kotlin
PGVectorStore.Builder(embeddingModel, dataSource)
    .withIndexType(PGVectorStore.PGVectorIndexType.HNSW)
    .build()
```

#### IVFFlat (Inverted File with Flat Compression)

- **특징**: 메모리 효율적
- **용도**: 중소규모 데이터, 메모리가 제한적인 경우
- **성능**: HNSW보다 느리지만 메모리 효율적

```kotlin
PGVectorStore.Builder(embeddingModel, dataSource)
    .withIndexType(PGVectorStore.PGVectorIndexType.IVFFLAT)
    .build()
```

### 5.2 거리 함수

PGVector는 여러 거리 함수를 지원합니다:

- **COSINE_DISTANCE**: 코사인 거리 (0~2, 0이 가장 유사)
- **L2_DISTANCE**: 유클리드 거리
- **INNER_PRODUCT**: 내적

```kotlin
PGVectorStore.Builder(embeddingModel, dataSource)
    .withDistanceType(PGVectorStore.PGVectorDistanceType.COSINE_DISTANCE)
    .build()
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
    fun addDocument(text: String, category: String, tags: List<String>): String {
        val document = Document(
            text,
            mapOf(
                "category" to category,
                "tags" to tags.joinToString(","),
                "createdAt" to System.currentTimeMillis()
            )
        )
        
        vectorStore.add(listOf(document))
        return document.id ?: "unknown"
    }
    
    /**
     * 카테고리별 검색
     */
    fun searchByCategory(query: String, category: String, topK: Int = 5): List<Document> {
        val results = vectorStore.similaritySearch(query) ?: emptyList()
        
        return results
            .filter { doc ->
                doc.metadata["category"] == category
            }
            .take(topK)
    }
    
    /**
     * 문서 삭제
     */
    fun deleteDocument(documentId: String) {
        vectorStore.delete(listOf(documentId))
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
                "type" to "knowledge",
                "createdAt" to System.currentTimeMillis()
            )
        )
        
        vectorStore.add(listOf(document))
    }
    
    /**
     * 주제별 지식 검색
     */
    fun searchKnowledge(query: String, topic: String? = null, topK: Int = 3): List<KnowledgeResult> {
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
        
        return filtered.take(topK).map { doc ->
            KnowledgeResult(
                title = doc.metadata["title"] as? String ?: "",
                content = doc.text ?: "",
                topic = doc.metadata["topic"] as? String ?: ""
            )
        }
    }
}
```

---

## 7. 마이그레이션: SimpleVectorStore → PGVectorStore

### 7.1 마이그레이션 전략

SimpleVectorStore에서 PGVectorStore로 마이그레이션하는 방법:

#### 1단계: PGVectorStore 설정

```kotlin
@Configuration
class VectorStoreConfig(
    private val embeddingModel: EmbeddingModel,
    private val dataSource: DataSource
) {
    @Bean
    @Primary
    fun pgVectorStore(): VectorStore {
        return PGVectorStore.Builder(embeddingModel, dataSource)
            .withIndexType(PGVectorStore.PGVectorIndexType.HNSW)
            .withDistanceType(PGVectorStore.PGVectorDistanceType.COSINE_DISTANCE)
            .withDimensions(1536)
            .build()
    }
    
    // SimpleVectorStore는 제거하거나 @Bean 제거
}
```

#### 2단계: application.yml 업데이트

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/vectordb
    username: springai
    password: springai123
  
  ai:
    vectorstore:
      pgvector:
        index-type: HNSW
        distance-type: COSINE_DISTANCE
        dimensions: 1536
        initialize-schema: true
```

#### 3단계: 코드 변경 없음

VectorStore 인터페이스를 사용하므로, 컨트롤러와 서비스 코드는 변경할 필요가 없습니다!

---

## 8. 성능 최적화

### 8.1 인덱스 최적화

```sql
-- HNSW 인덱스 파라미터 조정
CREATE INDEX ON vector_table 
USING hnsw (embedding vector_cosine_ops)
WITH (m = 16, ef_construction = 64);
```

### 8.2 연결 풀 설정

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
```

### 8.3 배치 처리

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
        vectorStore.add(listOf(Document(text)))  // 비효율적
    }
}
```

---

## 9. 주의사항 및 트러블슈팅

### 9.1 일반적인 문제들

#### 문제 1: 연결 오류

**증상:**
```
Connection refused: connect
```

**해결책:**
1. Docker 컨테이너 실행 확인: `docker-compose ps`
2. PostgreSQL 포트 확인: `netstat -an | grep 5432`
3. application.yml의 데이터소스 URL 확인

#### 문제 2: PGVector 확장 없음

**증상:**
```
ERROR: extension "vector" does not exist
```

**해결책:**
```sql
-- PostgreSQL에 접속하여 확장 활성화
CREATE EXTENSION IF NOT EXISTS vector;
```

#### 문제 3: 인덱스 생성 실패

**증상:**
```
ERROR: index creation failed
```

**해결책:**
1. 충분한 데이터가 있는지 확인 (IVFFlat은 최소 데이터 필요)
2. 인덱스 타입 변경 (HNSW 권장)
3. PostgreSQL 버전 확인 (PGVector 호환 버전)

### 9.2 성능 문제

#### 검색 속도 저하

**해결책:**
- HNSW 인덱스 사용
- 인덱스 파라미터 조정
- topK 값 최적화

#### 메모리 부족

**해결책:**
- IVFFlat 인덱스 사용
- 연결 풀 크기 조정
- 배치 크기 제한

---

## 10. 요약

### 10.1 핵심 내용 정리

1. **PGVector**: PostgreSQL의 벡터 확장 기능
2. **Docker 설정**: PostgreSQL/PGVector 컨테이너 실행
3. **spring-ai-pgvector-starter**: Spring AI와 PGVector 연동
4. **PGVectorStore**: 영구 저장소 기반 VectorStore 구현체
5. **인덱싱**: HNSW, IVFFlat 인덱스로 성능 최적화

### 10.2 기본 패턴

```kotlin
// 1. JdbcTemplate Bean 생성
@Bean
fun jdbcTemplate(): JdbcTemplate {
    return JdbcTemplate(dataSource)
}

// 2. PGVectorStore Bean 생성
@Bean
fun pgVectorStore(jdbcTemplate: JdbcTemplate): VectorStore {
    return PgVectorStore.builder(jdbcTemplate, embeddingModel)
        .dimensions(1536)  // OpenAI text-embedding-ada-002
        .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
        .indexType(PgVectorStore.PgIndexType.HNSW)
        .removeExistingVectorStoreTable(false)
        .initializeSchema(true)
        .build()
}

// 2. 문서 추가 (SimpleVectorStore와 동일)
val document = Document("문서 내용", metadata)
vectorStore.add(listOf(document))

// 3. 유사도 검색 (SimpleVectorStore와 동일)
val results = vectorStore.similaritySearch("쿼리") ?: emptyList()
val limitedResults = results.take(5)
```

### 10.3 선택 가이드

| 상황 | 추천 |
|------|------|
| 개발/테스트 | SimpleVectorStore |
| 프로덕션 (소규모) | PGVectorStore |
| 프로덕션 (대규모) | PGVectorStore (HNSW) |
| 메타데이터 필터링 중요 | PGVectorStore |
| 빠른 프로토타이핑 | SimpleVectorStore |

### 10.4 다음 학습 내용

이제 외부 벡터 저장소를 연동했으니, 다음 장에서는:
- **RAG 구현**: VectorStore를 활용한 검색 기반 생성
- **문서 로딩**: PDF, TXT 등 외부 문서 로드
- **문서 분할**: 긴 문서를 의미 있는 단위로 분할

---

## 📚 참고 자료

- [PGVector 공식 문서](https://github.com/pgvector/pgvector)
- [Spring AI PGVector 문서](https://docs.spring.io/spring-ai/reference/api/vectordb/pgvector.html)
- [Docker Hub - pgvector](https://hub.docker.com/r/pgvector/pgvector)
- [PostgreSQL 공식 문서](https://www.postgresql.org/docs/)

---

## ❓ 학습 확인 문제

다음 질문에 답할 수 있는지 확인해보세요:

1. PGVector란 무엇이고, 왜 사용해야 하나요?
2. Docker를 사용하여 PostgreSQL/PGVector를 실행하는 방법은?
3. Spring AI에서 PGVectorStore를 설정하는 방법은?
4. SimpleVectorStore와 PGVectorStore의 차이는?
5. HNSW와 IVFFlat 인덱스의 차이는?
6. 프로덕션 환경에서 어떤 VectorStore를 선택해야 하나요?

---

**다음 장**: [7.1: RAG 패턴의 이해](../README.md#71-rag-패턴의-이해)

