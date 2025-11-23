package com.example.chatclient.controller

import com.example.chatclient.model.ActorFilms
import com.example.chatclient.model.BookInfo
import com.example.chatclient.model.Product
import org.springframework.ai.chat.client.ChatClient
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.bind.annotation.*

/**
 * Sample 03: Entity Mapping
 *
 * AI 응답을 Kotlin 데이터 클래스로 자동 매핑
 */
@RestController
@RequestMapping("/api/entity")
class EntityMappingController(private val chatClient: ChatClient) {

    /** 1. 단일 엔티티 매핑 POST /api/entity/actor */
    @PostMapping("/actor")
    fun getActorFilms(@RequestParam actor: String): ActorFilms {
        return chatClient
                .prompt()
                .user("Generate the filmography for $actor with 5 movies")
                .call()
                .entity(ActorFilms::class.java)
    }

    /** 2. Product 엔티티 매핑 POST /api/entity/product */
    @PostMapping("/product")
    fun getProduct(@RequestParam productName: String): Product {
        return chatClient
                .prompt()
                .user("Generate product information for: $productName")
                .call()
                .entity(Product::class.java)
    }

    /** 3. List 매핑 (ParameterizedTypeReference 사용) POST /api/entity/actors-list */
    @PostMapping("/actors-list")
    fun getActorsList(@RequestParam actors: String): List<ActorFilms> {
        return chatClient
                .prompt()
                .user("Generate filmography for these actors: $actors. Include 3 movies for each.")
                .call()
                .entity(object : ParameterizedTypeReference<List<ActorFilms>>() {})
    }

    /** 4. BookInfo 엔티티 매핑 GET /api/entity/book */
    @GetMapping("/book")
    fun getBookInfo(@RequestParam title: String): BookInfo {
        return chatClient
                .prompt()
                .user("Generate information about the book: $title")
                .call()
                .entity(BookInfo::class.java)
    }
}
