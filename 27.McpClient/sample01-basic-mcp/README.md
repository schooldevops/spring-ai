# Sample 01: Basic MCP Client

## 📖 개요
이 프로젝트는 Spring AI의 MCP(Model Context Protocol) Client 기능의 가장 기본적인 사용 방법을 보여주는 샘플입니다. 외부 MCP Server와 통신하기 위해 `McpSyncClient`를 사용하며, 서버가 제공하는 도구(Tools)를 조회하고 호출하는 과정을 구현하고 있습니다.

## 🛠️ 주요 소스 코드 분석

### 1. `application.yml` (설정)
```yaml
spring:
  ai:
    mcp:
      client:
        enabled: true
        basic-server:
          transport:
            stdio:
              command: "java"
              args:
                [
                  "-jar",
                  ".../mcpserver-basic-sample-1.0.0.jar",
                ]
```
- **의도**: MCP Client를 활성화하고, 통신할 대상 서버(`basic-server`)를 설정합니다.
- **Transport**: `stdio` (표준 입출력) 방식을 사용하여 로컬 환경의 자바 어플리케이션(McpServer 샘플01의 빌드된 jar 파일)을 서브 프로세스로 직접 실행하여 통신합니다.

### 2. `BasicMcpService.kt` (서비스 로직)
- **`McpSyncClient` 의존성 주입**: Spring Boot Auto-configuration에 의해 생성된 MCP 클라이언트 빈을 주입받아 서버와의 동기식 통신을 담당합니다.
- **`getServerTools()`**: `mcpClient.listTools(null)`를 호출하여 연결된 MCP 서버가 현재 제공하고 있는 도구(Tools)의 목록을 가져옵니다.
- **`callTimeTool()`**: 서버가 노출하고 있는 특정 도구(여기서는 `"getServerTime"`)를 호출합니다. 인자(파라미터)로 `timezone`을 넘겨주어, 서버 로직을 실행한 결과를 받아옵니다.

### 3. `BasicMcpController.kt` (API 엔드포인트)
클라이언트 웹 서비스를 통해 MCP 기능에 접근할 수 있도록 REST API를 노출합니다.
- `GET /api/mcp/tools`: 연결된 MCP 서버의 도구 목록을 반환합니다.
- `GET /api/mcp/call-time-tool`: 클라이언트가 수신한 timezone(기본값 UTC) 파라미터를 사용해 서버의 `getServerTime` 도구를 호출하고 결과를 반환합니다.

## 🎯 как MCP가 서비스되고 사용되는지 (흐름 파악)
1. **서버 구동 (Bootstrapping)**: Spring Boot 앱 기동 시, `application.yml`의 설정에 따라 지정된 jar 파일(MCP Server)을 자식 프로세스로 실행하고, `stdio` 기반으로 연결이 맺어집니다.
2. **도구 조회 (Discovery)**: 사용자가 `/api/mcp/tools` 호출 시, 클라이언트는 서버 측에 사용 가능한 함수(도구)들의 목록을 요청하여 확인합니다.
3. **도구 실행 (Invocation)**: `/api/mcp/call-time-tool`이 호출되면 클라이언트는 `McpSchema.CallToolRequest`를 만들어 서버에 전송합니다. 서버는 해당 요청을 위임받아 실행(`a+b` 연산, 혹은 시간 조회 등)하고 그에 대한 결과를 클라이언트에 되돌려줍니다. 
4. **결과 노출**: 서버 스크립트/앱 영역에서 처리된 결과를 클라이언트가 받아 사용자(혹은 AI 프롬프트 체인)에게 응답합니다.
