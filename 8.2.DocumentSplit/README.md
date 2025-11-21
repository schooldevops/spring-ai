# 8.2: 문서 분할 (Document Transformers)

## 📖 학습 목표

이 강의를 마친 후 다음을 달성할 수 있습니다:
- **문서 분할(Chunking)의 필요성**을 이해할 수 있습니다
- **TokenTextSplitter**를 사용하여 토큰 기반으로 문서를 분할할 수 있습니다
- **Overlap(겹침)**의 개념과 중요성을 이해할 수 있습니다
- 다양한 분할 전략을 선택하고 적용할 수 있습니다
- 분할된 문서를 VectorStore에 효율적으로 추가할 수 있습니다
- 분할 파라미터(청크 크기, 오버랩)를 최적화할 수 있습니다

---

## 🔑 핵심 키워드

이 장에서 다루는 핵심 키워드들:

1. **TokenTextSplitter** - 토큰 기반 문서 분할기
2. **Chunking** - 문서를 작은 단위로 분할하는 과정
3. **Overlap** - 청크 간 겹치는 부분 (문맥 유지)
4. **Chunk Size** - 각 청크의 크기 (토큰 또는 문자 수)
5. **Document Transformer** - 문서를 변환하는 인터페이스

---

## 1. 문서 분할이란?

### 1.1 문서 분할의 필요성

긴 문서를 그대로 사용하면 다음과 같은 문제가 발생합니다:

#### 문제점

1. **LLM 토큰 제한**
   - GPT-4: 최대 8,192 토큰 (약 6,000 단어)
   - GPT-4 Turbo: 최대 128,000 토큰
   - 긴 문서는 토큰 제한을 초과할 수 있음

2. **임베딩 효율성**
   - 긴 텍스트는 임베딩 품질이 저하될 수 있음
   - 의미 있는 단위로 분할하면 더 정확한 임베딩 생성

3. **검색 정확도**
   - 작은 청크 단위로 검색하면 더 정확한 결과
   - 관련 부분만 검색하여 비용 절감

### 1.2 문서 분할의 목적

```
긴 문서 (10,000자)
    ↓
문서 분할 (Chunking)
    ↓
작은 청크들 (각 500자)
    ↓
각 청크 임베딩 생성
    ↓
VectorStore에 저장
    ↓
정확한 검색 가능
```

---

## 2. Chunking 전략

### 2.1 분할 전략의 종류

#### 1. 고정 크기 분할 (Fixed Size Chunking)

가장 간단한 방법으로, 고정된 크기로 문서를 분할합니다.

**장점:**
- 구현이 간단
- 예측 가능한 청크 수

**단점:**
- 문장이나 단락 중간에 잘릴 수 있음
- 의미 단위를 고려하지 않음

#### 2. 문장 기반 분할 (Sentence-based Chunking)

문장 단위로 분할하여 의미를 보존합니다.

**장점:**
- 문장 단위로 의미 보존
- 자연스러운 분할

**단점:**
- 문장 길이가 다양하여 청크 크기가 불균일

#### 3. 단락 기반 분할 (Paragraph-based Chunking)

단락 단위로 분할합니다.

**장점:**
- 논리적 단위로 분할
- 의미 보존 우수

**단점:**
- 단락 길이가 매우 길 수 있음

#### 4. 재귀적 분할 (Recursive Chunking)

여러 분할 전략을 재귀적으로 적용합니다.

**장점:**
- 유연한 분할
- 다양한 문서 구조에 적응

**단점:**
- 구현이 복잡

### 2.2 분할 전략 선택 가이드

| 문서 유형 | 권장 전략 | 청크 크기 |
|----------|----------|----------|
| 일반 텍스트 | 문장 기반 | 500-1000 토큰 |
| 코드 | 함수/클래스 기반 | 500-800 토큰 |
| 논문/보고서 | 단락 기반 | 1000-2000 토큰 |
| 대화/채팅 | 메시지 기반 | 200-500 토큰 |

---

## 3. TokenTextSplitter 사용법

### 3.1 TokenTextSplitter란?

**TokenTextSplitter**는 토큰 수를 기준으로 문서를 분할하는 도구입니다.

