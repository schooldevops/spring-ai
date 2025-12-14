package com.example.confluence.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/** Confluence 페이지 정보 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ConfluencePage(
        val id: String,
        val title: String,
        val type: String = "page",
        @JsonProperty("body") val body: PageBody? = null,
        @JsonProperty("version") val version: PageVersion? = null,
        @JsonProperty("_links") val links: Map<String, String>? = null,
        @JsonProperty("ancestors") val ancestors: List<PageAncestor>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PageBody(
        @JsonProperty("storage") val storage: StorageFormat? = null,
        @JsonProperty("view") val view: ViewFormat? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class StorageFormat(val value: String, val representation: String = "storage")

@JsonIgnoreProperties(ignoreUnknown = true)
data class ViewFormat(val value: String, val representation: String = "view")

@JsonIgnoreProperties(ignoreUnknown = true)
data class PageVersion(
        val number: Int,
        @JsonProperty("when") val whenUpdated: String,
        @JsonProperty("by") val by: PageUser? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PageUser(val displayName: String, val email: String? = null)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PageAncestor(val id: String, val title: String)

/** Confluence API 응답 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ConfluencePageResponse(
        val results: List<ConfluencePage>,
        val start: Int = 0,
        val limit: Int = 25,
        val size: Int = 0,
        @JsonProperty("_links") val links: Map<String, String>? = null
)

/** 간소화된 페이지 정보 (내부 사용) */
data class SimplePage(
        val id: String,
        val title: String,
        val content: String,
        val lastModified: LocalDateTime,
        val url: String
)
