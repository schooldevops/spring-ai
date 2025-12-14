package com.example.confluence.service

import com.example.confluence.model.*
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.stereotype.Service

/** AI 기반 문서 요약 서비스 */
@Service
class SummaryService(
        private val confluenceService: ConfluenceService,
        private val chatClientBuilder: ChatClient.Builder
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val chatClient = chatClientBuilder.build()

    private val summaryTemplate =
            """
        다음 Confluence 문서를 간결하게 요약해주세요:
        
        제목: {title}
        
        내용:
        {content}
        
        요약 지침:
        - 핵심 내용을 3-5문장으로 요약
        - 주요 키워드와 개념 포함
        - 기술적 세부사항 유지
        - 명확하고 간결한 한국어로 작성
    """.trimIndent()

    /** 페이지 트리 요약 */
    fun summarizePageTree(request: SummaryRequest): SummaryResponse {
        val startTime = System.currentTimeMillis()

        // 페이지 ID 확인
        val pageId =
                when {
                    request.pageId != null -> request.pageId
                    request.pageUrl != null -> confluenceService.extractPageId(request.pageUrl)
                    else -> throw IllegalArgumentException("pageId 또는 pageUrl이 필요합니다")
                }
                        ?: throw IllegalArgumentException("유효하지 않은 페이지 URL입니다")

        logger.info(
                "Starting summarization for page $pageId from ${request.startDate} to ${request.endDate}"
        )

        // 페이지 가져오기
        val pages =
                if (request.includeChildren) {
                    confluenceService.fetchPagesRecursively(
                            pageId,
                            request.startDate,
                            request.endDate,
                            request.maxDepth
                    )
                } else {
                    val page =
                            confluenceService.fetchPagesRecursively(
                                    pageId,
                                    request.startDate,
                                    request.endDate,
                                    maxDepth = 0
                            )
                    page
                }

        logger.info("Found ${pages.size} pages to summarize")

        // 각 페이지 요약
        val summaries = pages.map { page -> summarizePage(page) }

        val processingTime = System.currentTimeMillis() - startTime

        return SummaryResponse(
                totalPages = pages.size,
                summaries = summaries,
                dateRange = DateRange(request.startDate, request.endDate),
                processingTimeMs = processingTime
        )
    }

    /** 단일 페이지 요약 */
    private fun summarizePage(page: SimplePage): PageSummary {
        logger.debug("Summarizing page: ${page.title}")

        val summary =
                try {
                    // 콘텐츠가 너무 길면 잘라내기 (토큰 제한 고려)
                    val truncatedContent =
                            if (page.content.length > 4000) {
                                page.content.substring(0, 4000) + "..."
                            } else {
                                page.content
                            }

                    chatClient
                            .prompt()
                            .user { u ->
                                u.text(summaryTemplate)
                                        .param("title", page.title)
                                        .param("content", truncatedContent)
                            }
                            .call()
                            .content()
                            ?: "요약 생성 실패"
                } catch (e: Exception) {
                    logger.error("Error summarizing page ${page.id}", e)
                    "요약 생성 중 오류 발생: ${e.message}"
                }

        return PageSummary(
                pageId = page.id,
                title = page.title,
                url = page.url,
                lastModified = page.lastModified.toString(),
                summary = summary,
                wordCount = page.content.length
        )
    }

    /** 배치 요약 (여러 페이지를 한 번에 요약) */
    fun summarizeBatch(pages: List<SimplePage>): List<PageSummary> {
        logger.info("Batch summarizing ${pages.size} pages")

        return pages.map { page -> summarizePage(page) }
    }
}
