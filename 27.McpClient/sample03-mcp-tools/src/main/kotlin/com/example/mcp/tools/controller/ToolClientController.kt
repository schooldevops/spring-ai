package com.example.mcp.tools.controller

import com.example.mcp.tools.service.ToolClientService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/mcp/tools")
class ToolClientController(private val service: ToolClientService) {

    @GetMapping
    fun listTools(): List<String> {
        return service.listTools()
    }

    @GetMapping("/calculate")
    fun calculate(
            @RequestParam operation: String,
            @RequestParam a: Double,
            @RequestParam b: Double
    ): Map<String, String> {
        return mapOf("result" to service.callCalculateTool(operation, a, b))
    }

    @GetMapping("/convert-temperature")
    fun convertTemperature(
            @RequestParam value: Double,
            @RequestParam from: String,
            @RequestParam to: String
    ): Map<String, String> {
        return mapOf("result" to service.callConvertTemperature(value, from, to))
    }

    @GetMapping("/info")
    fun info(): Map<String, String> {
        return mapOf(
                "description" to "Sample 03 MCP Tools Client",
                "usage" to
                        "Connects via STDIO (local jar) and HTTP/SSE (external MCP server) to execute tools",
                "mcp-servers" to "local-server (stdio), remote-server (sse)"
        )
    }
}
