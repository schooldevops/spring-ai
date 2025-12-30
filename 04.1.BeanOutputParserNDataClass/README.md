# 4.1: BeanOutputParser와 Kotlin Data Class

## 📖 학습 목표

이 강의를 마친 후 다음을 달성할 수 있습니다:
- **BeanOutputParser**의 개념과 필요성을 이해할 수 있습니다
- **Kotlin Data Class**를 정의하고 BeanOutputParser와 함께 사용할 수 있습니다
- **.getFormat()** 메서드를 사용하여 LLM에 응답 형식을 지시할 수 있습니다
- **JSON 응답**을 자동으로 파싱하여 구조화된 데이터로 변환할 수 있습니다
- **실제 사용 예제**를 통해 BeanOutputParser를 활용할 수 있습니다

---

## 🔑 핵심 키워드

이 장에서 다루는 핵심 키워드들:

1. **BeanOutputParser** - LLM 응답을 Java/Kotlin 객체로 파싱하는 Spring AI 컴포넌트
2. **data class** - Kotlin에서 데이터를 담는 불변(immutable) 클래스
3. **.getFormat()** - LLM이 따라야 할 응답 형식을 반환하는 메서드
4. **JSON 응답** - 구조화된 데이터 형식으로 LLM 응답을 요청
5. **자동 파싱** - 문자열 응답을 자동으로 객체로 변환

---

## 1. BeanOutputParser란?

### 1.1 BeanOutputParser의 필요성

#### 문제: 비정형 텍스트 응답의 한계

```kotlin
// ❌ 문제: LLM 응답이 텍스트로만 반환되어 파싱이 어려움
val prompt = Prompt(UserMessage("사용자 정보를 JSON 형식으로 제공해주세요"))
val response = chatModel.call(prompt)
val text = response.results.firstOrNull()?.output?.text ?: ""

// 문제점:
// - 수동으로 파싱해야 함
// - JSON 형식이 일관되지 않을 수 있음
// - 에러 처리 복잡
// - 타입 안전성 부족
```

#### 해결: BeanOutputParser 사용

```kotlin
// ✅ 해결: 자동으로 구조화된 객체로 파싱
data class UserInfo(
    val name: String,
    val age: Int,
    val email: String
)

val parser = BeanOutputParser(UserInfo::class.java)
val format = parser.format

val prompt = Prompt(
    listOf(
        SystemMessage("다음 형식으로 응답해주세요: $format"),
        UserMessage("사용자 정보를 제공해주세요")
    )
)

val response = chatModel.call(prompt)
val userInfo: UserInfo = parser.parse(response.results.firstOrNull()?.output?.text ?: "")

// 장점:
// - 자동 파싱
// - 타입 안전성
// - 일관된 형식
// - 에러 처리 용이
```

### 1.2 BeanOutputParser의 정의

**BeanOutputParser**는 LLM의 텍스트 응답을 Java/Kotlin Bean(객체)으로 자동 파싱하는 Spring AI의 핵심 컴포넌트입니다.

**주요 특징:**
- **자동 파싱**: JSON 문자열을 자동으로 객체로 변환
- **타입 안전**: 컴파일 타임에 타입 체크
- **형식 지시**: `.getFormat()`으로 LLM에 응답 형식 요구
- **에러 처리**: 파싱 실패 시 명확한 예외 발생

---

## 2. Kotlin Data Class 기초

### 2.1 Data Class란?

**Data Class**는 Kotlin에서 데이터를 담는 불변(immutable) 클래스입니다.

#### 기본 정의

```kotlin
data class UserInfo(
    val name: String,
    val age: Int,
    val email: String
)
```

#### Data Class 특징

1. **불변성**: `val`로 선언된 속성은 읽기 전용
2. **자동 생성**: `equals()`, `hashCode()`, `toString()`, `copy()` 자동 생성
3. **구조 분해**: `val (name, age, email) = userInfo` 가능

