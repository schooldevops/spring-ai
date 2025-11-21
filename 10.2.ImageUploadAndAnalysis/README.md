# 10.2: 이미지 업로드 및 분석

## 📖 학습 목표

이 강의를 마친 후 다음을 달성할 수 있습니다:
- **Spring Boot API**를 통해 이미지를 업로드할 수 있습니다
- **MultipartFile**을 사용하여 파일 업로드를 처리할 수 있습니다
- 업로드된 이미지를 **Base64로 인코딩**하여 Spring AI에 전송할 수 있습니다
- 이미지를 **Spring AI를 통해 분석**하는 완전한 엔드포인트를 구현할 수 있습니다
- 이미지 업로드 시 **검증 및 에러 처리**를 구현할 수 있습니다
- 이미지 분석 결과를 **JSON 형식**으로 반환할 수 있습니다

---

## 🔑 핵심 키워드

이 장에서 다루는 핵심 키워드들:

1. **MultipartFile** - Spring에서 파일 업로드를 처리하는 인터페이스
2. **Base64** - 이미지를 텍스트로 인코딩하는 방식
3. **이미지 분석** - Spring AI를 통한 이미지 내용 분석
4. **파일 검증** - 업로드된 파일의 형식 및 크기 검증
5. **REST API** - 이미지 업로드 및 분석을 위한 RESTful 엔드포인트

---

## 1. 이미지 업로드 및 분석 개요

### 1.1 전체 흐름

```
1. 사용자가 이미지 파일 업로드
   (MultipartFile)
        ↓
2. 파일 검증
   (형식, 크기 확인)
        ↓
3. 이미지를 Base64로 인코딩
        ↓
4. Spring AI에 전송하여 분석
        ↓
5. 분석 결과를 JSON으로 반환
```

### 1.2 주요 구성 요소

- **Controller**: REST API 엔드포인트 제공
- **Service**: 이미지 처리 및 AI 분석 로직
- **파일 검증**: 이미지 형식 및 크기 검증
- **에러 처리**: 업로드 및 분석 실패 시 적절한 처리

---

## 2. MultipartFile을 사용한 이미지 업로드

### 2.1 MultipartFile 기본 사용법

```kotlin
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/image")
class ImageController {
    
    @PostMapping("/upload")
    fun uploadImage(@RequestParam("file") file: MultipartFile): Map<String, Any> {
        // 파일 정보 확인
        val filename = file.originalFilename ?: "unknown"
        val size = file.size
        val contentType = file.contentType ?: "unknown"
        
        return mapOf(
            "filename" to filename,
            "size" to size,
            "contentType" to contentType
        )
    }
}
```

### 2.2 파일 검증

```kotlin
@Service
class ImageValidationService {
    
    private val allowedContentTypes = setOf(
        "image/png",
        "image/jpeg",
        "image/jpg",
        "image/gif",
        "image/webp"
    )
    
    private val maxFileSize = 20 * 1024 * 1024 // 20MB
    
    fun validateImage(file: MultipartFile): ValidationResult {
        // 파일이 비어있는지 확인
        if (file.isEmpty) {
            return ValidationResult(false, "파일이 비어있습니다.")
        }
        
        // 파일 크기 확인
        if (file.size > maxFileSize) {
            return ValidationResult(false, "파일 크기가 너무 큽니다. 최대 ${maxFileSize / 1024 / 1024}MB까지 지원합니다.")
        }
        
        // 파일 형식 확인
        val contentType = file.contentType
        if (contentType == null || !allowedContentTypes.contains(contentType.lowercase())) {
            return ValidationResult(false, "지원하지 않는 파일 형식입니다. PNG, JPEG, GIF, WebP만 지원합니다.")
        }
        
        return ValidationResult(true, "검증 성공")
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val message: String
)
```

---

## 3. Base64 인코딩

### 3.1 MultipartFile을 Base64로 변환

```kotlin
import org.springframework.web.multipart.MultipartFile
import java.util.Base64

fun convertToBase64(file: MultipartFile): String {
    val imageBytes = file.bytes
    return Base64.getEncoder().encodeToString(imageBytes)
}
```

### 3.2 Resource를 Base64로 변환

```kotlin
import org.springframework.core.io.Resource
import org.springframework.util.StreamUtils
import java.util.Base64

fun convertResourceToBase64(resource: Resource): String {
    val imageBytes = StreamUtils.copyToByteArray(resource.inputStream)
    return Base64.getEncoder().encodeToString(imageBytes)
}
```

---

## 4. 실제 사용 예제

### 4.1 이미지 업로드 및 분석 엔드포인트