#### 특징

- **토큰 기반**: 문자 수가 아닌 토큰 수로 분할
- **Overlap 지원**: 청크 간 겹침 설정 가능
- **의미 보존**: 문장 중간에 잘리지 않도록 조정

### 3.2 기본 사용법

```kotlin
import org.springframework.ai.document.Document
import org.springframework.ai.transformer.splitter.TokenTextSplitter

@Service
class DocumentSplitterService {
    fun splitDocument(document: Document, chunkSize: Int = 500, overlap: Int = 50): List<Document> {
        // TokenTextSplitter 생성
        val splitter = TokenTextSplitter(
            chunkSize = chunkSize,
            chunkOverlap = overlap
        )
        
        // 문서 분할
        return splitter.apply(document)
    }
}
```

### 3.3 파라미터 설명

#### chunkSize

각 청크의 최대 토큰 수입니다.

```kotlin
// 작은 청크 (빠른 처리, 더 많은 청크)
val splitter = TokenTextSplitter(chunkSize = 200, chunkOverlap = 20)

// 중간 청크 (균형잡힌 선택)
val splitter = TokenTextSplitter(chunkSize = 500, chunkOverlap = 50)

// 큰 청크 (적은 청크 수, 더 많은 컨텍스트)
val splitter = TokenTextSplitter(chunkSize = 1000, chunkOverlap = 100)
```

#### chunkOverlap

청크 간 겹치는 토큰 수입니다.

```kotlin
// 오버랩 없음 (빠르지만 문맥 손실 가능)
val splitter = TokenTextSplitter(chunkSize = 500, chunkOverlap = 0)

// 작은 오버랩 (기본 권장)
val splitter = TokenTextSplitter(chunkSize = 500, chunkOverlap = 50)

// 큰 오버랩 (문맥 보존 우수, 더 많은 청크)
val splitter = TokenTextSplitter(chunkSize = 500, chunkOverlap = 100)
```

### 3.4 실제 사용 예제

```kotlin
@Service
class DocumentSplitterService {
    fun splitAndStore(
        document: Document,
        vectorStore: VectorStore,
        chunkSize: Int = 500,
        overlap: Int = 50
    ) {
        // 1. 문서 분할
        val splitter = TokenTextSplitter(
            chunkSize = chunkSize,
            chunkOverlap = overlap
        )
        val chunks = splitter.apply(document)
        
        // 2. 메타데이터 추가
        val chunksWithMetadata = chunks.mapIndexed { index, chunk ->
            Document(
                chunk.text,
                chunk.metadata + mapOf(
                    "chunkIndex" to index,
                    "totalChunks" to chunks.size,
                    "originalSource" to document.metadata["source"]
                )
            )
        }
        
        // 3. VectorStore에 추가
        vectorStore.add(chunksWithMetadata)
    }
}
```

---

## 4. Overlap의 중요성

### 4.1 Overlap이란?

**Overlap**은 청크 간 겹치는 부분으로, 문맥을 유지하기 위해 사용합니다.

#### Overlap 없음

```
청크 1: "Spring AI는 프레임워크입니다. 다양한 LLM을 지원합니다."
청크 2: "임베딩 모델을 사용하여 벡터를 생성합니다."
```

→ "다양한 LLM을 지원합니다"와 "임베딩 모델" 사이의 연결이 끊김

#### Overlap 있음

```
청크 1: "Spring AI는 프레임워크입니다. 다양한 LLM을 지원합니다."
청크 2: "다양한 LLM을 지원합니다. 임베딩 모델을 사용하여 벡터를 생성합니다."
```

→ "다양한 LLM을 지원합니다"가 겹쳐서 문맥이 유지됨

### 4.2 Overlap 크기 결정

#### 권장 Overlap 크기

- **chunkSize의 10-20%**: 일반적으로 권장
- **작은 청크 (200 토큰)**: 20-40 토큰 오버랩
- **중간 청크 (500 토큰)**: 50-100 토큰 오버랩
- **큰 청크 (1000 토큰)**: 100-200 토큰 오버랩

#### Overlap이 너무 작으면

- 문맥 손실
- 검색 정확도 저하

#### Overlap이 너무 크면

