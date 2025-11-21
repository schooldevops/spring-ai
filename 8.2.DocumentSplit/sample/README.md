# 문서 분할 (Document Transformers) 샘플 프로젝트

이 프로젝트는 긴 문서를 의미 있는 단위로 분할하는 방법을 보여줍니다.

## 📁 프로젝트 구조

```
sample/
├── src/main/kotlin/com/example/springai/
│   ├── DocumentSplitApplication.kt            # 메인 애플리케이션
│   ├── config/
│   │   └── VectorStoreConfig.kt                # VectorStore Bean 설정
│   ├── controller/
│   │   └── DocumentSplitController.kt          # 문서 분할 API
│   ├── service/
│   │   └── DocumentSplitService.kt             # 문서 분할 서비스
│   ├── util/
│   │   ├── TokenTextSplitter.kt                # 토큰 기반 분할기
│   │   ├── SentenceBasedSplitter.kt            # 문장 기반 분할기
│   │   └── ParagraphBasedSplitter.kt          # 단락 기반 분할기
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

### 3. 텍스트 분할 테스트

```bash
curl -X POST http://localhost:8080/api/split/text \
  -H "Content-Type: application/json" \
  -d '{
    "text": "이것은 긴 문서입니다. 여러 문장으로 구성되어 있습니다. 각 문장은 의미를 가지고 있습니다. 문서를 분할하면 더 효율적으로 처리할 수 있습니다.",
    "strategy": "TOKEN",
    "chunkSize": 50,
    "overlap": 10,
    "addToVectorStore": true
  }'
```

## 📝 주요 예제 설명

### 1. TokenTextSplitter (토큰 기반 분할기)

**기능:**
- 토큰 수를 기준으로 문서 분할
- Overlap 지원으로 문맥 유지
- 간단한 토큰 추정 (1 토큰 ≈ 4 문자)

```kotlin
val splitter = TokenTextSplitter(
    chunkSize = 500,  // 각 청크의 최대 토큰 수
    chunkOverlap = 50  // 청크 간 겹치는 토큰 수
)

val chunks = splitter.apply(document)
```

### 2. SentenceBasedSplitter (문장 기반 분할기)

**기능:**
- 문장 단위로 문서 분할
- 의미 보존
- 최대 청크 크기 제한

```kotlin
val splitter = SentenceBasedSplitter(
    maxChunkSize = 500  // 최대 청크 크기 (토큰)
)

val chunks = splitter.split(document)
```

### 3. ParagraphBasedSplitter (단락 기반 분할기)

**기능:**
- 단락 단위로 문서 분할
- 논리적 단위 보존
- 최대 청크 크기 제한

```kotlin
val splitter = ParagraphBasedSplitter(
    maxChunkSize = 1000  // 최대 청크 크기 (토큰)
)

val chunks = splitter.split(document)
```

### 4. DocumentSplitService (통합 서비스)

**기능:**
- 다양한 분할 전략 지원
- VectorStore에 자동 추가
- 배치 처리 지원

```kotlin
val result = documentSplitService.splitAndStore(
    document = document,
    strategy = SplitStrategy.TOKEN,
    chunkSize = 500,
    overlap = 50
)
```

## 💡 학습 포인트

이 샘플 프로젝트를 통해 학습할 수 있는 내용:

1. **문서 분할의 필요성**
   - LLM 토큰 제한 대응
   - 임베딩 효율성 향상
   - 검색 정확도 개선

2. **다양한 분할 전략**
   - 토큰 기반 분할
   - 문장 기반 분할
   - 단락 기반 분할

3. **Overlap의 중요성**
   - 문맥 유지
   - 검색 정확도 향상

4. **파라미터 최적화**
   - 청크 크기 결정
   - Overlap 크기 결정

## 🔧 핵심 패턴

```kotlin
// 1. TokenTextSplitter 생성
val splitter = TokenTextSplitter(
    chunkSize = 500,
    chunkOverlap = 50
)

// 2. 문서 분할
val chunks = splitter.apply(document)

// 3. 메타데이터 추가
val chunksWithMetadata = chunks.mapIndexed { index, chunk ->
    Document(
        chunk.text ?: "",
        document.metadata + chunk.metadata + mapOf(
            "chunkIndex" to index,
            "totalChunks" to chunks.size
        )
    )
}

// 4. VectorStore에 추가
vectorStore.add(chunksWithMetadata)
```

## 📚 참고사항

### 분할 전략 선택

- **TOKEN**: 일반적인 문서, 빠른 처리
- **SENTENCE**: 의미 보존이 중요한 문서
- **PARAGRAPH**: 논리적 구조가 중요한 문서

### 권장 파라미터

- **청크 크기**: 500-800 토큰 (일반 문서)
- **Overlap**: 청크 크기의 10-20%
- **전략**: 문서 유형에 따라 선택

### 메타데이터

분할된 청크에는 다음 메타데이터가 포함됩니다:
- `chunkIndex`: 청크 인덱스
- `totalChunks`: 총 청크 수
- `splitStrategy`: 사용된 분할 전략
- `originalSource`: 원본 문서 소스

---

**다음 학습**: [9.1: Function Calling 개념과 활용](../../README.md#91-function-calling-개념과-활용)

