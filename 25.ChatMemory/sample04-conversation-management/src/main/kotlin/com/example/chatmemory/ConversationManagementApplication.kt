package com.example.chatmemory

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class ConversationManagementApplication

fun main(args: Array<String>) {
    runApplication<ConversationManagementApplication>(*args)
}
