package com.example.function

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class WeatherFunctionApplication

fun main(args: Array<String>) {
    runApplication<WeatherFunctionApplication>(*args)
}
