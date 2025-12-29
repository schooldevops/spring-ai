# Thought Tag 제거 기능 추가

## 개요
Ollama와 같은 일부 LLM 모델은 응답에 `<thought>` 태그를 포함하여 사고 과정을 표시합니다. 이러한 태그는 파싱 과정에서 문제를 일으킬 수 있으므로, 모든 파서에서 자동으로 제거하도록 구현했습니다.

## 구현 내용

### 1. **TextCleanupUtil.kt** (새로 생성)
- **위치**: `src/main/kotlin/com/example/springai/util/TextCleanupUtil.kt`
- **기능**: LLM 응답에서 불필요한 태그 제거
- **주요 메서드**:
  - `removeThoughtTags(text: String)`: `<thought>...</thought>` 태그와 내용을 제거
  - `cleanupResponse(text: String)`: 여러 정리 작업을 한 번에 수행

**특징**:
- 대소문자 구분 없이 처리 (`<thought>`, `<THOUGHT>`, `<Thought>` 모두 제거)
- 여러 줄에 걸친 태그도 처리 가능
- 정규표현식 사용: `<thought>.*?</thought>`

### 2. **ListOutputParser.kt** (수정)
- `parse()` 메서드에서 파싱 전에 `TextCleanupUtil.cleanupResponse()` 호출
- 깨끗한 텍스트로 리스트 파싱 수행

### 3. **MapOutputParser.kt** (수정)
- `parse()` 메서드에서 파싱 전에 `TextCleanupUtil.cleanupResponse()` 호출
- 깨끗한 텍스트로 맵 파싱 수행

### 4. **TextCleanupUtilTest.kt** (새로 생성)
- **위치**: `src/test/kotlin/com/example/springai/util/TextCleanupUtilTest.kt`
- **테스트 케이스**:
  1. 기본 thought 태그 제거
  2. 여러 줄에 걸친 thought 태그 처리
  3. 대소문자 구분 없는 태그 제거
  4. thought 태그가 없는 경우 원본 유지

## 사용 예시

### Before (태그 포함)
```
<thought>Let me think about programming languages...</thought>
Python: 간결하고 읽기 쉬운 문법
Java: 강력한 타입 시스템
<thought>I should add more</thought>
JavaScript: 웹 개발의 필수 언어
```

### After (태그 제거)
```
Python: 간결하고 읽기 쉬운 문법
Java: 강력한 타입 시스템
JavaScript: 웹 개발의 필수 언어
```

## 영향받는 컨트롤러

모든 파서를 사용하는 컨트롤러에서 자동으로 적용됩니다:

1. **MapParserController** - 맵 파싱 시 자동 적용
2. **ListParserController** - 리스트 파싱 시 자동 적용
3. **ComplexParserController** - 복합 파싱 시 자동 적용
4. **ListMapClientController** - ChatClient 사용 시에도 파서를 통해 자동 적용

## 테스트 실행

```bash
./gradlew test --tests "com.example.springai.util.TextCleanupUtilTest"
```

**결과**: ✅ 모든 테스트 통과 (4/4)

## 빌드 확인

```bash
./gradlew compileKotlin
```

**결과**: ✅ BUILD SUCCESSFUL

## 주의사항

- 파서를 거치지 않고 직접 LLM 응답을 사용하는 경우, 수동으로 `TextCleanupUtil.cleanupResponse()`를 호출해야 합니다.
- 현재는 `<thought>` 태그만 제거하지만, 필요시 다른 태그도 추가할 수 있습니다.
