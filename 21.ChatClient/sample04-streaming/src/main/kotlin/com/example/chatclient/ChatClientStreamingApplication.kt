package com.example.chatclient

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class ChatClientStreamingApplication

fun main(args: Array<String>) {
    runApplication<ChatClientStreamingApplication>(*args)
}
