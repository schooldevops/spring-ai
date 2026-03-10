# Sample 02: MCP Resources Provider

## 📖 개요
이 샘플은 MCP Server에서 **외부 데이터 소스(Resources)** 를 클라이언트 모델이 읽을 수 있도록 노출하는 방법을 보여주기 위한 어플리케이션입니다.
STDIO 방식과 더불어 실시간 스트리밍을 위한 SSE(Server-Sent Events) Transport 개념을 포함합니다.

## 🛠️ 주요 소스 코드 분석

### 1. `application.yml` (설정)
```yaml
spring:
  ai:
    mcp:
      server:
        enabled: true
        name: "STDIO/SSE MCP Server"
        version: "1.0.0"
```
- MCP Server 기능을 활성화하고, 서버 이름을 `STDIO/SSE MCP Server` 로 지정합니다.
- 주석에 기재된 것처럼 로컬 프로세스 통신을 위한 STDIO 혹은 실시간 스트리밍 환경을 위한 SSE 전송 방식을 염두에 두고 설정되었습니다. 기본 실행 구동 포트는 9801 입니다.

### 2. `ResourceProvider.kt`
- 현재 `users`, `products` 등의 시뮬레이션 데이터를 하드코딩된 Map으로 들고 있는 `@Component` 객체입니다.
- 데이터를 반환하는 `getResource(name)` 함수와, 리소스 목록을 반환하는 `listResources()` 함수로 구성되어 있습니다.
- *참고: 상위 README 설계에 따르면 추후 `@McpResource` 어노테이션이 적용되어 AI 모델이 직접 인지할 수 있도록 확장될 것으로 보입니다.*

### 3. `StdioSseController.kt` (API 엔드포인트)
- REST 기반으로 리소스를 관리하고 상태를 체크할 수 있게 구성되었습니다.
- `/api/resources`: 전체 리소스 목록 반환
- `/api/resources/{name}`: 특정 리소스의 상세 데이터 반환

## 🎯 어떻게 서비스되고 사용되는지 (흐름 파악)
1. **데이터 준비**: `ResourceProvider` 내에 외부 리소스(파일, DB, 또는 메모리 데이터 등)를 읽어올 수 있는 로직을 구성합니다.
2. **호출 및 응답**: 외부 시스템이나 MCP 기반 AI 모델이 이 서버에 연결하여 필요한 데이터(예: User 데이터, Product 데이터) 목록을 확인하고, 리소스의 내용을 참조해 답변의 문맥(Context)을 풍부하게 만드는 데에 활용할 수 있습니다.
3. 이 샘플에서는 REST API(`StdioSseController`)를 통해 데이터에 대한 기본적인 CRUD/리딩을 테스트할 수 있도록 포장되어 있습니다.
