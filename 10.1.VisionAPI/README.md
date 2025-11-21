# 10.1: Vision API와 이미지 인식

## 📖 학습 목표

이 강의를 마친 후 다음을 달성할 수 있습니다:
- **Vision API**의 개념과 활용 방법을 이해할 수 있습니다
- GPT-4o, Claude 3 등 Vision을 지원하는 모델에 텍스트와 함께 이미지를 전송할 수 있습니다
- **UserMessage**에 **Media**를 포함하여 이미지를 전송할 수 있습니다
- **MimeType**을 사용하여 이미지 형식을 지정할 수 있습니다
- 이미지를 분석하고 설명을 요청할 수 있습니다
- 이미지와 텍스트를 함께 사용하는 **멀티모달** 요청을 처리할 수 있습니다

---

## 🔑 핵심 키워드

이 장에서 다루는 핵심 키워드들:

1. **Multi-modality** - 텍스트와 이미지 등 여러 형태의 데이터를 함께 처리
2. **Vision** - 이미지를 이해하고 분석하는 AI 기능
3. **UserMessage** - 사용자 메시지에 이미지를 포함
4. **Media** - 이미지나 다른 미디어 데이터를 나타내는 객체
5. **MimeType** - 이미지의 형식을 지정 (image/png, image/jpeg 등)

---

## 1. Vision API란?

### 1.1 Vision API의 개념

**Vision API**는 LLM이 이미지를 이해하고 분석할 수 있게 해주는 기능입니다. 텍스트뿐만 아니라 이미지도 함께 전송하여 다음과 같은 작업을 수행할 수 있습니다:

- **이미지 설명**: 이미지의 내용을 텍스트로 설명
- **이미지 분석**: 이미지의 객체, 색상, 스타일 등을 분석
- **이미지 질문**: 이미지에 대한 질문에 답변
- **OCR (광학 문자 인식)**: 이미지에서 텍스트 추출

### 1.2 Vision을 지원하는 모델

| 모델 | 제공자 | 특징 |
|------|--------|------|
| **GPT-4o** | OpenAI | 높은 이미지 이해 능력, 빠른 응답 |
| **GPT-4 Turbo** | OpenAI | 이미지 분석 및 설명 |
| **Claude 3** | Anthropic | 높은 이미지 해석 능력 |
| **Gemini Pro Vision** | Google | 이미지 및 비디오 분석 |

### 1.3 Vision API의 활용 사례

1. **이미지 설명 생성**: 시각 장애인을 위한 이미지 설명
2. **상품 이미지 분석**: 전자상거래 상품 태깅 및 분류
3. **의료 이미지 분석**: X-ray, CT 스캔 등 의료 이미지 분석
4. **문서 OCR**: 스캔된 문서에서 텍스트 추출
5. **콘텐츠 모더레이션**: 부적절한 이미지 감지

---

## 2. Spring AI에서 Vision API 사용하기

### 2.1 기본 구조

Spring AI에서 Vision API를 사용하려면:

1. **Vision 지원 모델 설정**: GPT-4o, Claude 3 등 Vision 모델 사용
2. **이미지 준비**: Base64로 인코딩된 이미지 또는 URL
3. **UserMessage 생성**: Media 객체를 포함한 UserMessage 생성
4. **ChatModel 호출**: 일반적인 ChatModel 호출과 동일

### 2.2 이미지 전송 방법

> ⚠️ **주의**: Spring AI 1.0.0-M6에서는 Vision API가 아직 완전히 지원되지 않을 수 있습니다. 
> 이 샘플은 Vision API 사용 패턴을 보여주며, 실제 구현은 모델별로 다를 수 있습니다.

#### Base64 인코딩된 이미지

