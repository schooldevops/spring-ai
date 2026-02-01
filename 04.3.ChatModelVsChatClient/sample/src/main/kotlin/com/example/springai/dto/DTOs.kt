package com.example.springai.dto

/** 기본 질문 요청 DTO */
data class QuestionRequest(val question: String)

/** 시스템 메시지를 포함한 질문 요청 DTO */
data class ContextQuestionRequest(val systemMessage: String, val userMessage: String)

/** 도메인 전문가 질문 요청 DTO */
data class ExpertQuestionRequest(val domain: String, val question: String)

/** 책 추천 응답 DTO */
data class BookRecommendation(
        val title: String,
        val author: String,
        val genre: String,
        val summary: String,
        val publishYear: Int? = null
)

/** 영화 추천 응답 DTO */
data class MovieRecommendation(
        val title: String,
        val director: String,
        val genre: String,
        val year: Int,
        val rating: Double,
        val summary: String
)

/** 제품 정보 DTO */
data class Product(
        val name: String,
        val price: Double,
        val category: String,
        val features: List<String>,
        val description: String? = null
)

/** 번역 요청 DTO */
data class TranslationRequest(val text: String, val targetLanguage: String)

/** 응답 메타데이터 DTO */
data class ResponseWithMetadata(
        val content: String,
        val model: String,
        val totalTokens: Int,
        val promptTokens: Int,
        val completionTokens: Int
)
