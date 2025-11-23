package com.example.chatmodel

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class ChatModelStreamingApplication

fun main(args: Array<String>) {
    runApplication<ChatModelStreamingApplication>(*args)
}
