package com.example.function.model

data class WeatherRequest(val location: String, val unit: String = "celsius")

data class WeatherResponse(
        val location: String,
        val temperature: Double,
        val condition: String,
        val humidity: Int,
        val unit: String
)
