package com.example.mcp.tools

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class ToolClientApplication

fun main(args: Array<String>) {
    runApplication<ToolClientApplication>(*args)
}
