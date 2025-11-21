# 9.2: Function Calling을 통한 외부 API 연동

## 📖 학습 목표

이 강의를 마친 후 다음을 달성할 수 있습니다:
- **외부 API 연동**을 Function Calling으로 구현할 수 있습니다
- '날씨 묻기', '주문 상태 확인' 등 LLM이 직접 처리할 수 없는 요청을 Function Calling으로 처리할 수 있습니다
- **java.util.function.Function**을 사용하여 외부 서비스를 호출하는 함수를 정의할 수 있습니다
- **@Description** 어노테이션으로 외부 API 함수를 명확히 설명할 수 있습니다
- 외부 API 호출 시 **에러 처리**와 **타임아웃**을 적절히 처리할 수 있습니다
- Mock 서비스를 활용하여 외부 API 연동을 테스트할 수 있습니다

---

## 🔑 핵심 키워드

이 장에서 다루는 핵심 키워드들:

1. **java.util.function.Function** - 외부 API를 호출하는 함수 정의
2. **@Description** - 외부 API 함수의 목적과 사용법을 설명하는 어노테이션
3. **API 통합** - 외부 REST API와의 통합
4. **RestTemplate / WebClient** - HTTP 클라이언트를 통한 외부 API 호출
5. **에러 처리** - 외부 API 호출 시 발생할 수 있는 오류 처리
6. **Mock 서비스** - 외부 API를 시뮬레이션하는 테스트용 서비스

---

## 1. 외부 API 연동이 필요한 이유

### 1.1 LLM의 한계

LLM은 학습 데이터에 기반하여 응답하므로, 다음과 같은 정보는 제공할 수 없습니다:
- **실시간 데이터**: 현재 날씨, 주식 가격, 뉴스 등
- **개인화된 정보**: 사용자별 주문 상태, 계정 정보 등
- **동적 데이터**: 실시간 재고, 예약 가능 여부 등

### 1.2 Function Calling을 통한 해결

Function Calling을 사용하면:
- LLM이 외부 API를 호출하여 최신 정보를 가져올 수 있습니다
- 사용자별 정보를 조회할 수 있습니다
- 실시간 데이터를 기반으로 응답할 수 있습니다

---

## 2. 외부 API 연동 구현하기

### 2.1 기본 구조

외부 API 연동을 위한 Function Calling 구현 단계:

1. **외부 API 클라이언트**: RestTemplate 또는 WebClient 사용
2. **함수 정의**: `java.util.function.Function`으로 API 호출 로직 구현
3. **에러 처리**: API 호출 실패 시 적절한 처리
4. **Bean 등록**: `@Bean`과 `@Description`으로 함수 등록

### 2.2 날씨 API 연동 예제

#### 날씨 API 함수 정의

```kotlin
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Description
import org.springframework.web.client.RestTemplate
import java.util.function.Function

@Configuration
class WeatherFunctionConfig(
    private val restTemplate: RestTemplate
) {
    
    @Bean
    @Description("특정 도시의 현재 날씨 정보를 조회합니다. location은 도시 이름입니다.")
    fun getWeatherFunction(): Function<WeatherRequest, WeatherResponse> {
        return Function { request ->
            try {
                // 외부 날씨 API 호출 (예: OpenWeatherMap)
                val url = "https://api.openweathermap.org/data/2.5/weather?q=${request.location}&appid=${apiKey}"
                val response = restTemplate.getForObject(url, WeatherApiResponse::class.java)
                
                WeatherResponse(
                    location = request.location,
                    temperature = response?.main?.temp ?: 0.0,
                    description = response?.weather?.firstOrNull()?.description ?: "정보 없음",
                    humidity = response?.main?.humidity ?: 0
                )
            } catch (e: Exception) {
                // 에러 처리
                WeatherResponse(
                    location = request.location,
                    temperature = 0.0,
                    description = "날씨 정보를 가져올 수 없습니다: ${e.message}",
                    humidity = 0
                )
            }
        }
    }
}

data class WeatherRequest(
    val location: String
)

data class WeatherResponse(
    val location: String,
    val temperature: Double,
    val description: String,
    val humidity: Int
)
```

### 2.3 주문 상태 조회 예제

#### 주문 상태 조회 함수 정의

