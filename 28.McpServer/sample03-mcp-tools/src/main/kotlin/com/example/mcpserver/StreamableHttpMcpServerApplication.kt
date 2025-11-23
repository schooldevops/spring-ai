package com.example.mcpserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class StreamableHttpMcpServerApplication

fun main(args: Array<String>) {
    runApplication<StreamableHttpMcpServerApplication>(*args)
}
