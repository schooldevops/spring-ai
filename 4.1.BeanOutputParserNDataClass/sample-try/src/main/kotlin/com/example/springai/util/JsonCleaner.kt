package com.example.springai.util

object JsonCleaner {
    fun cleanJsonText(text: String): String {
        // 1. <thought> 태그와 그 내용 제거
        val withoutThought =
                text.replace(Regex("<thought>.*?</thought>", RegexOption.DOT_MATCHES_ALL), "")

        // 2. 기존 정리 로직 적용
        return withoutThought
                .replace("```json", "")
                .replace("```", "")
                .trim()
                .lines()
                .filter { line ->
                    !line.trim().startsWith("//") && line.isNotBlank() || line.isEmpty()
                }
                .joinToString("\n")
                .trim()
    }

    fun externalJsonObject(text: String): String {
        val startIndex = text.indexOf('{')
        val endIndex = text.lastIndexOf('}')

        return if (startIndex >= 0 && endIndex > startIndex) {
            text.substring(startIndex, endIndex + 1)
        } else {
            text
        }
    }
}
