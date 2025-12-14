# Confluence Document Summarizer

Spring AI를 활용하여 Confluence 문서를 자동으로 요약하는 애플리케이션입니다.

## 주요 기능

- **Confluence API 연동**: REST API를 통한 문서 조회
- **날짜 필터링**: 특정 기간 내 업데이트된 문서만 선택
- **계층 구조 탐색**: 페이지의 하위 문서 재귀적 탐색
- **AI 요약**: Spring AI ChatClient를 사용한 자동 요약
- **배치 처리**: 여러 문서를 효율적으로 처리

## 기술 스택

- Spring Boot 3.3.0
- Spring AI (Ollama)
- Kotlin
- OkHttp (HTTP 클라이언트)
- Jsoup (HTML 파싱)

## 사전 준비

### 1. Confluence API 토큰 발급

1. Atlassian 계정 설정 페이지 접속: https://id.atlassian.com/manage-profile/security/api-tokens
2. "Create API token" 클릭
3. 토큰 이름 입력 후 생성
4. 생성된 토큰 복사 (한 번만 표시됨)

### 2. Ollama 설치 및 모델 다운로드

```bash
# Ollama 설치 (macOS)
brew install ollama

# Ollama 서버 시작
ollama serve

# 모델 다운로드
ollama pull llama3.2
```

## 설정

### application.yml

```yaml
confluence:
  base-url: https://your-domain.atlassian.net
  username: your-email@example.com
  api-token: your-api-token-here
```

또는 환경 변수로 설정:

```bash
export CONFLUENCE_BASE_URL=https://your-domain.atlassian.net
export CONFLUENCE_USERNAME=your-email@example.com
export CONFLUENCE_API_TOKEN=your-api-token
```

## 실행

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun
```

## API 사용법

### 페이지 트리 요약

```bash
curl -X POST http://localhost:9000/api/summary/tree \
  -H "Content-Type: application/json" \
  -d '{
    "pageId": "123456",
    "startDate": "2024-01-01",
    "endDate": "2024-12-31",
    "includeChildren": true,
    "maxDepth": 3
  }'
```

### URL로 요약

```bash
curl -X POST http://localhost:9000/api/summary/tree \
  -H "Content-Type: application/json" \
  -d '{
    "pageUrl": "https://your-domain.atlassian.net/wiki/spaces/SPACE/pages/123456/Page+Title",
    "startDate": "2024-01-01",
    "endDate": "2024-12-31",
    "includeChildren": true
  }'
```

## 요청 파라미터

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `pageId` | String | 조건부 | Confluence 페이지 ID |
| `pageUrl` | String | 조건부 | Confluence 페이지 URL |
| `startDate` | LocalDate | 필수 | 시작 날짜 (YYYY-MM-DD) |
| `endDate` | LocalDate | 필수 | 종료 날짜 (YYYY-MM-DD) |
| `includeChildren` | Boolean | 선택 | 하위 페이지 포함 여부 (기본: true) |
| `maxDepth` | Int | 선택 | 최대 탐색 깊이 (기본: 3) |

> **참고**: `pageId` 또는 `pageUrl` 중 하나는 반드시 제공해야 합니다.

## 응답 예시

```json
{
  "totalPages": 5,
  "summaries": [
    {
      "pageId": "123456",
      "title": "프로젝트 개요",
      "url": "https://your-domain.atlassian.net/wiki/pages/viewpage.action?pageId=123456",
      "lastModified": "2024-12-10T10:30:00",
      "summary": "이 문서는 프로젝트의 전반적인 개요를 설명합니다. 주요 목표는 사용자 경험 개선과 성능 최적화입니다. 팀 구성원은 5명이며, 예상 완료일은 2025년 1월입니다.",
      "wordCount": 1250
    }
  ],
  "dateRange": {
    "start": "2024-01-01",
    "end": "2024-12-31"
  },
  "processingTimeMs": 5432
}
```

## 주의사항

1. **API 제한**: Confluence API에는 요청 제한이 있습니다. 대량의 페이지를 처리할 때 주의하세요.
2. **토큰 보안**: API 토큰은 절대 코드에 하드코딩하지 마세요.
3. **콘텐츠 길이**: 매우 긴 문서는 자동으로 잘려서 요약됩니다 (4000자 제한).
4. **네트워크**: Confluence 서버와의 안정적인 네트워크 연결이 필요합니다.

## 트러블슈팅

### 401 Unauthorized

- Confluence 사용자명과 API 토큰을 확인하세요
- API 토큰이 올바르게 설정되었는지 확인하세요

### 404 Not Found

- 페이지 ID가 올바른지 확인하세요
- 페이지에 대한 접근 권한이 있는지 확인하세요

### Connection Timeout

- Confluence 서버 URL이 올바른지 확인하세요
- 네트워크 연결을 확인하세요
- 방화벽 설정을 확인하세요

## 라이선스

MIT License
