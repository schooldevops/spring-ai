package com.example.image

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class AzureImageApplication

fun main(args: Array<String>) {
    runApplication<AzureImageApplication>(*args)
}
