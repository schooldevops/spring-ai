package com.example.mcpserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class StockMcpServerApplication

fun main(args: Array<String>) {
    runApplication<StockMcpServerApplication>(*args)
}
