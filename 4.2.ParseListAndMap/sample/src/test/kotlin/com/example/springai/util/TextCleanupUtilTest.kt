package com.example.springai.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/** TextCleanupUtil 테스트 */
class TextCleanupUtilTest {

    @Test
    fun `should remove thought tags from text`() {
        val input =
                """
            <thought>Let me think about this...</thought>
            Python: 간결하고 읽기 쉬운 문법
            Java: 강력한 타입 시스템
            <thought>I should add more languages</thought>
            JavaScript: 웹 개발의 필수 언어
        """.trimIndent()

        val result = TextCleanupUtil.removeThoughtTags(input)

        // thought 태그가 제거되었는지 확인
        assertFalse(result.contains("<thought>"))
        assertFalse(result.contains("</thought>"))

        // 실제 내용이 포함되어 있는지 확인
        assertTrue(result.contains("Python: 간결하고 읽기 쉬운 문법"))
        assertTrue(result.contains("Java: 강력한 타입 시스템"))
        assertTrue(result.contains("JavaScript: 웹 개발의 필수 언어"))
    }

    @Test
    fun `should handle multiline thought tags`() {
        val input =
                """
            <thought>
            This is a multiline thought
            that spans multiple lines
            </thought>
            Kotlin: 현대적인 JVM 언어
        """.trimIndent()

        val result = TextCleanupUtil.removeThoughtTags(input)
        assertTrue(result.contains("Kotlin"))
        assertFalse(result.contains("<thought>"))
        assertFalse(result.contains("multiline thought"))
    }

    @Test
    fun `should handle case insensitive thought tags`() {
        val input =
                """
            <THOUGHT>uppercase thought</THOUGHT>
            <Thought>mixed case thought</Thought>
            Result: Some content
        """.trimIndent()

        val result = TextCleanupUtil.removeThoughtTags(input)
        assertFalse(result.contains("<thought>", ignoreCase = true))
        assertTrue(result.contains("Result"))
    }

    @Test
    fun `should return original text when no thought tags present`() {
        val input = "Python: 간결한 문법\nJava: 강력한 타입"
        val result = TextCleanupUtil.removeThoughtTags(input)
        assertEquals(input, result)
    }
}
