# Confluence 인증 문제 해결 가이드

## 401 Unauthorized 오류 해결 방법

### 1. application.yml 설정 확인

현재 설정:
```yaml
confluence:
  base-url: https://confluence.tde.sktelecom.com/wiki/rest/api
  username: 1111489@sk.com
  api-token: your-token
```

**문제**: base-url에 `/wiki/rest/api`가 포함되어 있어 URL이 중복됩니다.

**해결책**: base-url을 다음과 같이 수정하세요:

```yaml
confluence:
  base-url: https://confluence.tde.sktelecom.com/wiki/rest/api
  username: 1111489@sk.com
  api-token: your-token
  auth-type: bearer  # 또는 basic
```

### 2. 인증 방식 선택

#### Option 1: Bearer Token (자체 호스팅 Confluence)
```yaml
confluence:
  base-url: https://confluence.tde.sktelecom.com/wiki/rest/api
  username: 1111489@sk.com  # Bearer 방식에서는 사용 안 함
  api-token: your-personal-access-token
  auth-type: bearer
```

#### Option 2: Basic Authentication (Atlassian Cloud)
```yaml
confluence:
  base-url: https://confluence.tde.sktelecom.com/wiki/rest/api
  username: 1111489@sk.com
  api-token: your-api-token
  auth-type: basic
```

### 3. 토큰 확인 방법

SK Telecom Confluence에서 Personal Access Token 생성:
1. Confluence 로그인
2. 프로필 아이콘 클릭 → Settings
3. Personal Access Tokens 메뉴
4. Create token
5. 생성된 토큰을 `api-token`에 설정

### 4. 테스트 방법

```bash
# Bearer 토큰 테스트
curl -H "Authorization: Bearer your-token" \
  https://confluence.tde.sktelecom.com/wiki/rest/api/content/891779581

# Basic Auth 테스트
curl -u "1111489@sk.com:your-token" \
  https://confluence.tde.sktelecom.com/wiki/rest/api/content/891779581
```

### 5. 로그 확인

애플리케이션 로그에서 다음을 확인:
- 실제 요청 URL
- 인증 헤더 (처음 20자)
- 응답 코드

```
DEBUG - Fetching page: https://...
DEBUG - Using auth header: Bearer momv6e79h0c6...
ERROR - Failed to fetch page 891779581: 401
```

### 6. 권장 설정 (SK Telecom Confluence)

```yaml
confluence:
  base-url: https://confluence.tde.sktelecom.com/wiki/rest/api
  username: ""  # Bearer 방식에서는 비워둠
  api-token: momv6e79h0c63g22up62t8u0051rcrge489hie186dms34ssulllocs
  auth-type: bearer
```

### 7. 재시작

설정 변경 후 애플리케이션 재시작:
```bash
# 현재 실행 중인 애플리케이션 중지 (Ctrl+C)
# 재시작
./gradlew bootRun
```
