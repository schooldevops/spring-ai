package com.example.mcpserver.provider

import org.springframework.stereotype.Component

/**
 * Sample 03: Streamable HTTP MCP Server
 *
 * Provides tools via streamable HTTP for chunked responses
 */
@Component
class ToolProvider {

    fun calculate(operation: String, a: Double, b: Double): Double {
        return when (operation.lowercase()) {
            "add" -> a + b
            "subtract" -> a - b
            "multiply" -> a * b
            "divide" -> if (b != 0.0) a / b else Double.NaN
            else -> throw IllegalArgumentException("Unknown operation: $operation")
        }
    }

    fun convertTemperature(value: Double, from: String, to: String): Double {
        return when {
            from == "celsius" && to == "fahrenheit" -> value * 9 / 5 + 32
            from == "fahrenheit" && to == "celsius" -> (value - 32) * 5 / 9
            from == to -> value
            else -> throw IllegalArgumentException("Unsupported conversion")
        }
    }

    fun listTools(): List<Map<String, String>> {
        return listOf(
                mapOf("name" to "calculate", "description" to "Perform arithmetic operations"),
                mapOf("name" to "convertTemperature", "description" to "Convert temperature units")
        )
    }
}