### 2.2 BeanOutputParser와 호환되는 Data Class 작성법

#### ✅ 좋은 예: 기본 타입 사용

```kotlin
data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val description: String
)
```

#### ✅ 좋은 예: 중첩 객체

```kotlin
data class Address(
    val street: String,
    val city: String,
    val zipCode: String
)

data class User(
    val name: String,
    val age: Int,
    val address: Address
)
```

#### ✅ 좋은 예: 리스트 포함

```kotlin
data class Recipe(
    val name: String,
    val ingredients: List<String>,
    val steps: List<String>
)
```

#### ⚠️ 주의: Nullable 타입

```kotlin
data class UserProfile(
    val name: String,
    val age: Int? = null,  // 선택적 필드
    val bio: String? = null
)
```

---

## 3. BeanOutputParser 기본 사용법

### 3.1 단계별 예제

#### 1단계: Data Class 정의

```kotlin
data class UserInfo(
    val name: String,
    val age: Int,
    val email: String
)
```

#### 2단계: BeanOutputParser 생성

```kotlin
import org.springframework.ai.parser.BeanOutputParser

val parser = BeanOutputParser(UserInfo::class.java)
```

#### 3단계: Format 문자열 가져오기

```kotlin
val format = parser.format  // LLM에 전달할 형식 설명
```

#### 4단계: Prompt에 Format 포함

```kotlin
val prompt = Prompt(
    listOf(
        SystemMessage("다음 JSON 형식으로 응답해주세요:\n$format"),
        UserMessage("사용자 정보를 제공해주세요")
    )
)
```

#### 5단계: LLM 호출 및 파싱

```kotlin
val response = chatModel.call(prompt)
val text = response.results.firstOrNull()?.output?.text ?: ""
val userInfo: UserInfo = parser.parse(text)
```

### 3.2 전체 코드 예제

```kotlin
@RestController
class BeanParserController(
    private val chatModel: ChatModel
) {
    @PostMapping("/parse-user")
    fun parseUser(@RequestBody request: ParseRequest): UserInfo {
        // 1. Parser 생성
        val parser = BeanOutputParser(UserInfo::class.java)
        
        // 2. Format 가져오기
        val format = parser.format
        
        // 3. Prompt 생성
        val prompt = Prompt(
            listOf(
                SystemMessage(
                    """
                    다음 JSON 형식으로 응답해주세요:
                    $format
                    
                    응답은 반드시 JSON 형식이어야 합니다.
                    """.trimIndent()
                ),
                UserMessage(request.question)
            )
        )
        
        // 4. LLM 호출
        val response = chatModel.call(prompt)
        val text = response.results.firstOrNull()?.output?.text ?: throw IllegalStateException("응답 없음")
        
        // 5. 파싱
        return parser.parse(text)
    }
}

data class UserInfo(
    val name: String,
    val age: Int,
    val email: String
)

data class ParseRequest(
    val question: String
)
```

---

## 4. 실전 활용 예제

### 4.1 사용자 프로필 추출

```kotlin
@RestController
class ProfileExtractionController(
    private val chatModel: ChatModel
) {
    @PostMapping("/extract-profile")
    fun extractProfile(@RequestBody text: String): UserProfile {
        val parser = BeanOutputParser(UserProfile::class.java)
        val format = parser.format
        
        val prompt = Prompt(
            listOf(
                SystemMessage(
                    """
                    다음 텍스트에서 사용자 정보를 추출하여 다음 형식으로 응답해주세요:
                    $format
                    """.trimIndent()
                ),
                UserMessage(text)
            )
        )
        
        val response = chatModel.call(prompt)
        val text = response.results.firstOrNull()?.output?.text ?: throw IllegalStateException("응답 없음")
        return parser.parse(text)
    }
}

data class UserProfile(
    val name: String,
    val age: Int?,
    val email: String?,
    val location: String?,
    val bio: String?
)
```

### 4.2 제품 정보 추출

