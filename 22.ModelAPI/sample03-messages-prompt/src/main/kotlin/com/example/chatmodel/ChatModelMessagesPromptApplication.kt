package com.example.chatmodel

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class ChatModelMessagesPromptApplication

fun main(args: Array<String>) {
    runApplication<ChatModelMessagesPromptApplication>(*args)
}
