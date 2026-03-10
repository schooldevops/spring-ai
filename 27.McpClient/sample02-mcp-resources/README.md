# Sample 02: MCP Resources

## 📖 개요
이 샘플은 MCP Client에서 외부 리소스를 조회하고 읽는 방법을 제공합니다.
로컬 JAR 파일(STDIO)를 직접 실행하거나, 외부 서버(SSE 기반 원격 서버)를 연결하여 `ResourceClient` 를 구성하고 있습니다.

## 🛠️ 주요 소스 코드 분석

### 1. `application.yml` (설정)
```yaml
spring:
  ai:
    mcp:
      client:
        enabled: true
        local-server:
          transport:
            stdio:
              command: "java"
              args: [ "-jar", ".../mcpserver-stdio-sse-sample-1.0.0.jar" ]
        remote-server:
          transport:
            sse:
              url: "http://localhost:9801/mcp/sse"
```
- **의도**: 하나의 클라이언트에서 복수의 서버(`local-server`, `remote-server`)를 구성할 수 있습니다. 
- **STDIO**: 로컬에서 MCP Server Jar 파일을 자식 프로세스로 직접 실행합니다.
- **SSE**: 외부 원격 서버가 제공하는 HTTP Event 스트림 경로를 연동합니다.

### 2. `ResourceClientService.kt` (서비스 로직)
- **`McpSyncClient` 의존성 주입**: 복수의 연결정보에 대해 `List<McpSyncClient>` 주입을 받습니다.
- **`listResources()`**: MCP 서버가 제공 중인 외부 리소스(데이터, 파일) 목록을 호출합니다.
- **`readResource(uri: String)`**: 특정 리소스(`McpSchema.ReadResourceRequest`)를 읽도록 명령을 보내 서버로부터 반환된 값의 내용을 얻습니다.

### 3. `ResourceClientController.kt` (API 엔드포인트)
- `GET /api/mcp/resources`: 지원되는 리소스 목록 반환
- `GET /api/mcp/resources/read?uri={uri}`: 쿼리 파라미터로 받은 문자열 경로의 리소스 읽기 요청

## 🎯 어떻게 서비스되고 사용되는지 (흐름 파악)
1. 클라이언트(Sample02앱)가 시작되면서 `application.yml`의 설정정보를 인식하고, STDIO를 통한 로컬 jar 서브 프로세스를 실행시킴과 동시에 설정된 원격 SSE 접속을 시도하여 채널을 확보합니다.
2. 서버 단(`sample02-mcp-resources` 등)이 제공하는 `listResources()`를 호출해 현재 AI Context 확장에 사용할 외부 데이터가 무엇이 있는지 파악합니다.
3. 이를 바탕으로 `readResource(uri)` 를 사용하여 데이터를 얻고, 프롬프트나 답변 조합에 데이터를 추가해 반환할 수 있게 됩니다.
