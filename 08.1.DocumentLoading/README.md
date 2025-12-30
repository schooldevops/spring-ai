# 8.1: 문서 로딩 (Document Loaders)

## 📖 학습 목표

이 강의를 마친 후 다음을 달성할 수 있습니다:
- **ResourceReader** 인터페이스의 역할과 구조를 이해할 수 있습니다
- **TextReader**를 사용하여 텍스트 파일을 로드할 수 있습니다
- **PdfDocumentReader**를 사용하여 PDF 파일을 로드할 수 있습니다
- **MarkdownReader**를 사용하여 Markdown 파일을 로드할 수 있습니다
- 다양한 형식의 문서를 **Document** 객체로 변환할 수 있습니다
- 로드된 문서를 **VectorStore**에 추가할 수 있습니다
- 파일 시스템, 클래스패스, URL 등 다양한 소스에서 문서를 로드할 수 있습니다

---

## 🔑 핵심 키워드

이 장에서 다루는 핵심 키워드들:

1. **ResourceReader** - 문서를 로드하기 위한 Spring AI 인터페이스
2. **TextReader** - 텍스트 파일을 로드하는 ResourceReader 구현체
3. **PdfDocumentReader** - PDF 파일을 로드하는 ResourceReader 구현체
4. **MarkdownReader** - Markdown 파일을 로드하는 ResourceReader 구현체
5. **Resource** - Spring의 Resource 인터페이스 (파일, URL, 클래스패스 등)
6. **Document** - Spring AI의 문서 표현 클래스

---

## 1. 문서 로딩이란?

### 1.1 문서 로딩의 필요성

RAG 시스템을 구축하려면 외부 문서를 VectorStore에 추가해야 합니다. 문서 로딩은 다음과 같은 과정입니다:

```
외부 문서 (PDF, TXT, MD 등)
    ↓
ResourceReader를 통한 로딩
    ↓
Document 객체로 변환
    ↓
VectorStore에 추가
```

### 1.2 문서 로딩의 목적

- **외부 지식 통합**: 회사 문서, 매뉴얼, 정책 등을 RAG 시스템에 통합
- **자동화**: 수동으로 문서를 입력하는 대신 파일을 자동으로 로드
- **확장성**: 다양한 형식의 문서를 지원하여 시스템 확장

---

## 2. ResourceReader 인터페이스

### 2.1 ResourceReader란?

**ResourceReader**는 Spring AI에서 제공하는 인터페이스로, 다양한 형식의 문서를 로드하여 `Document` 객체로 변환합니다.

#### ResourceReader 인터페이스 구조

```kotlin
interface ResourceReader {
    fun getDocument(resource: Resource): Document
    fun getDocuments(resource: Resource): List<Document>
}
```

### 2.2 ResourceReader 구현체

Spring AI는 다양한 형식의 문서를 로드하기 위한 여러 ResourceReader 구현체를 제공합니다:

1. **TextReader**: 텍스트 파일 (.txt)
2. **PdfDocumentReader**: PDF 파일 (.pdf)
3. **MarkdownReader**: Markdown 파일 (.md, .markdown)
4. **JsonReader**: JSON 파일 (.json)
5. **XmlReader**: XML 파일 (.xml)

### 2.3 Resource 인터페이스

Spring의 `Resource` 인터페이스는 다양한 소스에서 데이터를 읽을 수 있게 해줍니다:

- **FileSystemResource**: 파일 시스템의 파일
- **ClassPathResource**: 클래스패스의 리소스
- **UrlResource**: URL의 리소스
- **ByteArrayResource**: 바이트 배열

---

## 3. TextReader: 텍스트 파일 로딩

### 3.1 TextReader 기본 사용법

`TextReader`는 텍스트 파일을 로드하는 가장 간단한 방법입니다.

#### 기본 예제

```kotlin
import org.springframework.ai.reader.TextReader
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource

@Service
class TextDocumentLoader {
    fun loadTextFile(filePath: String): List<Document> {
        // 1. Resource 생성
        val resource: Resource = FileSystemResource(filePath)
        
        // 2. TextReader 생성
        val reader = TextReader()
        
        // 3. 문서 로드
        return reader.getDocuments(resource)
    }
}
```

### 3.2 다양한 Resource 소스

#### 파일 시스템에서 로드

```kotlin
fun loadFromFileSystem(filePath: String): List<Document> {
    val resource = FileSystemResource(filePath)
    val reader = TextReader()
    return reader.getDocuments(resource)
}
```

#### 클래스패스에서 로드

```kotlin
fun loadFromClasspath(filePath: String): List<Document> {
    val resource = ClassPathResource(filePath)
    val reader = TextReader()
    return reader.getDocuments(resource)
}
```

