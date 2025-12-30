package com.example.springai.controller

import com.example.springai.service.TemplateClientService
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/basic-chat-client")
class BasicTemplateController(private val templateClientService: TemplateClientService) {
    @GetMapping("/greet/{name}")
    fun greet(@PathVariable name: String): String {
        return templateClientService.generateGreeting(name)
    }

    @PostMapping("/personalized")
    fun personalizedChat(@RequestBody request: QuestionRequest): String {
        return templateClientService.answerQuestion(
                request.userName,
                request.question,
                request.interest
        )
    }

    @PostMapping("/summary")
    fun summaryQuestion(@RequestBody request: QuestionRequest): String {
        return templateClientService.summarize(request.question)
    }
}

data class QuestionRequest(val userName: String, val question: String, val interest: String)
