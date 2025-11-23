package com.example.mcpserver.provider

import org.springframework.stereotype.Component

/**
 * Sample 02: STDIO/SSE MCP Server
 *
 * STDIO: Standard Input/Output for local process communication SSE: Server-Sent Events for
 * real-time streaming
 */
@Component
class ResourceProvider {

    // Simulated resource data
    private val resources =
            mapOf(
                    "users" to
                            listOf(
                                    mapOf("id" to 1, "name" to "Alice"),
                                    mapOf("id" to 2, "name" to "Bob")
                            ),
                    "products" to
                            listOf(
                                    mapOf("id" to 101, "name" to "Laptop", "price" to 1200),
                                    mapOf("id" to 102, "name" to "Mouse", "price" to 25)
                            )
            )

    fun getResource(name: String): Any? {
        return resources[name]
    }

    fun listResources(): List<String> {
        return resources.keys.toList()
    }
}
