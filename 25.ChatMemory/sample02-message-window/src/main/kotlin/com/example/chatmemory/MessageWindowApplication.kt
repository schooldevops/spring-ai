package com.example.chatmemory

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class MessageWindowApplication

fun main(args: Array<String>) {
    runApplication<MessageWindowApplication>(*args)
}
