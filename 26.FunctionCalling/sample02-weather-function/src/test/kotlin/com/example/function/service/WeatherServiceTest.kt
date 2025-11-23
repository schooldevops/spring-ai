package com.example.function.service

import com.example.function.model.WeatherRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class WeatherServiceTest {

    @Autowired lateinit var weatherService: WeatherService

    @Test
    fun `should get weather for Seoul`() {
        val response = weatherService.getWeather(WeatherRequest("Seoul"))
        assertThat(response.location).isEqualTo("Seoul")
        assertThat(response.temperature).isGreaterThan(0.0)
    }
}
