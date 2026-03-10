# 29. MCP with Spring AI Annotations

## 📖 학습 목표

- **Spring AI의 공식 `@Tool` Annotation** 개념과 활용법을 완벽히 이해합니다.
- 선언적 방식으로 강력하고 간결한 **MCP Tool**을 노출하는 서버를 구축합니다.
- 기존의 수동(Programmatic) 방식과 어노테이션(Declarative) 방식의 차이를 인지합니다.
- (참고) Spring AI 1.0.0 M6 이상에서 지원하는 최신 도구 노출 문법을 학습합니다.

---

## 🔑 핵심 키워드

1. **`@Tool`** - AI 모델이나 MCP Client가 실행할 수 있는 함수 툴로 노출합니다.
2. **`@ToolParam`** - `@Tool` 내부의 각 파라미터가 어떤 역할을 하는지 AI에게 설명합니다.
3. **Auto-Configuration** - Spring Context에 등록된 빈(Bean) 내의 `@Tool` 애노테이션을 자동으로 스캔하여 MCP 서버로 발행합니다.

> ⚠️ 주의사항: 기존 인터넷에 떠도는 `@McpTool`, `@McpResource`, `@McpClient` 등의 어노테이션은 공식 Spring AI 프레임워크에 존재하지 않는 환각(Hallucination)입니다. Spring AI는 단일 `@Tool` 어노테이션을 통해 LLM 함수 호출과 MCP 도구 노출을 동시에 통일성 있게 관리합니다.

---

## 1. Spring AI의 Tool Annotations란?

**Tool Annotations**는 선언적 방식으로 MCP 도구를 정의하는 Spring AI의 가장 강력한 기능 중 하나입니다.

### 왜 Annotations를 사용하나요?

**기존 방식 (Programmatic / Functional)**
```kotlin
// 빈을 수동으로 등록하고 람다나 함수로 매핑하는 코드가 필요했습니다.
@Bean
@Description("Calculate the sum of two integers")
fun calculateSum(): (MathRequest) -> Int {
    return { req -> req.a + req.b }
}
```

**Annotations 방식 (Declarative)**
```kotlin
// Service 클래스 내부의 일반 메서드에 애노테이션만 붙이면 끝납니다.
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam

@Service
class MathService {

    @Tool(description = "Calculate the sum of two integers")
    fun calculateSum(
        @ToolParam(description = "First integer") a: Int, 
        @ToolParam(description = "Second integer") b: Int
    ): Int {
        return a + b
    }
}
```

### 주요 장점
- ✅ **간결성**: 레코드(Record) 클래스나 불필요한 DTO 래퍼 클래스를 만들 필요가 없습니다.
- ✅ **이식성**: 여기서 정의한 `@Tool`은 MCP 서버로도 노출되며, 애플리케이션 내부 LLM(ChatClient)에서도 그대로 재사용됩니다.
- ✅ **가독성**: 실제 비즈니스 로직과 Tool 메타데이터가 한 공간에 명확히 선언됩니다.
- ✅ **자동 분석**: Spring AI가 리플렉션을 사용해 파라미터 타입을 JSON Schema로 자동 변환하여 MCP 스펙에 맞게 등록합니다.

---

## 2. Server Annotations 상세: `@Tool` (실행 가능한 함수)

AI나 외부 환경에서 필요할 때 호출할 수 있는 도구를 정의합니다.

### 2.1 기본 사용 예제

```kotlin
import org.springframework.stereotype.Service
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam

@Service // Spring Bean으로 등록되어야 합니다.
class TextProcessingService {

    @Tool(description = "Convert text case: upper, lower, title")
    fun convertCase(
        @ToolParam(description = "The original text to convert") text: String, 
        @ToolParam(description = "Target case format. Must be upper, lower, or title") toCase: String
    ): String {
        return when (toCase.lowercase()) {
            "upper" -> text.uppercase()
            "lower" -> text.lowercase()
            "title" -> text.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
            else -> text
        }
    }
}
```

**사용 시나리오:**
- 계산 함수 (수학, 통계)
- 데이터 변환 함수 (문자열, 형식, 인코딩)
- 외부 API 연동 기능 (주식 가격 조회, 일기예보 조회)
- 사내 DB 데이터 읽기 (읽기/쓰기 권한에 유의)

### 2.2 Nullable과 파라미터 제어

```kotlin
@Tool(description = "Update customer information")
fun updateCustomerInfo(
    @ToolParam(description = "Customer ID") id: Long,
    @ToolParam(description = "New customer name") name: String,
    @ToolParam(description = "Optional email address", required = false) email: String?
): String {
    // ... logic ...
    return "Success"
}
```

