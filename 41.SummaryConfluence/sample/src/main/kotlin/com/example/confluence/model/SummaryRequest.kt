package com.example.confluence.model

import java.time.LocalDate

/** 요약 요청 */
data class SummaryRequest(
        val pageId: String? = null,
        val pageUrl: String? = null,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val includeChildren: Boolean = true,
        val maxDepth: Int = 3
)

/** 요약 응답 */
data class SummaryResponse(
        val totalPages: Int,
        val summaries: List<PageSummary>,
        val dateRange: DateRange,
        val processingTimeMs: Long
)

/** 개별 페이지 요약 */
data class PageSummary(
        val pageId: String,
        val title: String,
        val url: String,
        val lastModified: String,
        val summary: String,
        val wordCount: Int
)

/** 날짜 범위 */
data class DateRange(val start: LocalDate, val end: LocalDate)
