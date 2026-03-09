# 28. MCP Server (Model Context Protocol Server)

## 📖 학습 목표

- **MCP Server**를 구축하고 제공합니다
- **@McpResource**로 데이터 소스를 노출합니다
- **@McpTool**로 함수를 제공합니다
- **@McpPrompt**로 템플릿을 관리합니다

---

## 🔑 핵심 키워드

1. **MCP Server** - AI 모델에게 서비스 제공
2. **@McpResource** - 데이터 소스 노출
3. **@McpTool** - 함수 제공
4. **@McpPrompt** - 프롬프트 템플릿
5. **STDIO/WebMVC** - Transport 방식

---

## 1. MCP Server란?

**MCP Server**는 AI 모델이 사용할 수 있는 Resources, Tools, Prompts를 제공하는 서버입니다.

### 제공 기능
- **Resources**: 파일, DB 등 데이터 소스
- **Tools**: 실행 가능한 함수
- **Prompts**: 재사용 가능한 템플릿

---

## 2. 샘플 구성

### Sample 01: Basic MCP Server
- MCP Server 기본 설정
- STDIO Transport
- **포트:** 9800

### Sample 02: MCP Resources Provider
- @McpResource 어노테이션
- 데이터 소스 제공
- **포트:** 9801

### Sample 03: MCP Tools Provider
- @McpTool 어노테이션
- 함수 제공
- **포트:** 9802

### Sample 04: MCP Prompts Provider
- @McpPrompt 어노테이션
- 템플릿 관리
- **포트:** 9803

---

## 3. MCP Server 어노테이션

```kotlin
@McpResource(
    uri = "file://data.txt",
    name = "Data File",
    description = "Sample data file"
)
fun getDataResource(): String {
    return "Sample data content"
}

@McpTool(
    name = "calculate",
    description = "Perform calculation"
)
fun calculate(a: Int, b: Int): Int {
    return a + b
}

@McpPrompt(
    name = "greeting",
    description = "Greeting template"
)
fun greetingPrompt(name: String): String {
    return "Hello, $name!"
}
```

---

## 4. Transport 방식

| Transport | 특징 | 사용 시나리오 |
|-----------|------|---------------|
| **STDIO** | 프로세스 통신 | 로컬 개발 |
| **WebMVC** | HTTP 통신 | 원격 서버 |
| **WebMVC Reactive** | 비동기 HTTP | 고성능 서버 |

---

## 5. 공통 설정

```yaml
spring:
  ai:
    mcp:
      server:
        enabled: true
        name: "My MCP Server"
        version: "1.1.2"
```

---

**시작하기**: [Sample 01: Basic MCP Server](./sample01-basic-server/)