```kotlin
@Configuration
class OrderFunctionConfig(
    private val orderService: OrderService
) {
    
    @Bean
    @Description("주문 ID로 주문 상태를 조회합니다. orderId는 주문 번호입니다.")
    fun getOrderStatusFunction(): Function<OrderStatusRequest, OrderStatusResponse> {
        return Function { request ->
            try {
                val order = orderService.getOrderById(request.orderId)
                
                OrderStatusResponse(
                    orderId = request.orderId,
                    status = order?.status ?: "NOT_FOUND",
                    items = order?.items ?: emptyList(),
                    totalAmount = order?.totalAmount ?: 0.0
                )
            } catch (e: Exception) {
                OrderStatusResponse(
                    orderId = request.orderId,
                    status = "ERROR",
                    items = emptyList(),
                    totalAmount = 0.0,
                    error = e.message
                )
            }
        }
    }
}

data class OrderStatusRequest(
    val orderId: String
)

data class OrderStatusResponse(
    val orderId: String,
    val status: String,
    val items: List<String>,
    val totalAmount: Double,
    val error: String? = null
)
```

---

## 3. RestTemplate을 사용한 외부 API 호출

### 3.1 RestTemplate 설정

```kotlin
@Configuration
class RestTemplateConfig {
    
    @Bean
    fun restTemplate(): RestTemplate {
        val restTemplate = RestTemplate()
        // 타임아웃 설정
        val requestFactory = HttpComponentsClientHttpRequestFactory()
        requestFactory.setConnectTimeout(5000) // 5초
        requestFactory.setReadTimeout(5000) // 5초
        restTemplate.requestFactory = requestFactory
        return restTemplate
    }
}
```

### 3.2 외부 API 호출 예제

```kotlin
@Service
class WeatherApiService(
    private val restTemplate: RestTemplate
) {
    fun getWeather(location: String): WeatherData {
        val url = "https://api.example.com/weather?location=$location"
        return restTemplate.getForObject(url, WeatherData::class.java)
            ?: throw RuntimeException("날씨 정보를 가져올 수 없습니다")
    }
}
```

---

## 4. WebClient를 사용한 외부 API 호출

### 4.1 WebClient 설정

```kotlin
@Configuration
class WebClientConfig {
    
    @Bean
    fun webClient(): WebClient {
        return WebClient.builder()
            .baseUrl("https://api.example.com")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
    }
}
```

### 4.2 비동기 API 호출 예제

```kotlin
@Service
class WeatherApiService(
    private val webClient: WebClient
) {
    fun getWeather(location: String): Mono<WeatherData> {
        return webClient.get()
            .uri("/weather?location={location}", location)
            .retrieve()
            .bodyToMono(WeatherData::class.java)
            .timeout(Duration.ofSeconds(5))
            .onErrorReturn(WeatherData(location = location, error = "API 호출 실패"))
    }
}
```

---

## 5. 실제 사용 예제

### 5.1 날씨 조회 함수 예제

#### Mock 날씨 서비스 구현

```kotlin
@Service
class MockWeatherService {
    private val weatherData = mapOf(
        "서울" to WeatherData("서울", 15.0, "맑음", 60),
        "부산" to WeatherData("부산", 18.0, "흐림", 70),
        "제주" to WeatherData("제주", 20.0, "맑음", 65)
    )
    
    fun getWeather(location: String): WeatherData {
        return weatherData[location] 
            ?: WeatherData(location, 0.0, "정보 없음", 0)
    }
}
```

#### Function 정의

```kotlin
@Configuration
class WeatherFunctionConfig(
    private val weatherService: MockWeatherService
) {
    
    @Bean
    @Description("특정 도시의 현재 날씨 정보를 조회합니다. location은 도시 이름입니다 (예: 서울, 부산, 제주).")
    fun getWeatherFunction(): Function<WeatherRequest, WeatherResponse> {
        return Function { request ->
            val weather = weatherService.getWeather(request.location)
            
            WeatherResponse(
                location = weather.location,
                temperature = weather.temperature,
                description = weather.description,
                humidity = weather.humidity
            )
        }
    }
}
```

### 5.2 주문 상태 조회 함수 예제

#### Mock 주문 서비스 구현

