package com.example.mcp.resources.controller

import com.example.mcp.resources.service.ResourceClientService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/mcp/resources")
class ResourceClientController(private val service: ResourceClientService) {

    @GetMapping
    fun listResources(): List<String> {
        return service.listResources()
    }

    @GetMapping("/read")
    fun readResource(@RequestParam uri: String): Map<String, String> {
        return mapOf("content" to service.readResource(uri))
    }

    @GetMapping("/info")
    fun info(): Map<String, String> {
        return mapOf(
                "description" to "Sample 02 MCP Resources Client",
                "usage" to
                        "Connects via STDIO (local jar) and SSE (external MCP server) to read resources",
                "mcp-servers" to "local-server (stdio), remote-server (sse)"
        )
    }
}
