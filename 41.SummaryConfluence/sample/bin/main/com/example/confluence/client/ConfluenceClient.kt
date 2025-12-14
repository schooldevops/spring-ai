package com.example.confluence.client

import com.example.confluence.config.ConfluenceConfig
import com.example.confluence.model.ConfluencePage
import com.example.confluence.model.ConfluencePageResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/** Confluence REST API 클라이언트 */
@Component
class ConfluenceClient(
        private val config: ConfluenceConfig,
        private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val httpClient =
            OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()

    /** 페이지 ID로 페이지 조회 */
    fun getPageById(
            pageId: String,
            expand: List<String> = listOf("body.storage", "version", "ancestors")
    ): ConfluencePage? {
        val expandParam = expand.joinToString(",")
        val url = "${config.baseUrl}/content/$pageId?expand=$expandParam"

        logger.debug("Fetching page: $url")
        logger.debug("Using auth header: ${config.getAuthHeader().take(20)}...")

        val request =
                Request.Builder()
                        .url(url)
                        .header("Authorization", config.getAuthHeader())
                        .header("Accept", "application/json")
                        .build()

        return try {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    logger.error(
                            "Failed to fetch page $pageId: ${response.code} ${response.message}"
                    )
                    return null
                }

                val body = response.body?.string() ?: return null
                objectMapper.readValue<ConfluencePage>(body)
            }
        } catch (e: Exception) {
            logger.error("Error fetching page $pageId", e)
            null
        }
    }

    /** 하위 페이지 조회 */
    fun getChildPages(pageId: String, limit: Int = 100): List<ConfluencePage> {
        val url = "${config.baseUrl}/content/$pageId/child/page?limit=$limit&expand=version"

        logger.debug("Fetching child pages: $url")

        val request =
                Request.Builder()
                        .url(url)
                        .header("Authorization", config.getAuthHeader())
                        .header("Accept", "application/json")
                        .build()

        return try {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    logger.error("Failed to fetch child pages for $pageId: ${response.code}")
                    return emptyList()
                }

                val body = response.body?.string() ?: return emptyList()
                val pageResponse = objectMapper.readValue<ConfluencePageResponse>(body)
                pageResponse.results
            }
        } catch (e: Exception) {
            logger.error("Error fetching child pages for $pageId", e)
            emptyList()
        }
    }

    /** URL에서 페이지 ID 추출 */
    fun extractPageIdFromUrl(url: String): String? {
        // URL 형식: https://domain.atlassian.net/wiki/spaces/SPACE/pages/123456/Page+Title
        val regex = """/pages/(\d+)""".toRegex()
        return regex.find(url)?.groupValues?.get(1)
    }

    /** 페이지 URL 생성 */
    fun buildPageUrl(pageId: String): String {
        // base-url이 이미 /wiki/rest/api를 포함하면 제거
        val baseUrl = config.baseUrl.replace("/rest/api", "")
        return "$baseUrl/pages/viewpage.action?pageId=$pageId"
    }
}
