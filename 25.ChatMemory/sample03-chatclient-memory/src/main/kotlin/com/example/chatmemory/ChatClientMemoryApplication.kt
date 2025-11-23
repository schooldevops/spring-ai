package com.example.chatmemory

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class ChatClientMemoryApplication

fun main(args: Array<String>) {
    runApplication<ChatClientMemoryApplication>(*args)
}
