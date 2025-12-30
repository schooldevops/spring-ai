package com.example.springai.util

/** 텍스트 정리 유틸리티 LLM 응답에서 불필요한 태그나 메타데이터를 제거 */
object TextCleanupUtil {

    /** <thought> 태그와 그 내용을 제거 일부 LLM 모델(특히 Ollama의 일부 모델)은 응답에 사고 과정을 <thought> 태그로 포함할 수 있음 */
    fun removeThoughtTags(text: String): String {
        // <thought>...</thought> 패턴을 모두 제거 (대소문자 구분 없이, 여러 줄에 걸쳐 있어도 처리)
        return text.replace(
                        Regex(
                                "<thought>.*?</thought>",
                                setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
                        ),
                        ""
                )
                .trim()
    }

    /** 여러 정리 작업을 한 번에 수행 */
    fun cleanupResponse(text: String): String {
        return removeThoughtTags(text)
    }
}
