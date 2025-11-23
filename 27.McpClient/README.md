# 27. MCP Client (Model Context Protocol)

## 📖 학습 목표

- **MCP (Model Context Protocol)**의 개념을 이해합니다
- **MCP Client**로 외부 서비스와 연동합니다
- **MCP Resources**와 **Tools**를 활용합니다
- **다양한 Transport** 방식을 학습합니다

---

## 🔑 핵심 키워드

1. **MCP** - Model Context Protocol
2. **MCP Client** - 외부 서비스 연동 클라이언트
3. **Resources** - 외부 데이터 소스
4. **Tools** - MCP를 통한 함수 호출
5. **Transport** - STDIO, HTTP, SSE

---

## 1. MCP란?

**MCP (Model Context Protocol)**는 AI 모델이 외부 서비스와 표준화된 방식으로 통신하는 프로토콜입니다.

### 주요 기능
- **Resources**: 외부 데이터 읽기
- **Tools**: 외부 함수 호출
- **Prompts**: 템플릿 관리

---

## 2. 샘플 구성

### Sample 01: Basic MCP Client
- MCP Client 기본 설정
- STDIO Transport
- **포트:** 9700

### Sample 02: MCP Resources
- 외부 리소스 조회
- Resource 읽기
- **포트:** 9701

### Sample 03: MCP Tools Integration
- MCP Tools 활용
- ChatClient 통합
- **포트:** 9702

---

## 3. MCP 구성 요소

```yaml
spring:
  ai:
    mcp:
      client:
        my-server:
          transport:
            stdio:
              command: "node"
              args: ["server.js"]
```

---

## 4. Transport 방식

| Transport | 특징 | 사용 시나리오 |
|-----------|------|---------------|
| **STDIO** | 프로세스 통신 | 로컬 서버 |
| **HTTP** | HTTP 통신 | 원격 서버 |
| **SSE** | Server-Sent Events | 실시간 스트리밍 |

---

## 5. 공통 설정

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
    mcp:
      client:
        enabled: true
```

---

**시작하기**: [Sample 01: Basic MCP Client](./sample01-basic-mcp/)