- 중복 데이터 증가
- 저장 공간 낭비
- 비용 증가

---

## 5. 다양한 분할 전략 구현

### 5.1 고정 크기 분할

```kotlin
@Service
class FixedSizeSplitter {
    fun splitByCharacterSize(text: String, chunkSize: Int, overlap: Int = 0): List<String> {
        val chunks = mutableListOf<String>()
        var start = 0
        
        while (start < text.length) {
            val end = minOf(start + chunkSize, text.length)
            chunks.add(text.substring(start, end))
            start = end - overlap
        }
        
        return chunks
    }
}
```

### 5.2 문장 기반 분할

```kotlin
@Service
class SentenceBasedSplitter {
    fun splitBySentence(text: String, maxChunkSize: Int): List<String> {
        // 문장 분리 (간단한 예제)
        val sentences = text.split(Regex("[.!?]\\s+"))
        val chunks = mutableListOf<String>()
        var currentChunk = StringBuilder()
        
        for (sentence in sentences) {
            if (currentChunk.length + sentence.length > maxChunkSize && currentChunk.isNotEmpty()) {
                chunks.add(currentChunk.toString())
                currentChunk = StringBuilder()
            }
            currentChunk.append(sentence).append(". ")
        }
        
        if (currentChunk.isNotEmpty()) {
            chunks.add(currentChunk.toString())
        }
        
        return chunks
    }
}
```

### 5.3 단락 기반 분할

```kotlin
@Service
class ParagraphBasedSplitter {
    fun splitByParagraph(text: String, maxChunkSize: Int): List<String> {
        val paragraphs = text.split(Regex("\\n\\n+"))
        val chunks = mutableListOf<String>()
        var currentChunk = StringBuilder()
        
        for (paragraph in paragraphs) {
            if (currentChunk.length + paragraph.length > maxChunkSize && currentChunk.isNotEmpty()) {
                chunks.add(currentChunk.toString())
                currentChunk = StringBuilder()
            }
            currentChunk.append(paragraph).append("\n\n")
        }
        
        if (currentChunk.isNotEmpty()) {
            chunks.add(currentChunk.toString())
        }
        
        return chunks
    }
}
```

### 5.4 재귀적 분할

```kotlin
@Service
class RecursiveSplitter {
    fun splitRecursively(text: String, chunkSize: Int, overlap: Int = 0): List<String> {
        // 1. 단락으로 분할 시도
        val paragraphs = text.split(Regex("\\n\\n+"))
        if (paragraphs.all { it.length <= chunkSize }) {
            return paragraphs
        }
        
        // 2. 문장으로 분할 시도
        val sentences = text.split(Regex("[.!?]\\s+"))
        if (sentences.all { it.length <= chunkSize }) {
            return combineSentences(sentences, chunkSize, overlap)
        }
        
        // 3. 고정 크기로 분할
        return splitByFixedSize(text, chunkSize, overlap)
    }
    
    private fun combineSentences(sentences: List<String>, chunkSize: Int, overlap: Int): List<String> {
        val chunks = mutableListOf<String>()
        var currentChunk = StringBuilder()
        
        for (sentence in sentences) {
            if (currentChunk.length + sentence.length > chunkSize && currentChunk.isNotEmpty()) {
                chunks.add(currentChunk.toString())
                currentChunk = StringBuilder()
            }
            currentChunk.append(sentence).append(". ")
        }
        
        if (currentChunk.isNotEmpty()) {
            chunks.add(currentChunk.toString())
        }
        
        return chunks
    }
    
    private fun splitByFixedSize(text: String, chunkSize: Int, overlap: Int): List<String> {
        val chunks = mutableListOf<String>()
        var start = 0
        
        while (start < text.length) {
            val end = minOf(start + chunkSize, text.length)
            chunks.add(text.substring(start, end))
            start = end - overlap
        }
        
        return chunks
    }
}
```

---

## 6. 분할된 문서를 VectorStore에 추가

### 6.1 기본 추가