### 2.3 Spring Boot + MCP의 자동 래핑 메커니즘

Spring 애플리케이션에 `spring-ai-mcp-spring-boot-starter`를 등록하면, 다음과 같은 일이 일어납니다.

1. **스캔**: Spring Context에 올라온 모든 `@Service`, `@Component` 안의 `@Tool` 어노테이션을 찾습니다.
2. **JSON Schema 생성**: 자바 타입을 읽고 MCP 호환 JSON 스키마로 변환합니다. `email`처럼 `required=false`를 주면 자동으로 Optional 스펙이 됩니다.
3. **McpSyncToolCallback (RPC 변환)**: 스캔한 메서드를 MCP 프로토콜이 전송할 수 있는 콜백 래퍼로 캡슐화합니다.
4. **Tool Registry 등록**: 클라이언트(Claude Desktop 등)에서 연결요청이 들어오면 노출할 목록에 즉시 올립니다.

---

## 3. (정보) Resource와 Prompt는 어떻게 구현하나요?

과거 할루시네이션(가상의 거짓 정보) 문서는 `@McpResource` 같은 어노테이션이 있다고 설명했지만, **Spring AI는 현재 이러한 어노테이션을 제공하지 않습니다.** 
(MCP에서 도구(Tool) 호출이 가장 핵심 메커니즘이기 때문이며, 리소스/프롬프트 기능은 자바 빌더 코드로 구성합니다.)

### 3.1 Resource 등록 (기존 방식 유지)
리소스 등록은 어노테이션이 아닌 빈 등록을 통해 수동으로 진행합니다.

```kotlin
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceRegistration
import io.modelcontextprotocol.spec.McpSchema.Resource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ResourceConfig {
    
    @Bean
    fun systemConfigResource(): SyncResourceRegistration {
        val resource = Resource("config://system", "System Configuration", "application/json", "System application settings", null)
        return SyncResourceRegistration(resource) { _ -> 
            // Return content implementation
            io.modelcontextprotocol.spec.McpSchema.ReadResourceResult(
                listOf(io.modelcontextprotocol.spec.McpSchema.TextResourceContents(
                    "config://system", "application/json", "{\"version\":\"1.0.0\"}"
                ))
            )
        }
    }
}
```

### 3.2 Prompt 등록 (기존 방식 유지)
프롬프트 또한 빌더 구조를 따릅니다.

```kotlin
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptRegistration
import io.modelcontextprotocol.spec.McpSchema.Prompt
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PromptConfig {
    
    @Bean
    fun greetingPrompt(): SyncPromptRegistration {
        val prompt = Prompt("greeting", "Welcome greeting template", emptyList())
        return SyncPromptRegistration(prompt) { _ ->
            // Return prompt building implementation
            io.modelcontextprotocol.spec.McpSchema.GetPromptResult(
                "greeting",
                listOf(io.modelcontextprotocol.spec.McpSchema.PromptMessage(
                    io.modelcontextprotocol.spec.McpSchema.Role.USER,
                    io.modelcontextprotocol.spec.McpSchema.TextContent("Hello, how can I help you?")
                ))
            )
        }
    }
}
```

---

## 4. 모범 사례

### ✅ DO

```kotlin
// 명확한 이름과 파라미터 설명 추가!
@Tool(
    name = "calculateSum",
    description = "Calculate the sum of two integers"
)
fun calculateSum(
    @ToolParam(description = "first number") a: Int, 
    @ToolParam(description = "second number to add") b: Int
): Int
```

### ❌ DON'T

```kotlin
// ❌ AI가 이게 무슨 용도인지 모호함
@Tool(name = "do")
fun doSomething(x: Any): Any

// ❌ 파라미터 설명이 전혀 없음
@Tool(description = "Divide numbers")
fun divide(a: Int, b: Int) = a / b // 만약 b=0이면 크래시 발생 가능, 검증이 필요
```

---

## 5. 실행 및 테스트 (참고)

Spring AI MCP 프로젝트에 구성한 Application을 실행하고 어노테이션 스캔이 원활한지 확인합니다.

```bash
# 로컬에서 스프링 부트 애플리케이션 구동
./gradlew bootRun
```

MCP 클라이언트(예: 이전 샘플 프로젝트의 클라이언트 또는 Claude Desktop 등)에서 연결 후 자동으로 발견되는 도구 목록에 작성하신 `@Tool` 들이 뜨는지 확인하시면 됩니다.

**관련 문서**:
- [MCP Protocol 스펙 가이드](https://modelcontextprotocol.io)
- [Spring AI 공식 Tools Reference](https://docs.spring.io/spring-ai/reference/api/tools.html)
