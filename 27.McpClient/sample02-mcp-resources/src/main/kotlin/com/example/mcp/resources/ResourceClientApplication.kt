package com.example.mcp.resources

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class ResourceClientApplication

fun main(args: Array<String>) {
    runApplication<ResourceClientApplication>(*args)
}
