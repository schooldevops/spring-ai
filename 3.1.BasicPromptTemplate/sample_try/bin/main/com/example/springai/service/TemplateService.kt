package com.example.springai.service

import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.chat.prompt.PromptTemplate
import org.springframework.stereotype.Service

@Service
class TemplateService(private val chatModel: ChatModel) {

    private val greetingTemplate = PromptTemplate("안녕하세요 {name}님! 오늘도 좋은 하루 되세요.")
    private val questionTemplate =
            PromptTemplate(
                    """
        {userName}님이 질문하셨습니다.

        질문: {question} 

        {additionalContext}

        친절하고 정확하게 답변해주세요.

        """.trimIndent()
            )

    private val summaryTemplate =
            PromptTemplate(
                    """
        다음 내용을 요약해주세요:

        {content}

        핵심 내용을 3-5문장으로 간결하게 요약해주세요.
        """.trimIndent()
            )

    fun createGreetingPrompt(name: String): Prompt {
        return greetingTemplate.create(mapOf("name" to name))
    }

    fun createQuestionPrompt(
            userName: String,
            question: String,
            additionalContext: String = ""
    ): Prompt {
        return questionTemplate.create(
                mapOf(
                        "userName" to userName,
                        "question" to question,
                        "additionalContext" to
                                if (additionalContext.isNotEmpty()) {
                                    "추가 컨텍스트: $additionalContext"
                                } else {
                                    ""
                                }
                )
        )
    }

    fun createSummaryPrompt(content: String): Prompt {
        return summaryTemplate.create(mapOf("content" to content))
    }

    fun generateGreeting(name: String): String {
        val prompt = createGreetingPrompt(name)
        val response = chatModel.call(prompt)
        return response.results.firstOrNull()?.output?.text ?: "응답 없음"
    }

    fun answerQuestion(userName: String, question: String, context: String = ""): String {
        val prompt = createQuestionPrompt(userName, question, context)
        val response = chatModel.call(prompt)
        return response.results.firstOrNull()?.output?.text ?: "응답 없음"
    }

    fun summarize(content: String): String {
        val prompt = createSummaryPrompt(content)
        val response = chatModel.call(prompt)
        return response.results.firstOrNull()?.output?.text ?: "응답 없음"
    }
}
