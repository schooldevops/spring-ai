package com.example.function.service

import com.example.function.model.WeatherRequest
import com.example.function.model.WeatherResponse
import kotlin.random.Random
import org.springframework.stereotype.Service

@Service
class WeatherService {

    private val weatherData =
            mapOf(
                    "seoul" to Triple(15.0, "Partly Cloudy", 65),
                    "busan" to Triple(18.0, "Sunny", 70),
                    "jeju" to Triple(20.0, "Rainy", 80),
                    "incheon" to Triple(14.0, "Cloudy", 60)
            )

    fun getWeather(request: WeatherRequest): WeatherResponse {
        val locationKey = request.location.lowercase()
        val (temp, condition, humidity) =
                weatherData[locationKey]
                        ?: Triple(Random.nextDouble(10.0, 25.0), "Clear", Random.nextInt(50, 90))

        val temperature =
                if (request.unit == "fahrenheit") {
                    temp * 9 / 5 + 32
                } else {
                    temp
                }

        return WeatherResponse(
                location = request.location,
                temperature = temperature,
                condition = condition,
                humidity = humidity,
                unit = request.unit
        )
    }
}
