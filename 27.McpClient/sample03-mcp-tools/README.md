# Sample 03: MCP Tools Integration

## 📖 개요
이 샘플은 MCP Client에서 외부 Tools(도구)를 활용하여 LLM(ChatClient)과 연동하는 방법을 보여줍니다.
로컬 JAR 파일(STDIO)를 직접 실행하거나, 외부 서버(SSE 기반 원격 서버)를 연결하여 `ToolClient` 구조를 구성하고 있습니다.

## 🛠️ 주요 소스 코드 분석

### 1. `application.yml` (설정)
```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
    mcp:
      client:
        enabled: true
        local-server:
          transport:
            stdio:
              command: "java"
              args: [ "-jar", ".../mcpserver-streamable-http-sample-1.0.0.jar" ]
        remote-server:
          transport:
            sse:
              url: "http://localhost:9802/mcp/sse"
```
- 하나의 클라이언트에서 복수의 서버 연동 정보(`local-server`, `remote-server`)를 설정합니다.
- **STDIO**: 로컬에서 MCP Server Jar 명령을 통해 다이렉트로 통신합니다. (여기서는 `28.McpServer/sample03` 의 Jar 파일을 실행하도록 예시하였습니다.)
- **SSE**: 외부 원격 서버가 제공하는 HTTP Event 스트림 경로(예: 포트 `9802`)를 연동하여 Tools 정보를 가져오도록 합니다.

### 2. `ToolClientService.kt` (서비스 로직)
- **`listTools()`**: MCP 클라이언트를 사용해 연결된 서버 모음에서 호출 가능한 도구(Function) 형태가 무엇무엇이 있는지(`calculate`, `convertTemperature` 등) 목록화합니다.
- **`callCalculateTool()`, `callConvertTemperature()`**: 클라이언트에서 직접 매개변수(`Map`)를 조합하여 서버의 특정 함수(`McpSchema.CallToolRequest`)를 실행하도록 지시합니다. 반환된 결괏값을 문자열로 수신합니다.

### 3. `ToolClientController.kt` (API 엔드포인트)
- `GET /api/mcp/tools`: 사용 가능한 도구 목록
- `GET /api/mcp/tools/calculate?operation={op}&a={a}&b={b}`: 계산기 도구 실행
- `GET /api/mcp/tools/convert-temperature?value={v}&from={f}&to={t}`: 온도 단위 변환 도구 실행

## 🎯 어떻게 서비스되고 사용되는지 (흐름 파악)
1. 어플리케이션은 설정에 기반하여 STDIO를 통해 로컬 자식 프로세스와 통신하고, HTTP(SSE)로 외부 서버와 통신하는 2개의 채널을 보유할 수 있습니다.
2. `listTools()`를 통해 연결된 서버들로부터 사용 가능한 연산 지시 항목을 얻습니다. (예산 연산 시스템, 통신사 서버 등 외부 기능 연동)
3. OpenAI 등의 Chat Model과 결합하게 될 경우, AI 모델이 클라이언트 프롬프트를 해석하여 도메인 서버가 제공하는 특정 기능(Tool)의 호출을 지시하게 되고 클라이언트는 이를 서버에 위임한 후 응답을 바탕으로 다음 작업을 수행(Function Calling)하게 될 수 있습니다. 
