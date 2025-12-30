package com.example.springai

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class VectorDBApplication

fun main(args: Array<String>) {
    runApplication<VectorDBApplication>(*args)
}