#### URL에서 로드

```kotlin
fun loadFromUrl(url: String): List<Document> {
    val resource = UrlResource(url)
    val reader = TextReader()
    return reader.getDocuments(resource)
}
```

### 3.3 메타데이터 추가

```kotlin
fun loadTextFileWithMetadata(filePath: String): List<Document> {
    val resource = FileSystemResource(filePath)
    val reader = TextReader()
    val documents = reader.getDocuments(resource)
    
    // 메타데이터 추가
    return documents.map { doc ->
        Document(
            doc.text,
            doc.metadata + mapOf(
                "source" to filePath,
                "type" to "text",
                "loadedAt" to System.currentTimeMillis()
            )
        )
    }
}
```

---

## 4. PdfDocumentReader: PDF 파일 로딩

### 4.1 PdfDocumentReader 기본 사용법

PDF 파일을 로드하려면 `PdfDocumentReader`를 사용합니다.

#### 의존성 추가

```kotlin
dependencies {
    // Spring AI PDF Reader
    implementation("org.springframework.ai:spring-ai-pdf-document-reader:1.0.0-M6")
    
    // Apache PDFBox (PDF 처리)
    implementation("org.apache.pdfbox:pdfbox:3.0.0")
}
```

#### 기본 예제

```kotlin
import org.springframework.ai.reader.pdf.PagePdfDocumentReader
import org.springframework.core.io.FileSystemResource

@Service
class PdfDocumentLoader {
    fun loadPdfFile(filePath: String): List<Document> {
        // 1. Resource 생성
        val resource = FileSystemResource(filePath)
        
        // 2. PdfDocumentReader 생성
        val reader = PagePdfDocumentReader(resource)
        
        // 3. 문서 로드
        return reader.get()
    }
}
```

### 4.2 PDF 페이지별 로딩

PDF는 여러 페이지로 구성되어 있으므로, 각 페이지를 별도의 Document로 로드할 수 있습니다.

```kotlin
fun loadPdfByPage(filePath: String): List<Document> {
    val resource = FileSystemResource(filePath)
    val reader = PagePdfDocumentReader(resource)
    val documents = reader.get()
    
    // 각 페이지에 메타데이터 추가
    return documents.mapIndexed { index, doc ->
        Document(
            doc.text,
            doc.metadata + mapOf(
                "source" to filePath,
                "type" to "pdf",
                "page" to (index + 1),
                "totalPages" to documents.size
            )
        )
    }
}
```

### 4.3 PDF 메타데이터 추출

```kotlin
fun loadPdfWithMetadata(filePath: String): List<Document> {
    val resource = FileSystemResource(filePath)
    val reader = PagePdfDocumentReader(resource)
    val documents = reader.get()
    
    // PDF 메타데이터 추가
    return documents.mapIndexed { index, doc ->
        Document(
            doc.text,
            doc.metadata + mapOf(
                "source" to filePath,
                "type" to "pdf",
                "page" to (index + 1),
                "fileName" to File(filePath).name,
                "fileSize" to File(filePath).length()
            )
        )
    }
}
```

---

## 5. MarkdownReader: Markdown 파일 로딩

### 5.1 MarkdownReader 기본 사용법

Markdown 파일을 로드하려면 `MarkdownReader`를 사용합니다.

#### 기본 예제

```kotlin
import org.springframework.ai.reader.markdown.MarkdownReader
import org.springframework.core.io.FileSystemResource

@Service
class MarkdownDocumentLoader {
    fun loadMarkdownFile(filePath: String): List<Document> {
        // 1. Resource 생성
        val resource = FileSystemResource(filePath)
        
        // 2. MarkdownReader 생성
        val reader = MarkdownReader(resource)
        
        // 3. 문서 로드
        return reader.get()
    }
}
```

### 5.2 Markdown 섹션별 로딩

Markdown 파일을 섹션별로 분할하여 로드할 수 있습니다.

```kotlin
fun loadMarkdownBySection(filePath: String): List<Document> {
    val resource = FileSystemResource(filePath)
    val reader = MarkdownReader(resource)
    val documents = reader.get()
    
    // 섹션별 메타데이터 추가
    return documents.mapIndexed { index, doc ->
        Document(
            doc.text,
            doc.metadata + mapOf(
                "source" to filePath,
                "type" to "markdown",
                "section" to (index + 1),
                "fileName" to File(filePath).name
            )
        )
    }
}
```

---

## 6. 통합 문서 로더 서비스

### 6.1 다중 형식 지원 로더

여러 형식의 문서를 지원하는 통합 로더를 만들 수 있습니다.

