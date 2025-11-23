package com.example.chatclient.model

/** Entity Mapping용 데이터 클래스들 */
data class ActorFilms(val actor: String, val movies: List<String>)

data class Product(
        val name: String,
        val price: Double,
        val category: String,
        val description: String
)

data class BookInfo(val title: String, val author: String, val year: Int, val genre: String)