```kotlin
@Service
class DocumentSplitAndStoreService(
    private val vectorStore: VectorStore
) {
    fun splitAndStore(document: Document, chunkSize: Int = 500, overlap: Int = 50) {
        // 1. 문서 분할
        val splitter = TokenTextSplitter(
            chunkSize = chunkSize,
            chunkOverlap = overlap
        )
        val chunks = splitter.apply(document)
        
        // 2. VectorStore에 추가
        vectorStore.add(chunks)
    }
}
```

### 6.2 메타데이터 보존

```kotlin
fun splitAndStoreWithMetadata(
    document: Document,
    chunkSize: Int = 500,
    overlap: Int = 50
) {
    val splitter = TokenTextSplitter(
        chunkSize = chunkSize,
        chunkOverlap = overlap
    )
    val chunks = splitter.apply(document)
    
    // 원본 메타데이터 보존 및 청크 정보 추가
    val chunksWithMetadata = chunks.mapIndexed { index, chunk ->
        Document(
            chunk.text,
            document.metadata + chunk.metadata + mapOf(
                "chunkIndex" to index,
                "totalChunks" to chunks.size,
                "chunkSize" to chunk.text.length
            )
        )
    }
    
    vectorStore.add(chunksWithMetadata)
}
```

### 6.3 배치 처리

```kotlin
fun splitAndStoreBatch(
    documents: List<Document>,
    chunkSize: Int = 500,
    overlap: Int = 50
) {
    val splitter = TokenTextSplitter(
        chunkSize = chunkSize,
        chunkOverlap = overlap
    )
    
    val allChunks = mutableListOf<Document>()
    
    documents.forEach { document ->
        val chunks = splitter.apply(document)
        val chunksWithMetadata = chunks.mapIndexed { index, chunk ->
            Document(
                chunk.text,
                document.metadata + mapOf(
                    "chunkIndex" to index,
                    "totalChunks" to chunks.size,
                    "originalSource" to document.metadata["source"]
                )
            )
        }
        allChunks.addAll(chunksWithMetadata)
    }
    
    // 모든 청크를 한 번에 추가
    vectorStore.add(allChunks)
}
```

---

## 7. 분할 파라미터 최적화

### 7.1 청크 크기 최적화

#### 작은 청크 (200-300 토큰)

**장점:**
- 빠른 처리
- 정확한 검색
- 낮은 비용

**단점:**
- 많은 청크 수
- 컨텍스트 부족 가능

**적용 시나리오:**
- 짧은 질문-답변
- FAQ 시스템
- 코드 스니펫

#### 중간 청크 (500-800 토큰)

**장점:**
- 균형잡힌 선택
- 적절한 컨텍스트
- 합리적인 청크 수

**단점:**
- 없음 (가장 권장)

**적용 시나리오:**
- 일반 문서
- 블로그 글
- 매뉴얼

#### 큰 청크 (1000-2000 토큰)

**장점:**
- 풍부한 컨텍스트
- 적은 청크 수

**단점:**
- 느린 처리
- 높은 비용
- 검색 정확도 저하 가능

**적용 시나리오:**
- 긴 논문
- 상세한 보고서
- 복잡한 문서

### 7.2 Overlap 최적화

#### Overlap = 0

**장점:**
- 빠른 처리
- 중복 없음

**단점:**
- 문맥 손실
- 검색 정확도 저하

#### Overlap = chunkSize의 10-20%

**장점:**
- 균형잡힌 선택
- 적절한 문맥 유지

**단점:**
- 없음 (가장 권장)

#### Overlap = chunkSize의 30% 이상

**장점:**
- 강한 문맥 유지

**단점:**
- 많은 중복
- 높은 비용

---

## 8. 실전 활용 예제

### 8.1 문서 로딩 + 분할 + 저장 파이프라인

