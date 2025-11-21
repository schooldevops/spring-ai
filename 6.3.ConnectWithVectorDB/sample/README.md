# PostgreSQL/PGVector 연동 샘플 프로젝트

이 프로젝트는 Spring AI에서 PostgreSQL/PGVector를 사용하여 벡터 저장소를 연동하는 방법을 보여줍니다.

## ✅ 완전히 동작하는 PgVectorStore 구현

이 샘플 프로젝트는 **실제 PgVectorStore**를 사용하여 구현되었습니다:
- ✅ PostgreSQL/PGVector 환경 설정
- ✅ Docker Compose를 사용한 자동 PostgreSQL 실행
- ✅ PGVector 확장 자동 활성화
- ✅ 실제 PgVectorStore Bean 설정
- ✅ 영구 데이터 저장

## 📁 프로젝트 구조

```
sample/
├── docker-compose.yml                          # PostgreSQL/PGVector Docker 설정
├── init-scripts/
│   └── 01-init-pgvector.sql                   # PGVector 확장 자동 활성화
├── src/main/kotlin/com/example/springai/
│   ├── PGVectorApplication.kt                 # 메인 애플리케이션
│   ├── config/
│   │   └── PGVectorStoreConfig.kt              # PgVectorStore Bean 설정
│   ├── controller/
│   │   ├── BasicDocumentController.kt          # 기본 문서 추가 및 검색
│   │   ├── BatchDocumentController.kt          # 배치 문서 추가
│   │   ├── DeleteDocumentController.kt         # 문서 삭제
│   │   └── ServiceBasedController.kt           # 서비스 기반 사용
│   ├── service/
│   │   └── DocumentService.kt                 # 문서 관리 서비스
│   └── model/
│       └── CommonModels.kt                     # 공통 모델
└── src/main/resources/
    └── application.yml                         # 설정 파일
```

## 🚀 빠른 시작

### 1. Docker로 PostgreSQL/PGVector 실행

```bash
# 프로젝트 디렉토리로 이동
cd 6.3.ConnectWithVectorDB/sample

# Docker Compose로 PostgreSQL 시작 (PGVector 확장 자동 활성화)
docker-compose up -d

# 로그 확인
docker-compose logs -f postgres

# 컨테이너 상태 확인
docker-compose ps
```

**참고**: `init-scripts/01-init-pgvector.sql` 파일이 자동으로 실행되어 PGVector 확장이 활성화됩니다.

### 2. 환경 변수 설정

```bash
export OPENAI_API_KEY="sk-your-api-key-here"
```

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

또는 IntelliJ IDEA에서 `PGVectorApplication.kt` 실행

### 4. 실행 확인

애플리케이션이 정상적으로 시작되면 다음과 같은 메시지가 표시됩니다:

