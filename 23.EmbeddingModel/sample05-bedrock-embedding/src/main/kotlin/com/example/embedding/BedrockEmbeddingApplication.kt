package com.example.embedding

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class BedrockEmbeddingApplication

fun main(args: Array<String>) {
    runApplication<BedrockEmbeddingApplication>(*args)
}
