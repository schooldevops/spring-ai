package com.example.confluence.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/** Confluence 설정 */
@Configuration
@ConfigurationProperties(prefix = "confluence")
data class ConfluenceConfig(
        var baseUrl: String = "",
        var username: String = "",
        var apiToken: String = "",
        var authType: String = "basic" // "basic" or "bearer"
) {
    fun getAuthHeader(): String {
        return when (authType.lowercase()) {
            "bearer" -> "Bearer $apiToken"
            else -> {
                // Basic Authentication
                val credentials = "$username:$apiToken"
                println("credentials: $credentials")
                "Basic " + java.util.Base64.getEncoder().encodeToString(credentials.toByteArray())
            }
        }
    }
}