```kotlin
@RestController
class ProductController(
    private val chatModel: ChatModel
) {
    @PostMapping("/parse-product")
    fun parseProduct(@RequestBody description: String): Product {
        val parser = BeanOutputParser(Product::class.java)
        val format = parser.format
        
        val prompt = Prompt(
            listOf(
                SystemMessage(
                    """
                    제품 설명을 분석하여 다음 형식으로 구조화해주세요:
                    $format
                    """.trimIndent()
                ),
                UserMessage(description)
            )
        )
        
        val response = chatModel.call(prompt)
        val text = response.results.firstOrNull()?.output?.text ?: throw IllegalStateException("응답 없음")
        return parser.parse(text)
    }
}

data class Product(
    val name: String,
    val price: Double,
    val category: String,
    val description: String,
    val features: List<String>
)
```

### 4.3 중첩 구조 파싱

```kotlin
@RestController
class NestedController(
    private val chatModel: ChatModel
) {
    @PostMapping("/parse-nested")
    fun parseNested(@RequestBody request: ParseRequest): CompanyInfo {
        val parser = BeanOutputParser(CompanyInfo::class.java)
        val format = parser.format
        
        val prompt = Prompt(
            listOf(
                SystemMessage("다음 형식으로 응답해주세요:\n$format"),
                UserMessage(request.question)
            )
        )
        
        val response = chatModel.call(prompt)
        val text = response.results.firstOrNull()?.output?.text ?: throw IllegalStateException("응답 없음")
        return parser.parse(text)
    }
}

data class Address(
    val street: String,
    val city: String,
    val zipCode: String,
    val country: String
)

data class CompanyInfo(
    val name: String,
    val address: Address,
    val employees: Int,
    val departments: List<String>
)
```

---

## 5. 고급 활용 기법

### 5.1 서비스 레이어 분리

```kotlin
@Service
class ParsingService(
    private val chatModel: ChatModel
) {
    /**
     * 제네릭을 사용한 범용 파싱 메서드
     */
    inline fun <reified T : Any> parseResponse(
        systemMessage: String,
        userMessage: String
    ): T {
        val parser = BeanOutputParser(T::class.java)
        val format = parser.format
        
        val prompt = Prompt(
            listOf(
                SystemMessage("$systemMessage\n\n응답 형식:\n$format"),
                UserMessage(userMessage)
            )
        )
        
        val response = chatModel.call(prompt)
        val text = response.results.firstOrNull()?.output?.text 
            ?: throw IllegalStateException("응답 없음")
        
        return parser.parse(text)
    }
}

@RestController
class ServiceBasedController(
    private val parsingService: ParsingService
) {
    @PostMapping("/parse-generic")
    fun parseGeneric(@RequestBody request: ParseRequest): UserInfo {
        return parsingService.parseResponse<UserInfo>(
            systemMessage = "사용자 정보를 추출해주세요.",
            userMessage = request.question
        )
    }
}
```

### 5.2 에러 처리 및 검증

```kotlin
@RestController
class SafeParserController(
    private val chatModel: ChatModel
) {
    @PostMapping("/safe-parse")
    fun safeParse(@RequestBody request: ParseRequest): Map<String, Any> {
        val parser = BeanOutputParser(UserInfo::class.java)
        val format = parser.format
        
        val prompt = Prompt(
            listOf(
                SystemMessage("다음 형식으로 응답해주세요:\n$format"),
                UserMessage(request.question)
            )
        )
        
        return try {
            val response = chatModel.call(prompt)
            val text = response.results.firstOrNull()?.output?.text 
                ?: throw IllegalStateException("응답 없음")
            
            val userInfo = parser.parse(text)
            mapOf(
                "success" to true,
                "data" to userInfo
            )
        } catch (e: Exception) {
            mapOf(
                "success" to false,
                "error" to e.message ?: "알 수 없는 오류"
            )
        }
    }
}
```

### 5.3 커스텀 Format 메시지

