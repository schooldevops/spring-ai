# ChatModel vs ChatClient 샘플 프로젝트

이 프로젝트는 Spring AI의 **ChatModel**과 **ChatClient**를 실제 코드로 비교하는 샘플입니다.

## 📁 프로젝트 구조

```
sample/
├── src/main/kotlin/com/example/springai/
│   ├── ChatModelVsChatClientApplication.kt  # 메인 애플리케이션
│   ├── config/
│   │   └── ChatClientConfig.kt              # ChatClient 설정
│   ├── controller/
│   │   ├── ChatModelController.kt           # ChatModel 예제
│   │   ├── ChatClientController.kt          # ChatClient 예제
│   │   └── ComparisonController.kt          # 직접 비교
│   └── dto/
│       └── DTOs.kt                          # 데이터 클래스
├── src/main/resources/
│   └── application.yml                      # 설정 파일
├── test.http                                # HTTP 테스트 파일
├── build.gradle.kts                         # Gradle 빌드 설정
└── README.md                                # 이 파일
```

## 🚀 실행 방법

### 1. 사전 준비

#### Ollama 설치 및 실행
```bash
# Ollama 실행
ollama serve

# 모델 다운로드 (다른 터미널에서)
ollama pull gemma2:2b
```

### 2. 애플리케이션 실행

```bash
# Gradle로 실행
./gradlew bootRun

# 또는 빌드 후 실행
./gradlew build
java -jar build/libs/chatmodel-vs-chatclient-0.0.1-SNAPSHOT.jar
```

### 3. 테스트

`test.http` 파일을 열고 IntelliJ IDEA 또는 VSCode의 REST Client 플러그인으로 요청을 실행하세요.

## 📚 주요 엔드포인트

### ChatModel 엔드포인트

| 엔드포인트 | 설명 |
|-----------|------|
| `POST /api/chatmodel/basic` | 기본 질의응답 |
| `POST /api/chatmodel/with-system` | System Message 포함 |
| `POST /api/chatmodel/expert` | 도메인 전문가 |
| `POST /api/chatmodel/book-recommendation` | Entity 매핑 (수동) |
| `POST /api/chatmodel/with-metadata` | 메타데이터 포함 |
| `GET /api/chatmodel/stream` | 스트리밍 응답 |
| `POST /api/chatmodel/conversation` | 대화 히스토리 |

### ChatClient 엔드포인트

| 엔드포인트 | 설명 |
|-----------|------|
| `POST /api/chatclient/basic` | 기본 질의응답 |
| `POST /api/chatclient/with-system` | System Message 포함 |
| `POST /api/chatclient/expert` | 도메인 전문가 |
| `POST /api/chatclient/book-recommendation` | Entity 매핑 (자동) |
| `POST /api/chatclient/with-metadata` | 메타데이터 포함 |
| `GET /api/chatclient/stream` | 스트리밍 응답 |
| `POST /api/chatclient/translate` | 템플릿 변수 |
| `POST /api/chatclient/movie-recommendation` | 영화 추천 |
| `POST /api/chatclient/extract-product` | 제품 정보 추출 |

### 비교 엔드포인트

| 엔드포인트 | 설명 |
|-----------|------|
| `POST /api/comparison/basic/chatmodel` | ChatModel 기본 (성능 측정) |
| `POST /api/comparison/basic/chatclient` | ChatClient 기본 (성능 측정) |
| `GET /api/comparison/complexity` | 코드 복잡도 비교 |
| `GET /api/comparison/recommendations` | 사용 권장사항 |

## 🔍 코드 비교 예제

### 예제 1: 기본 질의응답

**ChatModel 방식 (3줄)**
```kotlin
val prompt = Prompt(UserMessage(request.question))
val response = chatModel.call(prompt)
return response.result.output.content
```

**ChatClient 방식 (1줄)**
```kotlin
return chatClient.prompt(request.question).call().content()
```

### 예제 2: Entity 매핑

**ChatModel 방식 (수동 파싱)**
```kotlin
val response = chatModel.call(prompt)
val json = response.result.output.content
return objectMapper.readValue(json, BookRecommendation::class.java)
```

**ChatClient 방식 (자동 변환)**
```kotlin
return chatClient.prompt()
    .user("Recommend a book")
    .call()
    .entity(BookRecommendation::class.java)
```

## 📊 주요 차이점

| 항목 | ChatModel | ChatClient |
|-----|-----------|------------|
| 코드 길이 | 길다 | 짧다 |
| 가독성 | 보통 | 우수 |
| Entity 매핑 | 수동 | 자동 |
| 템플릿 | 별도 처리 | 내장 |
| 학습 곡선 | 높음 | 낮음 |
| 제어 수준 | 매우 높음 | 높음 |

## 💡 학습 가이드

1. **ChatModelController.kt** 먼저 읽기
   - ChatModel의 기본 사용법 이해
   - Prompt, Message 개념 파악
   - 응답 추출 방법 학습

2. **ChatClientController.kt** 읽기
   - ChatClient의 Fluent API 이해
   - 메서드 체이닝 패턴 학습
   - 자동 변환 기능 확인

3. **ComparisonController.kt** 비교
   - 동일 기능의 두 가지 구현 비교
   - 코드 복잡도 차이 확인
   - 사용 시나리오별 권장사항 확인

4. **test.http** 실행
   - 실제 API 호출 테스트
   - 응답 시간 비교
   - 결과 확인

## 🎯 권장 사항

### ChatModel을 사용하세요:
- ✅ 복잡한 대화 히스토리 직접 관리
- ✅ 토큰 사용량 정밀 모니터링
- ✅ 커스텀 메시지 타입 필요
- ✅ 메시지 리스트 동적 조작

### ChatClient를 사용하세요:
- ✅ 일반적인 질의응답
- ✅ 빠른 프로토타이핑
- ✅ Entity 매핑 필요
- ✅ 템플릿 기반 프롬프트
- ✅ RAG, Memory 등 Advisor 활용
- ✅ 코드 가독성 중요

**결론: 대부분의 경우 ChatClient를 사용하세요!**

## 🔧 문제 해결

### Ollama 연결 실패
```
Error: Connection refused to localhost:11434
```
**해결:** `ollama serve` 명령으로 Ollama 서버를 먼저 실행하세요.

### 모델을 찾을 수 없음
```
Error: model 'gemma2:2b' not found
```
**해결:** `ollama pull gemma2:2b` 명령으로 모델을 다운로드하세요.

### Entity 매핑 실패
**해결:** AI 응답이 JSON 형식이 아닐 수 있습니다. System Message에 JSON 형식 요청을 명시하세요.

## 📖 참고 자료

- [상위 README.md](../README.md) - 상세한 개념 설명
- [Spring AI 공식 문서](https://docs.spring.io/spring-ai/reference/)
- [ChatModel API](https://docs.spring.io/spring-ai/reference/api/chatmodel.html)
- [ChatClient API](https://docs.spring.io/spring-ai/reference/api/chatclient.html)
