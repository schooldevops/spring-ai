# Sample 04: MCP Prompts Provider

## 📖 개요
이 샘플은 MCP Server에서 **프롬프트 템플릿(Prompts)** 을 클라이언트 애플리케이션 및 AI 모델에게 제공하는 방법을 보여줍니다.
서버 측에 세션 상태를 유지하지 않는 완전한 무상태(Stateless) 기반으로 요청과 응답을 처리하는 구조로 설계되었습니다.

## 🛠️ 주요 소스 코드 분석

### 1. `application.yml` (설정)
```yaml
spring:
  ai:
    mcp:
      server:
        enabled: true
        name: "Stateless MCP Server"
        version: "1.0.0"
```
- 무상태 기반의 구동을 염두에 둔 서버입니다.
- 기본 포트 9803 으로 구동됩니다.

### 2. `PromptProvider.kt`
- 사전에 정의된 프롬프트 템플릿(Greeting, Farewell, Summary, Translate 등)을 서버 메모리 상에 가지고 있는 컴포넌트입니다.
- **`getPrompt(name, params)`**: 클라이언트로부터 템플릿 이름과 치환할 매개변수(`Map<String, String>`)를 전달받아, 해당 템플릿 내의 플레이스홀더(`{키}`)를 실제 값으로 치환하여 완성된 프롬프트 문자열을 반환합니다.
- **`listPrompts()`**: 사용 가능한 프롬프트 목록을 반환합니다.
- *참고: 상위 README 설계에 따르면 추후 `@McpPrompt` 어노테이션을 통해 관리될 역할의 클래스입니다.*

### 3. `StatelessController.kt` (API 엔드포인트)
- 무상태 서버 컨트롤러로서, 상태를 저장하지 않고 요청이 올 때마다 독립적으로 템플릿 조합 결과를 반환합니다.
- `GET /api/prompts`: 템플릿 목록 반환
- `POST /api/prompts/{name}`: 특정 템플릿에 파라미터를 주입하여 완성된 프롬프트 생성 요청

## 🎯 어떻게 서비스되고 사용되는지 (흐름 파악)
1. **프롬프트 중앙 관리**: 서비스에서 자주 사용되는 프롬프트 문자열들을 각각의 클라이언트나 AI 모델쪽 하드코딩으로 두지 않고, MCP Server 측에서 통합 관리합니다(`PromptProvider`).
2. **동적 생성 요청**: AI 로직을 구동해야 하는 클라이언트(`mcp-client`) 측에서, 어떤 작업을 수행할 때 필요한 지시문을 서버에 "이름"과 "동적 변수"를 전달하여 요청합니다.
3. **결과 활용**: 서버는 완성된 프롬프트를 내려주고, 클라이언트는 이 프롬프트를 바탕으로 실제 LLM(OpenAI 등) 에게 Chat Request를 날리게 됩니다.
4. 이 샘플은 별도의 세션을 맺지 않는 Stateless 구조를 지향하여 설계되었으므로, 서버 확장이 용이하고(Horizontal Scaling) 빠른 응답을 기대할 수 있습니다.