```kotlin
@RestController
@RequestMapping("/api/image")
class ImageUploadController(
    private val imageAnalysisService: ImageAnalysisService,
    private val imageValidationService: ImageValidationService
) {
    
    @PostMapping("/upload-and-analyze")
    fun uploadAndAnalyze(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("question", defaultValue = "이 이미지를 설명해주세요.") question: String
    ): ResponseEntity<Map<String, Any>> {
        // 파일 검증
        val validation = imageValidationService.validateImage(file)
        if (!validation.isValid) {
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "error" to validation.message
            ))
        }
        
        try {
            // 이미지 분석
            val analysis = imageAnalysisService.analyzeImage(file, question)
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "filename" to (file.originalFilename ?: "unknown"),
                "size" to file.size,
                "contentType" to (file.contentType ?: "unknown"),
                "question" to question,
                "analysis" to analysis
            ))
        } catch (e: Exception) {
            return ResponseEntity.status(500).body(mapOf(
                "success" to false,
                "error" to "이미지 분석 중 오류 발생: ${e.message}"
            ))
        }
    }
}
```

### 4.2 이미지 분석 서비스

```kotlin
@Service
class ImageAnalysisService(
    private val chatModel: ChatModel
) {
    
    fun analyzeImage(file: MultipartFile, question: String): String {
        // MultipartFile을 Base64로 변환
        val imageBytes = file.bytes
        val base64Image = Base64.getEncoder().encodeToString(imageBytes)
        
        // MimeType 확인
        val mimeType = file.contentType ?: "image/jpeg"
        
        // Spring AI에 전송
        val promptText = """
            $question
            
            이미지 데이터 (Base64): $base64Image
            이미지 형식: $mimeType
        """.trimIndent()
        
        val userMessage = UserMessage(promptText)
        val prompt = Prompt(userMessage)
        val response = chatModel.call(prompt)
        
        return response.results.firstOrNull()?.output?.text 
            ?: "이미지 분석 결과를 생성할 수 없습니다."
    }
}
```

---

## 5. 파일 저장 및 관리

### 5.1 이미지 파일 저장

```kotlin
@Service
class ImageStorageService {
    
    private val uploadDir = Paths.get("uploads/images")
    
    init {
        // 업로드 디렉토리 생성
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir)
        }
    }
    
    fun saveImage(file: MultipartFile): String {
        val filename = "${UUID.randomUUID()}_${file.originalFilename}"
        val filePath = uploadDir.resolve(filename)
        
        file.transferTo(filePath.toFile())
        
        return filename
    }
    
    fun getImageResource(filename: String): Resource {
        val filePath = uploadDir.resolve(filename)
        return FileSystemResource(filePath.toFile())
    }
}
```

### 5.2 이미지 파일 삭제

```kotlin
fun deleteImage(filename: String): Boolean {
    val filePath = uploadDir.resolve(filename)
    return try {
        Files.deleteIfExists(filePath)
        true
    } catch (e: Exception) {
        false
    }
}
```

---

## 6. 에러 처리 및 모범 사례

### 6.1 에러 처리 패턴

```kotlin
@PostMapping("/upload")
fun uploadImage(@RequestParam("file") file: MultipartFile): ResponseEntity<Map<String, Any>> {
    return try {
        // 파일 검증
        val validation = imageValidationService.validateImage(file)
        if (!validation.isValid) {
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "error" to validation.message
            ))
        }
        
        // 이미지 분석
        val result = imageAnalysisService.analyzeImage(file)
        
        ResponseEntity.ok(mapOf(
            "success" to true,
            "result" to result
        ))
    } catch (e: FileSizeLimitExceededException) {
        ResponseEntity.status(413).body(mapOf(
            "success" to false,
            "error" to "파일 크기가 너무 큽니다."
        ))
    } catch (e: Exception) {
        ResponseEntity.status(500).body(mapOf(
            "success" to false,
            "error" to "오류 발생: ${e.message}"
        ))
    }
}
```

### 6.2 파일 크기 제한 설정

```yaml
# application.yml
spring:
  servlet:
    multipart:
      max-file-size: 20MB
      max-request-size: 20MB
```

### 6.3 모범 사례

1. **파일 검증**: 업로드 전 파일 형식 및 크기 검증
2. **에러 처리**: 모든 예외 상황 처리
3. **보안**: 파일명 검증 및 경로 조작 방지
4. **성능**: 큰 파일의 경우 비동기 처리 고려
5. **로깅**: 업로드 및 분석 과정 로깅

---

## 7. 실제 사용 예제

### 7.1 완전한 이미지 업로드 및 분석 API