```kotlin
@RestController
class CustomFormatController(
    private val chatModel: ChatModel
) {
    @PostMapping("/custom-format")
    fun customFormat(@RequestBody request: ParseRequest): Product {
        val parser = BeanOutputParser(Product::class.java)
        val format = parser.format
        
        // 커스텀 Format 메시지
        val formatInstructions = """
        다음 JSON 형식으로 응답해주세요:
        $format
        
        추가 요구사항:
        - 모든 필드는 필수입니다
        - 가격은 숫자로만 표시해주세요
        - features는 최소 3개 이상 포함해주세요
        """.trimIndent()
        
        val prompt = Prompt(
            listOf(
                SystemMessage(formatInstructions),
                UserMessage(request.question)
            )
        )
        
        val response = chatModel.call(prompt)
        val text = response.results.firstOrNull()?.output?.text 
            ?: throw IllegalStateException("응답 없음")
        
        return parser.parse(text)
    }
}
```

---

## 6. BeanOutputParser 메서드 상세

### 6.1 .format 속성

**format**은 LLM이 따라야 할 JSON 스키마 형식을 반환합니다.

```kotlin
val parser = BeanOutputParser(UserInfo::class.java)
val format = parser.format

// 예시 출력:
// {
//   "name": "string",
//   "age": "integer",
//   "email": "string"
// }
```

### 6.2 .parse() 메서드

**parse()**는 JSON 문자열을 객체로 변환합니다.

```kotlin
val jsonText = """{"name": "홍길동", "age": 30, "email": "hong@example.com"}"""
val userInfo = parser.parse(jsonText)
```

### 6.3 파싱 과정

1. **JSON 문자열 입력**: LLM 응답 텍스트
2. **JSON 파싱**: Jackson을 사용하여 JSON 파싱
3. **객체 변환**: Data Class 인스턴스로 변환
4. **타입 검증**: 필드 타입 확인

---

## 7. 베스트 프랙티스

### 7.1 Data Class 설계 원칙

#### ✅ 좋은 예: 명확한 필드명

```kotlin
data class UserProfile(
    val fullName: String,
    val birthYear: Int,
    val contactEmail: String
)
```

#### ❌ 나쁜 예: 모호한 필드명

```kotlin
data class UserProfile(
    val n: String,  // name인지 nickname인지 불명확
    val y: Int,     // year인지 yearOfBirth인지 불명확
    val e: String   // email인지 employeeId인지 불명확
)
```

### 7.2 Nullable vs Non-nullable

#### 선택적 필드는 Nullable로

```kotlin
data class UserProfile(
    val name: String,        // 필수
    val age: Int? = null,    // 선택
    val bio: String? = null  // 선택
)
```

### 7.3 기본값 사용

```kotlin
data class Settings(
    val theme: String = "light",
    val language: String = "ko",
    val notifications: Boolean = true
)
```

### 7.4 중첩 객체 vs 평면 구조

#### 중첩 구조가 더 명확한 경우

```kotlin
// ✅ 좋은 예: 논리적으로 그룹화
data class Address(
    val street: String,
    val city: String,
    val zipCode: String
)

data class User(
    val name: String,
    val address: Address
)
```

#### 평면 구조가 더 간단한 경우

```kotlin
// ✅ 좋은 예: 단순한 경우
data class User(
    val name: String,
    val street: String,
    val city: String
)
```

---

## 8. 주의사항 및 트러블슈팅

### 8.1 실제 발생한 오류: Type Mismatch Error

#### 증상

```
com.fasterxml.jackson.databind.exc.InvalidFormatException: 
Cannot deserialize value of type `int` from String "연령은 민감한 정보일 수 있으므로, 
온라인 스펙이나 관심사를 묻는 것이 좋습니다.": not a valid `int` value
```

#### 원인 분석

이 오류는 다음과 같은 상황에서 발생합니다:

