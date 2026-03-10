# Sample 01: Basic MCP Server

## 📖 개요
이 프로젝트는 Spring AI의 MCP(Model Context Protocol) Server 로서 가장 기본적인 동작 방식을 보여주는 샘플입니다.
주로 클라이언트 어플리케이션(예: `27.McpClient/sample01-basic-mcp`)에서 요청을 받아 처리할 수 있는 통로와 기능을 제공합니다.

## 🛠️ 주요 소스 코드 분석

### 1. `application.yml` (설정)
```yaml
spring:
  ai:
    mcp:
      server:
        enabled: true
        name: "Basic MCP Server"
        version: "1.0.0"
```
- **의도**: Spring Boot 어플리케이션이 MCP Server 로 동작할 수 있도록 활성화(`enabled: true`)합니다. 서버의 이름과 버전을 명시하여 클라이언트가 정보를 식별할 수 있게 합니다.

### 2. `ServerToolProvider.kt` (Tools 제공)
```kotlin
@Configuration
class ServerToolProvider {
    @Bean
    @Description("Get the current server time")
    fun getServerTime(): (String) -> String {
        return { timezone ->
            "Server time in $timezone is ${LocalDateTime.now()}"
        }
    }
}
```
- **의도**: MCP Server가 클라이언트나 AI 모델에 노출할 "함수(Tool)"를 정의합니다.
- Spring의 `@Bean`과 `@Description` 어노테이션을 사용하여 `getServerTime` 함수를 정의했습니다. 이렇게 등록된 빈은 MCP 프로토콜을 통해 외부에서 도구(Tool)로 조회 및 호출이 가능해집니다.

### 3. `BasicMcpServerController.kt` (API 엔드포인트)
- REST API 서버로서 동작 상태를 점검하기 위한 단순한 Health Check 성격의 API(`/api/server/info`, `/api/server/status`)를 제공합니다.

## 🎯 어떻게 서비스되고 사용되는지 (흐름 파악)
1. **서버 시작**: 이 어플리케이션은 기본적으로 HTTP (포트 9800) 혹은 로컬 프로세스의 `stdio` 모드로 기동될 수 있습니다. `mcp-client` 에서는 `command: "java"`, `args: ["-jar", "...jar"]` 방식으로 이 서버를 서브 프로세스로 구동시킵니다.
2. **도구 노출(Capabilities)**: 어플리케이션 컨텍스트에 등록된 `@Bean` 함수 중 `@Description` 등 특정 조건을 만족하는 컴포넌트(`getServerTime`)가 MCP Tool 로 래핑되어 준비됩니다.
3. **요청 처리 (Execution)**: 외부 클라이언트가 Tool Name `getServerTime` 을 지정하여 `timezone` 매개변수와 함께 호출하면, 이 서버가 해당 요청을 받아 내부의 람다 함수 `(String) -> String` 를 실행 완료하고 결과 문자열을 클라이언트로 반환합니다.
