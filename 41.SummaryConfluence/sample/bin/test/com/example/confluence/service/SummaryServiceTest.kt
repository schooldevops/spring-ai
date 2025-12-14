package com.example.confluence.service

import com.example.confluence.model.SimplePage
import com.example.confluence.model.SummaryRequest
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.ai.chat.client.ChatClient

@ExtendWith(MockitoExtension::class)
class SummaryServiceTest {

        @Mock private lateinit var confluenceService: ConfluenceService

        @Mock private lateinit var chatClientBuilder: ChatClient.Builder

        @Mock private lateinit var chatClient: ChatClient

        @Mock private lateinit var chatClientRequestSpec: ChatClient.ChatClientRequestSpec

        @Mock private lateinit var chatClientCallResponseSpec: ChatClient.CallResponseSpec

        private lateinit var summaryService: SummaryService

        private fun setupChatClient(expectedResponse: String) {
                whenever(chatClientBuilder.build()).thenReturn(chatClient)
                whenever(chatClient.prompt()).thenReturn(chatClientRequestSpec)
                whenever(
                                chatClientRequestSpec.user(
                                        any<
                                                java.util.function.Consumer<
                                                        ChatClient.PromptUserSpec>>()
                                )
                        )
                        .thenReturn(chatClientRequestSpec)
                whenever(chatClientRequestSpec.call()).thenReturn(chatClientCallResponseSpec)
                whenever(chatClientCallResponseSpec.content()).thenReturn(expectedResponse)

                summaryService = SummaryService(confluenceService, chatClientBuilder)
        }

        @Test
        fun `should summarize page tree successfully`() {
                // Given
                setupChatClient("테스트 문서의 요약입니다.")

                val pageId = "123456"
                val startDate = LocalDate.of(2024, 1, 1)
                val endDate = LocalDate.of(2024, 12, 31)
                val maxDepth = 3

                val request =
                        SummaryRequest(
                                pageId = pageId,
                                startDate = startDate,
                                endDate = endDate,
                                includeChildren = true,
                                maxDepth = maxDepth
                        )

                val mockPages =
                        listOf(
                                SimplePage(
                                        id = "123456",
                                        title = "테스트 페이지",
                                        content = "테스트 내용입니다.",
                                        lastModified = LocalDateTime.now(),
                                        url = "https://example.com/page/123456"
                                )
                        )

                whenever(
                                confluenceService.fetchPagesRecursively(
                                        eq(pageId),
                                        eq(startDate),
                                        eq(endDate),
                                        eq(maxDepth)
                                )
                        )
                        .thenReturn(mockPages)

                // When
                val result = summaryService.summarizePageTree(request)

                // Then
                assertNotNull(result)
                assertEquals(1, result.totalPages)
                assertEquals(1, result.summaries.size)
                assertEquals("테스트 페이지", result.summaries[0].title)
        }
}
