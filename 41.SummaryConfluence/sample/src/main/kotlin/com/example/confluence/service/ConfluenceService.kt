package com.example.confluence.service

import com.example.confluence.client.ConfluenceClient
import com.example.confluence.model.ConfluencePage
import com.example.confluence.model.SimplePage
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/** Confluence 문서 관리 서비스 */
@Service
class ConfluenceService(private val confluenceClient: ConfluenceClient) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /** 페이지와 하위 페이지를 재귀적으로 가져오기 */
    fun fetchPagesRecursively(
            pageId: String,
            startDate: LocalDate,
            endDate: LocalDate,
            maxDepth: Int = 3,
            currentDepth: Int = 0
    ): List<SimplePage> {
        if (currentDepth >= maxDepth) {
            logger.debug("Reached max depth $maxDepth")
            return emptyList()
        }

        val pages = mutableListOf<SimplePage>()

        // 현재 페이지 가져오기
        val page = confluenceClient.getPageById(pageId)
        if (page != null) {
            val simplePage = convertToSimplePage(page)
            if (simplePage != null && isWithinDateRange(simplePage.lastModified, startDate, endDate)
            ) {
                pages.add(simplePage)
                logger.info("Added page: ${simplePage.title} (${simplePage.id})")
            }
        }

        // 하위 페이지 가져오기
        val childPages = confluenceClient.getChildPages(pageId)
        logger.debug("Found ${childPages.size} child pages for $pageId")

        for (childPage in childPages) {
            val childSimplePage = convertToSimplePage(childPage)
            if (childSimplePage != null &&
                            isWithinDateRange(childSimplePage.lastModified, startDate, endDate)
            ) {
                pages.add(childSimplePage)
                logger.info("Added child page: ${childSimplePage.title} (${childSimplePage.id})")
            }

            // 재귀적으로 하위 페이지의 하위 페이지 가져오기
            val grandChildPages =
                    fetchPagesRecursively(
                            childPage.id,
                            startDate,
                            endDate,
                            maxDepth,
                            currentDepth + 1
                    )
            pages.addAll(grandChildPages)
        }

        return pages
    }

    /** Confluence 페이지를 SimplePage로 변환 */
    private fun convertToSimplePage(page: ConfluencePage): SimplePage? {
        try {
            val content = extractTextContent(page.body?.storage?.value ?: "")
            val lastModified = parseDateTime(page.version?.whenUpdated ?: return null)
            val url = confluenceClient.buildPageUrl(page.id)

            return SimplePage(
                    id = page.id,
                    title = page.title,
                    content = content,
                    lastModified = lastModified,
                    url = url
            )
        } catch (e: Exception) {
            logger.error("Error converting page ${page.id}", e)
            return null
        }
    }

    /** HTML 콘텐츠에서 텍스트 추출 */
    private fun extractTextContent(html: String): String {
        return try {
            Jsoup.parse(html).text()
        } catch (e: Exception) {
            logger.warn("Failed to parse HTML content", e)
            html
        }
    }

    /** 날짜 문자열을 LocalDateTime으로 파싱 */
    private fun parseDateTime(dateString: String): LocalDateTime {
        // Confluence API 날짜 형식: 2024-12-10T13:45:00.000+09:00
        return try {
            LocalDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        } catch (e: Exception) {
            logger.warn("Failed to parse date: $dateString", e)
            LocalDateTime.now()
        }
    }

    /** 날짜 범위 확인 */
    private fun isWithinDateRange(
            dateTime: LocalDateTime,
            startDate: LocalDate,
            endDate: LocalDate
    ): Boolean {
        val date = dateTime.toLocalDate()
        return !date.isBefore(startDate) && !date.isAfter(endDate)
    }

    /** URL에서 페이지 ID 추출 */
    fun extractPageId(url: String): String? {
        return confluenceClient.extractPageIdFromUrl(url)
    }
}