```kotlin
@Service
class MockOrderService {
    private val orders = mapOf(
        "ORD-001" to Order("ORD-001", "배송중", listOf("노트북", "마우스"), 1500000.0),
        "ORD-002" to Order("ORD-002", "주문완료", listOf("키보드"), 150000.0),
        "ORD-003" to Order("ORD-003", "배송완료", listOf("모니터"), 300000.0)
    )
    
    fun getOrderById(orderId: String): Order? {
        return orders[orderId]
    }
}
```

#### Function 정의

```kotlin
@Configuration
class OrderFunctionConfig(
    private val orderService: MockOrderService
) {
    
    @Bean
    @Description("주문 ID로 주문 상태를 조회합니다. orderId는 주문 번호입니다 (예: ORD-001).")
    fun getOrderStatusFunction(): Function<OrderStatusRequest, OrderStatusResponse> {
        return Function { request ->
            val order = orderService.getOrderById(request.orderId)
            
            if (order == null) {
                OrderStatusResponse(
                    orderId = request.orderId,
                    status = "NOT_FOUND",
                    items = emptyList(),
                    totalAmount = 0.0,
                    error = "주문을 찾을 수 없습니다"
                )
            } else {
                OrderStatusResponse(
                    orderId = order.orderId,
                    status = order.status,
                    items = order.items,
                    totalAmount = order.totalAmount
                )
            }
        }
    }
}
```

---

## 6. 에러 처리 및 모범 사례

### 6.1 에러 처리 패턴

```kotlin
@Bean
@Description("외부 API 호출 함수")
fun externalApiFunction(): Function<Request, Response> {
    return Function { request ->
        try {
            // API 호출
            val result = apiService.call(request)
            Response(success = true, data = result)
        } catch (e: HttpServerErrorException) {
            // 서버 오류 (5xx)
            Response(success = false, error = "서버 오류: ${e.statusCode}")
        } catch (e: HttpClientErrorException) {
            // 클라이언트 오류 (4xx)
            Response(success = false, error = "요청 오류: ${e.statusCode}")
        } catch (e: ResourceAccessException) {
            // 네트워크 오류
            Response(success = false, error = "네트워크 오류: ${e.message}")
        } catch (e: Exception) {
            // 기타 오류
            Response(success = false, error = "오류 발생: ${e.message}")
        }
    }
}
```

### 6.2 타임아웃 설정

```kotlin
@Bean
fun restTemplate(): RestTemplate {
    val restTemplate = RestTemplate()
    val requestFactory = HttpComponentsClientHttpRequestFactory()
    requestFactory.setConnectTimeout(3000) // 연결 타임아웃 3초
    requestFactory.setReadTimeout(5000) // 읽기 타임아웃 5초
    restTemplate.requestFactory = requestFactory
    return restTemplate
}
```

### 6.3 재시도 로직

```kotlin
@Service
class RetryableApiService(
    private val restTemplate: RestTemplate
) {
    fun callWithRetry(url: String, maxRetries: Int = 3): String {
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return restTemplate.getForObject(url, String::class.java) ?: ""
            } catch (e: Exception) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    Thread.sleep(1000 * (attempt + 1)) // 지수 백오프
                }
            }
        }
        
        throw RuntimeException("API 호출 실패 (재시도 ${maxRetries}회)", lastException)
    }
}
```

---

## 7. 실제 사용 예제

### 7.1 날씨 조회 API 사용

```kotlin
@RestController
class WeatherController(
    private val functionCallService: FunctionCallService
) {
    
    @PostMapping("/api/weather")
    fun getWeather(@RequestBody request: Map<String, String>): Map<String, Any> {
        val userMessage = request["message"] ?: return mapOf("error" to "Message is required")
        
        val response = functionCallService.callWithWeatherFunction(userMessage)
        
        return mapOf(
            "userMessage" to userMessage,
            "aiResponse" to response
        )
    }
}
```

### 7.2 주문 상태 조회 API 사용

```kotlin
@RestController
class OrderController(
    private val functionCallService: FunctionCallService
) {
    
    @PostMapping("/api/order")
    fun getOrderStatus(@RequestBody request: Map<String, String>): Map<String, Any> {
        val userMessage = request["message"] ?: return mapOf("error" to "Message is required")
        
        val response = functionCallService.callWithOrderFunction(userMessage)
        
        return mapOf(
            "userMessage" to userMessage,
            "aiResponse" to response
        )
    }
}
```

