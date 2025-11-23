package com.example.mcpserver.controller

import com.example.mcpserver.provider.ToolProvider
import org.springframework.web.bind.annotation.*

/** Streamable HTTP Server Controller */
@RestController
@RequestMapping("/api/tools")
class StreamableHttpController(private val toolProvider: ToolProvider) {

    @GetMapping
    fun listTools(): Map<String, Any> {
        return mapOf("transport" to "Streamable HTTP", "tools" to toolProvider.listTools())
    }

    @PostMapping("/calculate")
    fun calculate(@RequestBody request: Map<String, Any>): Map<String, Any> {
        val operation = request["operation"] as String
        val a = (request["a"] as Number).toDouble()
        val b = (request["b"] as Number).toDouble()

        val result = toolProvider.calculate(operation, a, b)
        return mapOf("result" to result)
    }

    @PostMapping("/convert-temperature")
    fun convertTemperature(@RequestBody request: Map<String, Any>): Map<String, Any> {
        val value = (request["value"] as Number).toDouble()
        val from = request["from"] as String
        val to = request["to"] as String

        val result = toolProvider.convertTemperature(value, from, to)
        return mapOf("result" to result, "unit" to to)
    }

    @GetMapping("/info")
    fun getInfo(): Map<String, Any> {
        return mapOf(
                "transport" to "Streamable HTTP",
                "description" to "HTTP with chunked transfer encoding",
                "features" to
                        listOf(
                                "Chunked responses",
                                "Progressive data transfer",
                                "Efficient for large payloads"
                        )
        )
    }
}