```
Started PGVectorApplication in X.XXX seconds
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

### 3. DeleteDocumentController

**문서 삭제:**
- `/api/documents/delete`: 문서 삭제 (PgVectorStore에서 지원)

### 4. ServiceBasedController

**서비스 기반 사용:**
- `/api/service/document/*`: 문서 관리 서비스
- `/api/service/document/stats`: 문서 통계

## 💡 학습 포인트

이 샘플 프로젝트를 통해 학습할 수 있는 내용:

1. **PostgreSQL/PGVector 설정**
   - Docker Compose를 사용한 PostgreSQL 실행
   - PGVector 확장 자동 활성화 (init-scripts)

2. **Spring AI PGVector 연동**
   - spring-ai-pgvector-store 의존성 추가
   - JdbcTemplate과 EmbeddingModel을 사용한 PgVectorStore 설정

3. **PgVectorStore Bean 설정**
   - JdbcTemplate Bean 생성
   - PgVectorStore.Builder 사용
   - HNSW 인덱스, 코사인 거리 설정

4. **VectorStore 인터페이스**
   - add() 메서드로 문서 추가
   - similaritySearch() 메서드로 검색
   - delete() 메서드로 문서 삭제

5. **프로덕션 환경**
   - 영구 데이터 저장
   - 대규모 데이터 지원

## 🔧 핵심 패턴

```kotlin
// 1. JdbcTemplate Bean 생성
@Bean
fun jdbcTemplate(): JdbcTemplate {
    return JdbcTemplate(dataSource)
}

// 2. PgVectorStore Bean 생성
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

// 3. 문서 추가 (SimpleVectorStore와 동일)
val document = Document("문서 내용", metadata)
vectorStore.add(listOf(document))

// 4. 유사도 검색 (SimpleVectorStore와 동일)
val results = vectorStore.similaritySearch(query) ?: emptyList()
val limited = results.take(topK)

// 5. 문서 삭제 (PgVectorStore에서 지원)
vectorStore.delete(listOf(documentId))
```

## 📚 참고사항

### PostgreSQL 설정

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/vectordb
    username: springai
    password: springai123
    driver-class-name: org.postgresql.Driver
```

### Docker Compose

```yaml
services:
  postgres:
    image: pgvector/pgvector:pg16  # PostgreSQL 16 버전 사용 (안정적)
    environment:
      POSTGRES_USER: springai
      POSTGRES_PASSWORD: springai123
      POSTGRES_DB: vectordb
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init-scripts:/docker-entrypoint-initdb.d  # 자동 초기화
```

### PGVector 확장 자동 활성화

`init-scripts/01-init-pgvector.sql` 파일이 Docker 컨테이너 시작 시 자동으로 실행되어 PGVector 확장을 활성화합니다:

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

## ❌ 문제 해결

### 문제 1: Docker 컨테이너가 시작되지 않음

**증상:**
```
Error: Cannot connect to the Docker daemon
```

**해결책:**
1. Docker Desktop 실행 확인
2. Docker 서비스 상태 확인: `docker ps`

### 문제 2: PostgreSQL 연결 실패

**증상:**
```
Connection refused: connect
```

**해결책:**
1. Docker 컨테이너 실행 확인: `docker-compose ps`
2. PostgreSQL 포트 확인: `netstat -an | grep 5432`
3. application.yml의 데이터소스 URL 확인

### 문제 3: PGVector 확장 없음

**증상:**
```
ERROR: extension "vector" does not exist
```

**해결책:**
1. `init-scripts/01-init-pgvector.sql` 파일 확인
2. Docker 컨테이너 재시작: `docker-compose restart postgres`
3. 수동으로 확장 활성화:
   ```sql
   CREATE EXTENSION IF NOT EXISTS vector;
   ```

### 문제 4: PgVectorStore 빌드 오류

**증상:**
```
Unresolved reference: Builder
```

**해결책:**
1. `JdbcTemplate` Bean이 생성되었는지 확인
2. `PgVectorStore.builder(jdbcTemplate, embeddingModel)` 사용 확인
3. Spring AI 1.0.0-M6 버전 확인

## ✅ 체크리스트

실행 전 확인사항:

- [ ] JDK 17 이상 설치됨
- [ ] Docker 및 Docker Compose 설치됨
- [ ] Docker 컨테이너 실행됨 (`docker-compose ps`)
- [ ] PGVector 확장 활성화됨 (자동 또는 수동)
- [ ] OpenAI API Key 발급됨
- [ ] 환경 변수 설정됨
- [ ] 프로젝트 빌드 성공 (`./gradlew build`)
- [ ] 애플리케이션 실행 성공 (`./gradlew bootRun`)
- [ ] API 엔드포인트 응답 확인

## 🎓 학습 권장 순서

1. **Docker 환경 설정**: `docker-compose up -d`
2. **PGVector 확장 확인**: `docker exec -it spring-ai-postgres psql -U springai -d vectordb -c "SELECT * FROM pg_extension WHERE extname = 'vector';"`
3. **기본 문서 추가**: `/api/documents/add`
4. **유사도 검색**: `/api/documents/search`
5. **배치 문서 추가**: `/api/documents/batch/add`
6. **문서 삭제**: `/api/documents/delete`
7. **서비스 기반 사용**: `/api/service/document/*`
8. **PostgreSQL 데이터 확인**: `psql` 접속하여 테이블 확인
9. **코드 분석**: PGVectorStoreConfig, DocumentService 확인

---

**다음 학습**: [7.1: RAG 패턴의 이해](../../README.md#71-rag-패턴의-이해)