---

## 8. 주의사항 및 모범 사례

### 8.1 보안 고려사항

1. **API 키 관리**: 환경 변수나 설정 파일로 관리
2. **입력 검증**: 사용자 입력을 검증하여 SQL Injection, XSS 등 방지
3. **Rate Limiting**: API 호출 빈도 제한
4. **HTTPS 사용**: 외부 API 호출 시 HTTPS 사용

### 8.2 성능 고려사항

1. **캐싱**: 자주 조회되는 데이터는 캐싱
2. **비동기 처리**: 가능한 경우 비동기 처리
3. **타임아웃 설정**: 적절한 타임아웃 설정
4. **연결 풀링**: HTTP 클라이언트 연결 풀 사용

### 8.3 모범 사례

1. **에러 처리**: 모든 예외 상황 처리
2. **로깅**: API 호출 및 오류 로깅
3. **테스트**: Mock 서비스를 활용한 테스트
4. **문서화**: API 함수의 목적과 사용법 명확히 문서화

---

## 9. 트러블슈팅

### 9.1 일반적인 문제들

#### 문제 1: 외부 API 호출 실패

**증상**: API 호출 시 예외 발생

**원인**:
- 네트워크 연결 문제
- API 서버 다운
- 잘못된 URL 또는 파라미터

**해결책**:
- 타임아웃 설정 확인
- 에러 처리 로직 추가
- API 서버 상태 확인

#### 문제 2: 응답 파싱 오류

**증상**: API 응답을 파싱할 수 없음

**원인**: 응답 형식이 예상과 다름

**해결책**:
- 응답 형식 확인
- 예외 처리 추가
- 로깅을 통한 디버깅

#### 문제 3: 타임아웃 발생

**증상**: API 호출이 타임아웃됨

**원인**: 응답 시간이 너무 김

**해결책**:
- 타임아웃 시간 조정
- 재시도 로직 추가
- 비동기 처리 고려

---

## 10. 요약

### 10.1 핵심 내용 정리

1. **외부 API 연동**: Function Calling을 통해 외부 API를 호출하여 실시간 데이터 제공
2. **RestTemplate/WebClient**: HTTP 클라이언트를 통한 외부 API 호출
3. **에러 처리**: API 호출 실패 시 적절한 에러 처리
4. **Mock 서비스**: 테스트를 위한 Mock 서비스 구현
5. **타임아웃 설정**: 적절한 타임아웃 설정으로 안정성 확보

### 10.2 구현 패턴

```kotlin
// 1. 외부 API 서비스 구현
@Service
class ExternalApiService {
    fun callApi(request: Request): Response {
        // API 호출 로직
    }
}

// 2. Function 정의
@Bean
@Description("외부 API 호출 함수 설명")
fun externalApiFunction(
    apiService: ExternalApiService
): Function<Request, Response> {
    return Function { request ->
        try {
            apiService.callApi(request)
        } catch (e: Exception) {
            // 에러 처리
            Response(error = e.message)
        }
    }
}

// 3. Function Calling 사용
val prompt = Prompt(UserMessage("사용자 요청"))
val response = chatModel.call(prompt)
```

### 10.3 다음 학습 내용

이제 외부 API 연동을 배웠으니, 다음 장에서는:
- **멀티모달**: 이미지와 텍스트를 함께 처리하는 방법
- **고급 Function Calling**: 여러 함수를 조합하여 사용하는 방법

---

## 📚 참고 자료

- [Spring AI Function Calling 공식 문서](https://docs.spring.io/spring-ai/reference/api/function-calling.html)
- [Spring RestTemplate 가이드](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html)
- [Spring WebClient 가이드](https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html)

---

## ❓ 학습 확인 문제

다음 질문에 답할 수 있는지 확인해보세요:

1. 외부 API 연동이 필요한 이유는 무엇인가요?
2. Function Calling으로 외부 API를 호출하는 방법은?
3. RestTemplate과 WebClient의 차이점은 무엇인가요?
4. 외부 API 호출 시 에러 처리는 어떻게 해야 하나요?
5. Mock 서비스를 사용하는 이유는 무엇인가요?

---

**다음 장**: [10장: 멀티모달 (Multi-modality)](../README.md#10장-멀티모달-multi-modality)

