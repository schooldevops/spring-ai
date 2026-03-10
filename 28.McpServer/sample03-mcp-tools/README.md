# Sample 03: MCP Tools Provider

## 📖 개요
이 샘플은 MCP Server에서 **외부 도구(Tools, 함수)** 를 클라이언트 애플리케이션 및 AI 모델에게 어떻게 제공할 수 있는지 구조를 보여주는 프로젝트입니다.
Streamable HTTP (Chunked Transfer Encoding) Transport 방식을 사용하여 큰 응답이나 점진적인 데이터 전송(스트리밍)을 지원하도록 설계되었습니다.

## 🛠️ 주요 소스 코드 분석

### 1. `application.yml` (설정)
```yaml
spring:
  ai:
    mcp:
      server:
        enabled: true
        name: "Streamable HTTP MCP Server"
        version: "1.0.0"
```
- MCP Server를 활성화하고 서버명을 지정합니다.
- HTTP 통신 기반이며, 기본 포트는 9802 로 설정되어 있습니다.

### 2. `ToolProvider.kt`
- 비즈니스 로직(함수)을 구현해 둔 `@Component` 객체입니다.
- **`calculate(operation, a, b)`**: 사칙연산을 수행하는 함수
- **`convertTemperature(value, from, to)`**: 섭씨/화씨 온도를 변환해주는 함수
- **`listTools()`**: 서버에서 제공 가능한 Tool 의 목록(이름 및 설명)을 반환합니다.
- *참고: 상위 README 설계에 따르면 추후 `@McpTool` 어노테이션이 적용되어 AI 모델(ChatClient)이 직접 Function Calling으로 호출할 수 있는 형태로 매핑될 수 있습니다.*

### 3. `StreamableHttpController.kt` (API 엔드포인트)
- REST API 인터페이스를 제공하여 실제 Tool의 실행을 요청받고 그 결과를 반환합니다.
- `GET /api/tools`: 도구 목록 확인
- `POST /api/tools/calculate`: 계산기 도구 실행
- `POST /api/tools/convert-temperature`: 온도 변환 도구 실행

## 🎯 어떻게 서비스되고 사용되는지 (흐름 파악)
1. **함수(Tool) 등록**: `ToolProvider` 내에 AI 모델이나 외부 클라이언트가 필요로 하는 부가 기능 연산(계산기, 온도 변환 등) 로직을 구현합니다.
2. **호출(Function Calling)**: 클라이언트(`mcp-client`)가 AI(ChatGPT 등)에게 질문을 던지면, AI는 직접 연산하는 대신 이 서버에 등록된 도구 목록을 보고 특정 Tool의 파라미터를 완성해 호출을 지시합니다.
3. 이 샘플에서는 Streamable HTTP 전송 방식을 이용하도록 설계되었으므로, 큰 결과물이나 순차적으로 생성되는 데이터를 끊기지 않고 원활하게 반환하는 데에 그 의도를 두고 있습니다.