```kotlin
@Service
class UniversalDocumentLoader(
    private val vectorStore: VectorStore
) {
    fun loadDocument(filePath: String): List<Document> {
        val file = File(filePath)
        val extension = file.extension.lowercase()
        
        val resource = FileSystemResource(filePath)
        
        return when (extension) {
            "txt" -> {
                val reader = TextReader()
                reader.getDocuments(resource)
            }
            "pdf" -> {
                val reader = PagePdfDocumentReader(resource)
                reader.get()
            }
            "md", "markdown" -> {
                val reader = MarkdownReader(resource)
                reader.get()
            }
            else -> {
                throw UnsupportedOperationException("지원하지 않는 파일 형식: $extension")
            }
        }.map { doc ->
            Document(
                doc.text,
                doc.metadata + mapOf(
                    "source" to filePath,
                    "type" to extension,
                    "fileName" to file.name,
                    "loadedAt" to System.currentTimeMillis()
                )
            )
        }
    }
    
    fun loadAndStore(filePath: String) {
        val documents = loadDocument(filePath)
        vectorStore.add(documents)
    }
}
```

### 6.2 디렉토리 전체 로드

디렉토리 내의 모든 문서를 로드할 수 있습니다.

```kotlin
fun loadDirectory(directoryPath: String): List<Document> {
    val directory = File(directoryPath)
    if (!directory.isDirectory) {
        throw IllegalArgumentException("디렉토리가 아닙니다: $directoryPath")
    }
    
    val allDocuments = mutableListOf<Document>()
    
    directory.walkTopDown().forEach { file ->
        if (file.isFile) {
            try {
                val documents = loadDocument(file.absolutePath)
                allDocuments.addAll(documents)
            } catch (e: Exception) {
                println("파일 로드 실패: ${file.name} - ${e.message}")
            }
        }
    }
    
    return allDocuments
}
```

---

## 7. VectorStore에 문서 추가

### 7.1 로드된 문서를 VectorStore에 추가

문서를 로드한 후 VectorStore에 추가하여 검색 가능하게 만듭니다.

```kotlin
@Service
class DocumentLoadingService(
    private val vectorStore: VectorStore
) {
    fun loadAndStoreTextFile(filePath: String) {
        val resource = FileSystemResource(filePath)
        val reader = TextReader()
        val documents = reader.getDocuments(resource)
        
        // 메타데이터 추가
        val documentsWithMetadata = documents.map { doc ->
            Document(
                doc.text,
                doc.metadata + mapOf(
                    "source" to filePath,
                    "type" to "text",
                    "loadedAt" to System.currentTimeMillis()
                )
            )
        }
        
        // VectorStore에 추가
        vectorStore.add(documentsWithMetadata)
    }
    
    fun loadAndStorePdfFile(filePath: String) {
        val resource = FileSystemResource(filePath)
        val reader = PagePdfDocumentReader(resource)
        val documents = reader.get()
        
        val documentsWithMetadata = documents.mapIndexed { index, doc ->
            Document(
                doc.text,
                doc.metadata + mapOf(
                    "source" to filePath,
                    "type" to "pdf",
                    "page" to (index + 1),
                    "loadedAt" to System.currentTimeMillis()
                )
            )
        }
        
        vectorStore.add(documentsWithMetadata)
    }
}
```

### 7.2 배치 로딩

여러 파일을 한 번에 로드하고 추가할 수 있습니다.

```kotlin
fun loadAndStoreMultipleFiles(filePaths: List<String>) {
    val allDocuments = mutableListOf<Document>()
    
    filePaths.forEach { filePath ->
        try {
            val documents = loadDocument(filePath)
            allDocuments.addAll(documents)
        } catch (e: Exception) {
            println("파일 로드 실패: $filePath - ${e.message}")
        }
    }
    
    // 모든 문서를 한 번에 추가
    vectorStore.add(allDocuments)
}
```

---

## 8. 에러 처리 및 검증

### 8.1 파일 존재 확인

```kotlin
fun loadDocumentSafely(filePath: String): List<Document> {
    val file = File(filePath)
    
    if (!file.exists()) {
        throw FileNotFoundException("파일을 찾을 수 없습니다: $filePath")
    }
    
    if (!file.isFile) {
        throw IllegalArgumentException("파일이 아닙니다: $filePath")
    }
    
    val resource = FileSystemResource(filePath)
    val reader = TextReader()
    return reader.getDocuments(resource)
}
```

### 8.2 파일 형식 검증

```kotlin
fun loadDocumentWithValidation(filePath: String): List<Document> {
    val file = File(filePath)
    val extension = file.extension.lowercase()
    
    val supportedFormats = listOf("txt", "pdf", "md", "markdown")
    if (extension !in supportedFormats) {
        throw UnsupportedOperationException("지원하지 않는 파일 형식: $extension")
    }
    
    return loadDocument(filePath)
}
```