```kotlin
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.core.io.Resource
import org.springframework.util.StreamUtils
import java.util.Base64

@Service
class VisionService(
    private val chatModel: ChatModel
) {
    fun analyzeImage(imageResource: Resource, question: String): String {
        // 이미지를 Base64로 인코딩
        val imageBytes = StreamUtils.copyToByteArray(imageResource.inputStream)
        val base64Image = Base64.getEncoder().encodeToString(imageBytes)
        
        // MimeType 자동 감지
        val mimeType = getMimeTypeFromResource(imageResource)
        
        // Vision API를 사용하려면 모델별로 다른 방식이 필요할 수 있습니다.
        // 현재는 Base64 인코딩된 이미지를 텍스트로 포함하는 방식으로 시뮬레이션합니다.
        val promptText = """
            $question
            
            이미지 데이터 (Base64): $base64Image
            이미지 형식: $mimeType
            
            주의: 실제 Vision API를 사용하려면 GPT-4o 또는 Claude 3 등 Vision 지원 모델이 필요하며,
            모델별로 이미지 전송 방식이 다를 수 있습니다.
        """.trimIndent()
        
        // UserMessage 생성
        val userMessage = UserMessage(promptText)
        
        // ChatModel 호출
        val prompt = Prompt(userMessage)
        val response = chatModel.call(prompt)
        
        return response.results.firstOrNull()?.output?.text ?: "응답 없음"
    }
}
```

#### 이미지 URL 사용

> ⚠️ **주의**: Spring AI 1.0.0-M6에서는 이미지 URL을 직접 사용하는 방식이 지원되지 않을 수 있습니다.
> 이미지를 다운로드하여 Base64로 인코딩하는 방식을 사용하세요.

---

## 3. UserMessage와 Media 사용하기

### 3.1 UserMessage에 이미지 포함

> ⚠️ **주의**: Spring AI 1.0.0-M6에서는 Media 클래스가 없을 수 있습니다.
> 현재는 Base64 인코딩된 이미지를 텍스트로 포함하는 방식을 사용합니다.

```kotlin
import org.springframework.ai.chat.messages.UserMessage

// 이미지를 Base64로 인코딩하여 텍스트로 포함
val promptText = """
    이 이미지를 설명해주세요
    
    이미지 데이터 (Base64): $base64Image
    이미지 형식: image/png
""".trimIndent()

val userMessage = UserMessage(promptText)
```

### 3.2 여러 이미지 전송

```kotlin
// 여러 이미지를 Base64로 인코딩하여 텍스트로 포함
val promptText = """
    이 두 이미지를 비교해주세요
    
    첫 번째 이미지 데이터 (Base64): $base64Image1
    첫 번째 이미지 형식: image/png
    
    두 번째 이미지 데이터 (Base64): $base64Image2
    두 번째 이미지 형식: image/jpeg
""".trimIndent()

val userMessage = UserMessage(promptText)
```

### 3.3 MimeType 지정

지원하는 이미지 형식:
- `image/png` - PNG 형식
- `image/jpeg` 또는 `image/jpg` - JPEG 형식
- `image/gif` - GIF 형식
- `image/webp` - WebP 형식

```kotlin
val media = Media(
    data = base64Image,
    mimeType = "image/jpeg" // JPEG 형식 지정
)
```

---

## 4. 실제 사용 예제

### 4.1 이미지 설명 요청

```kotlin
@Service
class ImageDescriptionService(
    private val chatModel: ChatModel
) {
    fun describeImage(imageResource: Resource): String {
        val imageBytes = StreamUtils.copyToByteArray(imageResource.inputStream)
        val base64Image = Base64.getEncoder().encodeToString(imageBytes)
        val mimeType = getMimeTypeFromResource(imageResource)
        
        val promptText = """
            이 이미지를 자세히 설명해주세요.
            
            이미지 데이터 (Base64): $base64Image
            이미지 형식: $mimeType
        """.trimIndent()
        
        val userMessage = UserMessage(promptText)
        val prompt = Prompt(userMessage)
        val response = chatModel.call(prompt)
        
        return response.results.firstOrNull()?.output?.text ?: "설명을 생성할 수 없습니다."
    }
}
```

### 4.2 이미지에 대한 질문

```kotlin
fun askQuestionAboutImage(
    imageResource: Resource,
    question: String
): String {
    val imageBytes = StreamUtils.copyToByteArray(imageResource.inputStream)
    val base64Image = Base64.getEncoder().encodeToString(imageBytes)
    val mimeType = getMimeTypeFromResource(imageResource)
    
    val promptText = """
        $question
        
        이미지 데이터 (Base64): $base64Image
        이미지 형식: $mimeType
    """.trimIndent()
    
    val userMessage = UserMessage(promptText)
    val prompt = Prompt(userMessage)
    val response = chatModel.call(prompt)
    
    return response.results.firstOrNull()?.output?.text ?: "답변을 생성할 수 없습니다."
}
```

### 4.3 이미지 분석 (객체, 색상, 스타일 등)

