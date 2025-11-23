# 24. ImageModel API

## 📖 학습 목표

- **ImageModel** 인터페이스로 AI 이미지 생성을 이해합니다
- **다양한 프로바이더**의 이미지 생성 모델을 활용합니다
- **ImagePrompt**와 **ImageOptions**로 이미지 생성을 제어합니다
- **이미지 저장 및 처리** 방법을 학습합니다

---

## 🔑 핵심 키워드

1. **ImageModel** - AI 이미지 생성 인터페이스
2. **ImagePrompt** - 이미지 생성 요청
3. **ImageResponse** - 생성된 이미지 응답
4. **ImageOptions** - 크기, 품질, 스타일 등 옵션
5. **Image Generation** - DALL-E, Stable Diffusion 등

---

## 1. ImageModel이란?

**ImageModel**은 텍스트 프롬프트로 이미지를 생성하는 AI 모델 인터페이스입니다.

```kotlin
interface ImageModel {
    fun call(prompt: ImagePrompt): ImageResponse
}
```

---

## 2. 샘플 구성

### Sample 01: OpenAI DALL-E
- DALL-E 3 이미지 생성
- 다양한 크기 및 품질 옵션
- **포트:** 9400

### Sample 02: Azure OpenAI
- Azure의 DALL-E 모델
- 엔터프라이즈 환경
- **포트:** 9401

### Sample 03: Stability AI
- Stable Diffusion 모델
- 고품질 이미지 생성
- **포트:** 9402

### Sample 04: ZhiPuAI
- 중국 AI 프로바이더
- CogView 모델
- **포트:** 9403

### Sample 05: QianFan (Baidu)
- Baidu AI 플랫폼
- 중국 시장 특화
- **포트:** 9404

---

## 3. ImageOptions

```kotlin
val options = OpenAiImageOptions.builder()
    .withModel("dall-e-3")
    .withWidth(1024)
    .withHeight(1024)
    .withQuality("hd")
    .withStyle("vivid")
    .build()
```

---

## 4. 프로바이더 비교

| Provider | Model | Max Size | 특징 |
|----------|-------|----------|------|
| OpenAI | DALL-E 3 | 1024x1792 | 고품질, 다양한 스타일 |
| Azure | DALL-E 3 | 1024x1792 | 엔터프라이즈 |
| Stability | SD XL | 1024x1024 | 오픈소스, 커스터마이징 |
| ZhiPuAI | CogView | 1024x1024 | 중국 시장 |
| QianFan | ERNIE-ViLG | 1024x1024 | Baidu 생태계 |

---

## 5. 공통 설정

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      image:
        options:
          model: dall-e-3
```

---

**시작하기**: [Sample 01: OpenAI Image Generation](./sample01-openai-image/)