1. **모호한 질문**: 사용자가 "어떤 질문이 좋을까?"와 같이 실제 데이터를 요청하지 않고 조언을 구함
2. **AI의 잘못된 응답**: AI가 실제 데이터 대신 조언 텍스트를 JSON 필드에 넣음
3. **타입 불일치**: `age` 필드(Int 타입)에 문자열이 들어가서 역직렬화 실패

**잘못된 AI 응답 예시:**
```json
{
  "name": "사용자",
  "age": "연령은 민감한 정보일 수 있으므로...",
  "email": "example@example.com"
}
```

#### 해결 방법

**1. 명확한 프롬프트 작성**

```kotlin
// ❌ 나쁜 예: 모호한 시스템 메시지
SystemMessage("다음 JSON 형식으로 응답해주세요: $format")

// ✅ 좋은 예: 명확하고 구체적인 지시
SystemMessage(
    """
    당신은 JSON 데이터 생성 전문가입니다.
    사용자의 질문에서 정보를 추출하거나 생성하여 다음 JSON 형식으로 응답해주세요:
    $format
    
    중요한 규칙:
    1. 응답은 반드시 유효한 JSON 형식이어야 합니다.
    2. JSON 외에 다른 텍스트나 설명을 포함하지 마세요.
    3. age는 반드시 숫자(정수)여야 합니다.
    4. 정보가 명시되지 않은 경우 합리적인 기본값을 사용하세요.
    5. 조언이나 제안이 아닌 실제 데이터를 반환하세요.
    """.trimIndent()
)
```

**2. 구체적인 테스트 질문 사용**

```kotlin
// ❌ 나쁜 예: 모호한 질문
{"question": "사용자 정보를 제공해주세요. 어떤 질문이 좋을까?"}

// ✅ 좋은 예: 구체적인 데이터 포함
{"question": "사용자 정보를 제공해주세요. 이름은 홍길동, 나이는 30, 이메일은 hong@example.com입니다."}

// ✅ 좋은 예: 자연어에서 추출
{"question": "안녕하세요, 저는 김철수입니다. 25살이고 이메일은 kim@test.com 입니다."}
```

**3. 에러 처리 강화**

```kotlin
return try {
    parser.parse(text)
} catch (e: Exception) {
    throw IllegalArgumentException(
        "JSON 파싱 실패: ${e.message}\n\n" +
        "LLM 응답 내용:\n$text\n\n" +
        "예상 형식:\n$format",
        e
    )
}
```

### 8.2 프롬프트 작성 베스트 프랙티스

#### ✅ 효과적인 프롬프트 패턴

```kotlin
val effectivePrompt = """
당신은 [역할]입니다.
[작업 설명]을 수행하여 다음 형식으로 응답해주세요:
$format

중요한 규칙:
1. [형식 요구사항]
2. [타입 제약사항]
3. [기본값 처리 방법]
4. [금지사항]
""".trimIndent()
```

#### ❌ 피해야 할 프롬프트 패턴

```kotlin
// 너무 간단함
val badPrompt1 = "JSON으로 응답해주세요"

// 모호함
val badPrompt2 = "다음 형식으로 응답해주세요: $format"

// 타입 제약 없음
val badPrompt3 = "사용자 정보를 JSON으로 주세요"
```

### 8.3 일반적인 문제들

#### 문제 1: 파싱 실패

**증상:**
```
JsonProcessingException: Unrecognized field
```

**원인**: LLM 응답이 JSON 형식이 아니거나 필드명 불일치