```kotlin
fun analyzeImage(
    imageResource: Resource
): String {
    val imageBytes = StreamUtils.copyToByteArray(imageResource.inputStream)
    val base64Image = Base64.getEncoder().encodeToString(imageBytes)
    val mimeType = getMimeTypeFromResource(imageResource)
    
    val promptText = """
        이 이미지를 분석해주세요:
        1. 이미지에 있는 주요 객체들
        2. 주요 색상
        3. 이미지 스타일
        4. 이미지의 분위기나 감정
        
        이미지 데이터 (Base64): $base64Image
        이미지 형식: $mimeType
    """.trimIndent()
    
    val userMessage = UserMessage(promptText)
    val prompt = Prompt(userMessage)
    val response = chatModel.call(prompt)
    
    return response.results.firstOrNull()?.output?.text ?: "분석을 생성할 수 없습니다."
}
```

---

## 5. 이미지 형식 처리

### 5.1 이미지 형식 자동 감지

```kotlin
fun getMimeTypeFromResource(resource: Resource): String {
    val filename = resource.filename ?: ""
    return when {
        filename.endsWith(".png", ignoreCase = true) -> "image/png"
        filename.endsWith(".jpg", ignoreCase = true) -> "image/jpeg"
        filename.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
        filename.endsWith(".gif", ignoreCase = true) -> "image/gif"
        filename.endsWith(".webp", ignoreCase = true) -> "image/webp"
        else -> "image/jpeg" // 기본값
    }
}

fun analyzeImageWithAutoMimeType(imageResource: Resource, question: String): String {
    val imageBytes = StreamUtils.copyToByteArray(imageResource.inputStream)
    val base64Image = Base64.getEncoder().encodeToString(imageBytes)
    val mimeType = getMimeTypeFromResource(imageResource)
    
    val media = Media(
        data = base64Image,
        mimeType = mimeType
    )
    
    val userMessage = UserMessage(question, media)
    val prompt = Prompt(userMessage)
    val response = chatModel.call(prompt)
    
    return response.results.firstOrNull()?.output?.text ?: "응답 없음"
}
```

### 5.2 이미지 크기 제한

대부분의 Vision 모델은 이미지 크기에 제한이 있습니다:

- **GPT-4o**: 최대 20MB
- **Claude 3**: 최대 5MB
- **이미지 해상도**: 일반적으로 2048x2048 픽셀 이하 권장

```kotlin
fun validateImageSize(imageBytes: ByteArray, maxSizeMB: Int = 5): Boolean {
    val sizeMB = imageBytes.size / (1024.0 * 1024.0)
    return sizeMB <= maxSizeMB
}

fun analyzeImageWithValidation(imageResource: Resource, question: String): String {
    val imageBytes = StreamUtils.copyToByteArray(imageResource.inputStream)
    
    if (!validateImageSize(imageBytes, maxSizeMB = 5)) {
        throw IllegalArgumentException("이미지 크기가 너무 큽니다. 최대 5MB까지 지원합니다.")
    }
    
    val base64Image = Base64.getEncoder().encodeToString(imageBytes)
    val mimeType = getMimeTypeFromResource(imageResource)
    
    val media = Media(
        data = base64Image,
        mimeType = mimeType
    )
    
    val userMessage = UserMessage(question, media)
    val prompt = Prompt(userMessage)
    val response = chatModel.call(prompt)
    
    return response.results.firstOrNull()?.output?.text ?: "응답 없음"
}
```

---

## 6. REST API 엔드포인트 구현

### 6.1 이미지 업로드 및 분석

```kotlin
@RestController
@RequestMapping("/api/vision")
class VisionController(
    private val visionService: VisionService
) {
    
    @PostMapping("/analyze")
    fun analyzeImage(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("question", defaultValue = "이 이미지를 설명해주세요") question: String
    ): Map<String, Any> {
        val imageResource = file.resource
        val description = visionService.analyzeImage(imageResource, question)
        
        return mapOf(
            "question" to question,
            "description" to description,
            "filename" to file.originalFilename,
            "size" to file.size
        )
    }
}
```

### 6.2 이미지 설명 생성

```kotlin
@PostMapping("/describe")
fun describeImage(
    @RequestParam("file") file: MultipartFile
): Map<String, Any> {
    val imageResource = file.resource
    val description = visionService.describeImage(imageResource)
    
    return mapOf(
        "description" to description,
        "filename" to file.originalFilename
    )
}
```

---

## 7. 주의사항 및 모범 사례