```kotlin
@Service
class DocumentProcessingPipeline(
    private val vectorStore: VectorStore,
    private val documentLoader: UniversalDocumentLoader
) {
    fun processDocument(
        filePath: String,
        chunkSize: Int = 500,
        overlap: Int = 50
    ): Map<String, Any> {
        // 1. 문서 로드
        val documents = documentLoader.loadDocument(filePath)
        
        // 2. 각 문서 분할
        val splitter = TokenTextSplitter(
            chunkSize = chunkSize,
            chunkOverlap = overlap
        )
        
        val allChunks = mutableListOf<Document>()
        documents.forEach { document ->
            val chunks = splitter.apply(document)
            val chunksWithMetadata = chunks.mapIndexed { index, chunk ->
                Document(
                    chunk.text,
                    document.metadata + mapOf(
                        "chunkIndex" to index,
                        "totalChunks" to chunks.size
                    )
                )
            }
            allChunks.addAll(chunksWithMetadata)
        }
        
        // 3. VectorStore에 추가
        vectorStore.add(allChunks)
        
        return mapOf(
            "status" to "success",
            "originalDocuments" to documents.size,
            "totalChunks" to allChunks.size,
            "averageChunkSize" to allChunks.map { it.text.length }.average()
        )
    }
}
```

### 8.2 디렉토리 전체 처리

```kotlin
fun processDirectory(
    directoryPath: String,
    chunkSize: Int = 500,
    overlap: Int = 50
): Map<String, Any> {
    val directory = File(directoryPath)
    val allChunks = mutableListOf<Document>()
    val splitter = TokenTextSplitter(
        chunkSize = chunkSize,
        chunkOverlap = overlap
    )
    
    directory.walkTopDown().forEach { file ->
        if (file.isFile) {
            try {
                val documents = documentLoader.loadDocument(file.absolutePath)
                documents.forEach { document ->
                    val chunks = splitter.apply(document)
                    val chunksWithMetadata = chunks.mapIndexed { index, chunk ->
                        Document(
                            chunk.text,
                            document.metadata + mapOf(
                                "chunkIndex" to index,
                                "totalChunks" to chunks.size,
                                "fileName" to file.name
                            )
                        )
                    }
                    allChunks.addAll(chunksWithMetadata)
                }
            } catch (e: Exception) {
                println("파일 처리 실패: ${file.name} - ${e.message}")
            }
        }
    }
    
    vectorStore.add(allChunks)
    
    return mapOf(
        "status" to "success",
        "totalChunks" to allChunks.size,
        "processedFiles" to directory.walkTopDown().count { it.isFile }
    )
}
```

---

## 9. 요약

### 9.1 핵심 내용 정리

1. **문서 분할의 필요성**: LLM 토큰 제한, 임베딩 효율성, 검색 정확도
2. **TokenTextSplitter**: 토큰 기반 문서 분할기
3. **Chunking 전략**: 고정 크기, 문장 기반, 단락 기반, 재귀적
4. **Overlap**: 청크 간 겹침으로 문맥 유지
5. **파라미터 최적화**: 청크 크기와 오버랩 조정

### 9.2 기본 패턴

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
        chunk.text,
        document.metadata + mapOf(
            "chunkIndex" to index,
            "totalChunks" to chunks.size
        )
    )
}

// 4. VectorStore에 추가
vectorStore.add(chunksWithMetadata)
```

### 9.3 권장 설정

- **청크 크기**: 500-800 토큰 (일반 문서)
- **Overlap**: 청크 크기의 10-20%
- **전략**: 문장 기반 또는 단락 기반 분할

### 9.4 다음 학습 내용

이제 문서를 분할할 수 있으니, 다음 장에서는:
- **Function Calling**: LLM이 함수를 호출하도록 설정
- **멀티모달**: 이미지와 텍스트를 함께 처리

---

## 📚 참고 자료

- [Spring AI Document Transformers 문서](https://docs.spring.io/spring-ai/reference/api/document-transformers.html)
- [ETL 파이프라인 문서](https://docs.spring.io/spring-ai/reference/patterns/etl-pipeline.html)

---

## ❓ 학습 확인 문제

다음 질문에 답할 수 있는지 확인해보세요:

1. 문서 분할이 왜 필요한가요?
2. TokenTextSplitter를 사용하여 문서를 분할하는 방법은?
3. Overlap이 무엇이고 왜 중요한가요?
4. 청크 크기를 어떻게 결정하나요?
5. 분할된 문서를 VectorStore에 추가하는 방법은?
6. 다양한 분할 전략의 장단점은 무엇인가요?

---

**다음 장**: [9.1: Function Calling 개념과 활용](../README.md#91-function-calling-개념과-활용)

