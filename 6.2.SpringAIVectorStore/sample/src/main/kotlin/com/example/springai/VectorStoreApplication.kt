package com.example.springai

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class VectorStoreApplication

fun main(args: Array<String>) {
    runApplication<VectorStoreApplication>(*args)
}