### 7.1 주의사항

1. **모델 선택**: Vision을 지원하는 모델 사용 (GPT-4o, Claude 3 등)
2. **이미지 크기**: 이미지 크기 제한 확인 및 검증
3. **비용**: 이미지 분석은 텍스트보다 비용이 높을 수 있음
4. **프라이버시**: 민감한 이미지는 주의하여 사용

### 7.2 모범 사례

1. **이미지 크기 최적화**: 필요 이상으로 큰 이미지는 리사이즈
2. **MimeType 정확히 지정**: 올바른 MimeType 지정으로 성능 향상
3. **에러 처리**: 이미지 로드 실패, 크기 초과 등 예외 처리
4. **캐싱**: 동일한 이미지에 대한 반복 분석은 캐싱 고려

---

## 8. 트러블슈팅

### 8.1 일반적인 문제들

#### 문제 1: 이미지가 전송되지 않음

**증상**: 이미지가 포함되지 않고 텍스트만 처리됨

**원인**:
- Media 객체가 올바르게 생성되지 않음
- Base64 인코딩 오류
- MimeType이 올바르지 않음

**해결책**:
- Base64 인코딩 확인
- MimeType이 올바른지 확인
- Media 객체 생성 코드 확인

#### 문제 2: 이미지 크기 초과

**증상**: 이미지 업로드 시 오류 발생

**원인**: 이미지 크기가 모델의 제한을 초과

**해결책**:
- 이미지 리사이즈
- 이미지 압축
- 이미지 크기 검증 로직 추가

#### 문제 3: Vision 모델이 아닌 모델 사용

**증상**: 이미지를 처리하지 못함

**원인**: Vision을 지원하지 않는 모델 사용

**해결책**:
- GPT-4o, Claude 3 등 Vision 지원 모델 사용
- application.yml에서 모델 설정 확인

---

## 9. 요약

### 9.1 핵심 내용 정리

1. **Vision API**: 이미지를 이해하고 분석하는 AI 기능
2. **UserMessage**: 이미지를 포함한 사용자 메시지
3. **Media**: 이미지 데이터를 나타내는 객체
4. **MimeType**: 이미지 형식 지정
5. **Base64 인코딩**: 이미지를 Base64로 인코딩하여 전송

### 9.2 구현 패턴

> ⚠️ **주의**: Spring AI 1.0.0-M6에서는 Media 클래스가 없을 수 있습니다.
> 현재는 Base64 인코딩된 이미지를 텍스트로 포함하는 방식을 사용합니다.

```kotlin
// 1. 이미지를 Base64로 인코딩
val imageBytes = StreamUtils.copyToByteArray(imageResource.inputStream)
val base64Image = Base64.getEncoder().encodeToString(imageBytes)
val mimeType = getMimeTypeFromResource(imageResource)

// 2. 텍스트 프롬프트에 이미지 데이터 포함
val promptText = """
    이미지에 대한 질문
    
    이미지 데이터 (Base64): $base64Image
    이미지 형식: $mimeType
""".trimIndent()

// 3. UserMessage 생성
val userMessage = UserMessage(promptText)

// 4. ChatModel 호출
val prompt = Prompt(userMessage)
val response = chatModel.call(prompt)
```

### 9.3 다음 학습 내용

이제 Vision API를 배웠으니, 다음 장에서는:
- **이미지 업로드**: Spring Boot API를 통한 이미지 업로드 및 분석
- **실전 프로젝트**: Vision API를 활용한 실제 애플리케이션 구현

---

## 📚 참고 자료

- [Spring AI Multimodality 공식 문서](https://docs.spring.io/spring-ai/reference/api/multimodal.html)
- [OpenAI Vision API 가이드](https://platform.openai.com/docs/guides/vision)
- [Anthropic Claude Vision 가이드](https://docs.anthropic.com/claude/docs/vision)

---

## ❓ 학습 확인 문제

다음 질문에 답할 수 있는지 확인해보세요:

1. Vision API가 필요한 이유는 무엇인가요?
2. Spring AI에서 이미지를 전송하는 방법은?
3. UserMessage에 Media를 포함하는 방법은?
4. MimeType의 역할은 무엇인가요?
5. Vision을 지원하는 모델은 어떤 것들이 있나요?

---

**다음 장**: [10.2: 이미지 업로드 및 분석](../README.md#102-이미지-업로드-및-분석)