**해결책:**
```kotlin
// 1. Format 메시지를 명확히 전달
val formatInstructions = """
반드시 다음 형식으로만 응답해주세요:
$format

JSON만 반환하고 다른 텍스트는 포함하지 마세요.
""".trimIndent()

// 2. 에러 처리 추가
try {
    val userInfo = parser.parse(text)
} catch (e: JsonProcessingException) {
    // JSON 정리 (마크다운 코드 블록 제거 등)
    val cleanedJson = text.replace("```json", "")
        .replace("```", "")
        .trim()
    val userInfo = parser.parse(cleanedJson)
}
```

#### 문제 2: 필드 타입 불일치

**증상:**
```
MismatchedInputException: Cannot deserialize value of type `Int` from String
```

**원인**: LLM이 숫자를 문자열로 반환

**해결책:**
```kotlin
// Data Class에서 타입 변환 허용
data class UserInfo(
    val name: String,
    @JsonProperty("age")
    val age: Int,  // Jackson이 자동으로 변환 시도
    val email: String
)
```

#### 문제 3: Optional 필드 누락

**증상:**
```
MissingRequiredPropertyException
```

**원인**: 필수 필드가 LLM 응답에 없음

**해결책:**
```kotlin
// Nullable 타입으로 변경
data class UserInfo(
    val name: String,
    val age: Int? = null,  // 선택적
    val email: String
)
```

### 8.2 JSON 정리 유틸리티

```kotlin
object JsonCleaner {
    fun cleanJsonText(text: String): String {
        return text
            .replace("```json", "")
            .replace("```", "")
            .trim()
            .lines()
            .filter { !it.trim().startsWith("//") }  // 주석 제거
            .joinToString("\n")
    }
}

// 사용
val cleanedJson = JsonCleaner.cleanJsonText(text)
val userInfo = parser.parse(cleanedJson)
```

---

## 9. 요약

### 9.1 핵심 내용 정리

1. **BeanOutputParser**: LLM 응답을 객체로 자동 파싱
2. **Data Class**: Kotlin의 불변 데이터 클래스
3. **.format**: LLM에 전달할 JSON 스키마 형식
4. **.parse()**: JSON 문자열을 객체로 변환
5. **타입 안전**: 컴파일 타임에 타입 체크

### 9.2 기본 패턴

```kotlin
// 1. Data Class 정의
data class UserInfo(
    val name: String,
    val age: Int,
    val email: String
)

// 2. Parser 생성
val parser = BeanOutputParser(UserInfo::class.java)
val format = parser.format

// 3. Prompt 생성 (Format 포함)
val prompt = Prompt(
    listOf(
        SystemMessage("다음 형식으로 응답해주세요:\n$format"),
        UserMessage("사용자 정보를 제공해주세요")
    )
)

// 4. LLM 호출
val response = chatModel.call(prompt)
val text = response.results.firstOrNull()?.output?.text ?: ""

// 5. 파싱
val userInfo: UserInfo = parser.parse(text)
```

> 💡 **중요**: Spring AI 1.0.0-M6에서 응답은 `response.results.firstOrNull()?.output?.text`로 접근합니다.

### 9.3 다음 학습 내용

이제 BeanOutputParser 기본 사용법을 배웠으니, 다음 장에서는:
- **리스트 및 맵 파싱**: ListOutputParser, MapOutputParser
- **복잡한 구조 파싱**: 중첩 리스트, 맵 조합
- **커스텀 파서**: 특수한 형식 파싱

---

## 📚 참고 자료

- [Spring AI OutputParser 공식 문서](https://docs.spring.io/spring-ai/reference/api/output-parser.html)
- [Kotlin Data Classes](https://kotlinlang.org/docs/data-classes.html)
- [Jackson JSON Processing](https://github.com/FasterXML/jackson)

---

## ❓ 학습 확인 문제

다음 질문에 답할 수 있는지 확인해보세요:

1. BeanOutputParser를 사용하는 이유는 무엇인가요?
2. .format 메서드는 어떤 역할을 하나요?
3. Kotlin Data Class를 BeanOutputParser와 함께 사용하는 방법은?
4. 파싱 실패 시 어떻게 처리하나요?
5. 중첩된 구조의 데이터를 파싱하는 방법은?

---

**다음 장**: [4.2: 리스트 및 맵 파싱](../README.md#42-리스트-및-맵-파싱)