```kotlin
@RestController
@RequestMapping("/api/image")
class ImageUploadController(
    private val imageAnalysisService: ImageAnalysisService,
    private val imageValidationService: ImageValidationService
) {
    
    @PostMapping("/upload")
    fun uploadImage(
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Map<String, Any>> {
        // 파일 검증
        val validation = imageValidationService.validateImage(file)
        if (!validation.isValid) {
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "error" to validation.message
            ))
        }
        
        try {
            // 이미지 분석
            val analysis = imageAnalysisService.analyzeImage(file)
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "filename" to (file.originalFilename ?: "unknown"),
                "size" to file.size,
                "analysis" to analysis
            ))
        } catch (e: Exception) {
            return ResponseEntity.status(500).body(mapOf(
                "success" to false,
                "error" to "이미지 분석 중 오류 발생: ${e.message}"
            ))
        }
    }
    
    @PostMapping("/upload-with-question")
    fun uploadWithQuestion(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("question") question: String
    ): ResponseEntity<Map<String, Any>> {
        val validation = imageValidationService.validateImage(file)
        if (!validation.isValid) {
            return ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "error" to validation.message
            ))
        }
        
        try {
            val analysis = imageAnalysisService.analyzeImage(file, question)
            
            return ResponseEntity.ok(mapOf(
                "success" to true,
                "filename" to (file.originalFilename ?: "unknown"),
                "question" to question,
                "analysis" to analysis
            ))
        } catch (e: Exception) {
            return ResponseEntity.status(500).body(mapOf(
                "success" to false,
                "error" to "이미지 분석 중 오류 발생: ${e.message}"
            ))
        }
    }
}
```

---

## 8. 주의사항 및 모범 사례

### 8.1 보안 고려사항

1. **파일명 검증**: 경로 조작 공격 방지
2. **파일 크기 제한**: 서버 리소스 보호
3. **파일 형식 검증**: 악성 파일 업로드 방지
4. **저장 경로**: 웹 루트 외부에 저장

### 8.2 성능 고려사항

1. **비동기 처리**: 큰 파일의 경우 비동기 처리
2. **캐싱**: 동일한 이미지에 대한 반복 분석 캐싱
3. **리사이징**: 필요시 이미지 리사이징
4. **압축**: 이미지 압축으로 전송 크기 감소

### 8.3 모범 사례

1. **파일 검증**: 업로드 전 모든 검증 수행
2. **에러 처리**: 명확한 에러 메시지 제공
3. **로깅**: 업로드 및 분석 과정 로깅
4. **문서화**: API 문서 작성

---

## 9. 트러블슈팅

### 9.1 일반적인 문제들

#### 문제 1: 파일 업로드 실패

**증상**: MultipartFile이 비어있음

**원인**:
- 파일 크기 제한 초과
- Content-Type 설정 오류

**해결책**:
- application.yml에서 파일 크기 제한 확인
- Content-Type을 multipart/form-data로 설정

#### 문제 2: Base64 인코딩 오류

**증상**: 인코딩 실패

**원인**: 파일이 손상되었거나 읽을 수 없음

**해결책**:
- 파일이 올바르게 업로드되었는지 확인
- 파일 크기 및 형식 확인

#### 문제 3: 이미지 분석 실패

**증상**: AI 분석 결과가 없음

**원인**:
- Vision 지원 모델 미사용
- Base64 인코딩 오류

**해결책**:
- GPT-4o 또는 Claude 3 사용
- Base64 인코딩 확인

---

## 10. 요약

### 10.1 핵심 내용 정리

1. **MultipartFile**: Spring에서 파일 업로드를 처리하는 인터페이스
2. **Base64 인코딩**: 이미지를 텍스트로 변환하여 전송
3. **파일 검증**: 업로드 전 파일 형식 및 크기 검증
4. **에러 처리**: 모든 예외 상황 처리
5. **REST API**: 이미지 업로드 및 분석을 위한 엔드포인트

### 10.2 구현 패턴

```kotlin
// 1. MultipartFile로 이미지 업로드 받기
@PostMapping("/upload")
fun uploadImage(@RequestParam("file") file: MultipartFile) {
    // 2. 파일 검증
    val validation = validateImage(file)
    
    // 3. Base64로 인코딩
    val base64Image = Base64.getEncoder().encodeToString(file.bytes)
    
    // 4. Spring AI에 전송하여 분석
    val analysis = analyzeImage(base64Image)
    
    // 5. 결과 반환
    return analysis
}
```

### 10.3 다음 학습 내용

이제 이미지 업로드 및 분석을 배웠으니, 다음 장에서는:
- **실전 프로젝트**: 지금까지 배운 모든 기술을 통합한 프로젝트
- **고급 기능**: 이미지 저장, 캐싱, 비동기 처리 등

---

## 📚 참고 자료

- [Spring AI Multimodality 공식 문서](https://docs.spring.io/spring-ai/reference/api/multimodal.html)
- [Spring MultipartFile 가이드](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/multipart.html)
- [OpenAI Vision API 가이드](https://platform.openai.com/docs/guides/vision)

---

## ❓ 학습 확인 문제

다음 질문에 답할 수 있는지 확인해보세요:

1. MultipartFile을 사용하여 이미지를 업로드하는 방법은?
2. Base64 인코딩이 필요한 이유는 무엇인가요?
3. 이미지 업로드 시 파일 검증은 어떻게 하나요?
4. 이미지 분석 결과를 JSON으로 반환하는 방법은?
5. 파일 업로드 시 에러 처리는 어떻게 하나요?

---

**다음 장**: [11장: [실전] 간단한 Q&A 챗봇 API 구현](../README.md#11장-실전-간단한-qa-챗봇-api-구현)

