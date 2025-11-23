package com.example.function.config

import com.example.function.model.WeatherRequest
import com.example.function.model.WeatherResponse
import com.example.function.service.WeatherService
import java.util.function.Function
import org.springframework.ai.model.tool.Tool
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class WeatherFunctionConfig(private val weatherService: WeatherService) {

    @Bean
    @Tool(
            name = "getCurrentWeather",
            description =
                    "Get the current weather for a specific location. Supports celsius and fahrenheit units."
    )
    fun weatherFunction(): Function<WeatherRequest, WeatherResponse> {
        return Function { request -> weatherService.getWeather(request) }
    }
}
