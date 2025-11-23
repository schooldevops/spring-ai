package com.example.chatclient.controller

import com.example.chatclient.model.ActorFilms
import com.example.chatclient.model.BookInfo
import com.example.chatclient.model.Product
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.ai.chat.client.ChatClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.ParameterizedTypeReference

/** TDD: Sample 03 - Entity Mapping 테스트 */
@SpringBootTest
class EntityMappingControllerTest {

    @Autowired lateinit var chatClient: ChatClient

    @Test
    fun `should map response to ActorFilms entity`() {
        // When
        val result =
                chatClient
                        .prompt()
                        .user("Generate filmography for Tom Hanks with 3 movies")
                        .call()
                        .entity(ActorFilms::class.java)

        // Then
        assertThat(result).isNotNull
        assertThat(result.actor).isNotEmpty()
        assertThat(result.movies).isNotEmpty()
        assertThat(result.movies).hasSizeGreaterThanOrEqualTo(1)
    }

    @Test
    fun `should map response to Product entity`() {
        // When
        val result =
                chatClient
                        .prompt()
                        .user("Generate a product: laptop")
                        .call()
                        .entity(Product::class.java)

        // Then
        assertThat(result).isNotNull
        assertThat(result.name).isNotEmpty()
        assertThat(result.price).isGreaterThan(0.0)
        assertThat(result.category).isNotEmpty()
    }

    @Test
    fun `should map response to List of ActorFilms using ParameterizedTypeReference`() {
        // When
        val result =
                chatClient
                        .prompt()
                        .user("Generate filmography for Tom Hanks and Bill Murray, 2 movies each")
                        .call()
                        .entity(object : ParameterizedTypeReference<List<ActorFilms>>() {})

        // Then
        assertThat(result).isNotNull
        assertThat(result).hasSizeGreaterThanOrEqualTo(1)
        result.forEach { actorFilm ->
            assertThat(actorFilm.actor).isNotEmpty()
            assertThat(actorFilm.movies).isNotEmpty()
        }
    }

    @Test
    fun `should map response to BookInfo entity`() {
        // When
        val result =
                chatClient
                        .prompt()
                        .user("Generate information about the book '1984' by George Orwell")
                        .call()
                        .entity(BookInfo::class.java)

        // Then
        assertThat(result).isNotNull
        assertThat(result.title).isNotEmpty()
        assertThat(result.author).isNotEmpty()
        assertThat(result.year).isGreaterThan(1900)
    }
}
