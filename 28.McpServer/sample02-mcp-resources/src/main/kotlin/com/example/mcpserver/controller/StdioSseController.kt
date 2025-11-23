package com.example.mcpserver.controller

import com.example.mcpserver.provider.ResourceProvider
import org.springframework.web.bind.annotation.*

/** STDIO/SSE Server Controller */
@RestController
@RequestMapping("/api/resources")
class StdioSseController(private val resourceProvider: ResourceProvider) {

    @GetMapping
    fun listResources(): Map<String, Any> {
        return mapOf("transport" to "STDIO/SSE", "resources" to resourceProvider.listResources())
    }

    @GetMapping("/{name}")
    fun getResource(@PathVariable name: String): Map<String, Any?> {
        return mapOf("name" to name, "data" to resourceProvider.getResource(name))
    }

    @GetMapping("/info")
    fun getInfo(): Map<String, Any> {
        return mapOf(
                "transport" to "STDIO/SSE",
                "description" to "STDIO for local process, SSE for streaming",
                "features" to
                        listOf(
                                "Real-time updates",
                                "Bidirectional communication",
                                "Event streaming"
                        )
        )
    }
}