### 8.3 예외 처리

```kotlin
fun loadDocumentWithErrorHandling(filePath: String): Result<List<Document>> {
    return try {
        val documents = loadDocument(filePath)
        Result.success(documents)
    } catch (e: FileNotFoundException) {
        Result.failure(Exception("파일을 찾을 수 없습니다: $filePath", e))
    } catch (e: UnsupportedOperationException) {
        Result.failure(Exception("지원하지 않는 파일 형식: $filePath", e))
    } catch (e: Exception) {
        Result.failure(Exception("문서 로드 중 오류 발생: ${e.message}", e))
    }
}
```

---

## 9. 실전 활용 예제

### 9.1 지식베이스 구축

```kotlin
@Service
class KnowledgeBaseBuilder(
    private val vectorStore: VectorStore
) {
    fun buildKnowledgeBase(documentsDirectory: String) {
        val directory = File(documentsDirectory)
        
        directory.walkTopDown().forEach { file ->
            if (file.isFile) {
                try {
                    val documents = loadDocument(file.absolutePath)
                    val documentsWithMetadata = documents.map { doc ->
                        Document(
                            doc.text,
                            doc.metadata + mapOf(
                                "source" to file.absolutePath,
                                "category" to file.parentFile.name,
                                "loadedAt" to System.currentTimeMillis()
                            )
                        )
                    }
                    vectorStore.add(documentsWithMetadata)
                    println("문서 추가 완료: ${file.name}")
                } catch (e: Exception) {
                    println("문서 추가 실패: ${file.name} - ${e.message}")
                }
            }
        }
    }
}
```

### 9.2 문서 업데이트

```kotlin
fun updateDocument(filePath: String) {
    // 기존 문서 삭제 (파일 경로로 식별)
    val existingDocs = vectorStore.similaritySearch("") ?: emptyList()
    val docsToDelete = existingDocs.filter { 
        it.metadata["source"] == filePath 
    }.map { it.id }
    
    if (docsToDelete.isNotEmpty()) {
        vectorStore.delete(docsToDelete)
    }
    
    // 새 문서 로드 및 추가
    val documents = loadDocument(filePath)
    vectorStore.add(documents)
}
```

---

## 10. 요약

### 10.1 핵심 내용 정리

1. **ResourceReader**: 문서를 로드하기 위한 Spring AI 인터페이스
2. **TextReader**: 텍스트 파일 로딩
3. **PdfDocumentReader**: PDF 파일 로딩
4. **MarkdownReader**: Markdown 파일 로딩
5. **Resource**: Spring의 Resource 인터페이스 (파일, URL, 클래스패스 등)
6. **Document 변환**: 로드된 문서를 Document 객체로 변환
7. **VectorStore 추가**: 로드된 문서를 VectorStore에 추가

### 10.2 기본 패턴

```kotlin
// 1. Resource 생성
val resource = FileSystemResource(filePath)

// 2. ResourceReader 생성
val reader = TextReader()

// 3. 문서 로드
val documents = reader.getDocuments(resource)

// 4. 메타데이터 추가 (선택)
val documentsWithMetadata = documents.map { doc ->
    Document(
        doc.text,
        doc.metadata + mapOf("source" to filePath)
    )
}

// 5. VectorStore에 추가
vectorStore.add(documentsWithMetadata)
```

### 10.3 다음 학습 내용

이제 문서를 로드할 수 있으니, 다음 장에서는:
- **문서 분할**: 긴 문서를 의미 있는 단위로 분할
- **청킹 전략**: 토큰 기반, 문장 기반 분할
- **오버랩**: 청크 간 겹침 처리

---

## 📚 참고 자료

- [Spring AI Document Loaders 문서](https://docs.spring.io/spring-ai/reference/api/document-loaders.html)
- [Spring Resource 문서](https://docs.spring.io/spring-framework/reference/core/resources.html)
- [Apache PDFBox 문서](https://pdfbox.apache.org/)

---

## ❓ 학습 확인 문제

다음 질문에 답할 수 있는지 확인해보세요:

1. ResourceReader 인터페이스의 역할은 무엇인가요?
2. TextReader를 사용하여 텍스트 파일을 로드하는 방법은?
3. PdfDocumentReader를 사용하여 PDF 파일을 로드하는 방법은?
4. MarkdownReader를 사용하여 Markdown 파일을 로드하는 방법은?
5. 로드된 문서를 VectorStore에 추가하는 방법은?
6. 다양한 소스(파일, 클래스패스, URL)에서 문서를 로드하는 방법은?

---

**다음 장**: [8.2: 문서 분할 (Document Transformers)](../README.md#82-문서-분할-document-transformers)

