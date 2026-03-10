package com.example.mcp.tools.service

import io.modelcontextprotocol.client.McpSyncClient
import io.modelcontextprotocol.spec.McpSchema
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class ToolClientService(@Qualifier("mcpSyncClients") private val mcpClients: List<McpSyncClient>) {

    // mcpClients 리스트 중에서 첫 번째 클라이언트(local-server)를 가져옵니다.
    private val mcpClient: McpSyncClient
        get() = mcpClients.firstOrNull() ?: throw IllegalStateException("No MCP clients found")

    fun listTools(): List<String> {
        val tools = mcpClient.listTools(null)
        return tools.tools().map { it.name() }
    }

    fun callCalculateTool(operation: String, a: Double, b: Double): String {
        val result =
                mcpClient.callTool(
                        McpSchema.CallToolRequest(
                                "calculate",
                                mapOf("operation" to operation, "a" to a, "b" to b)
                        )
                )
        return result.content().joinToString("\n") {
            if (it is McpSchema.TextContent) it.text() else it.toString()
        }
    }

    fun callConvertTemperature(value: Double, from: String, to: String): String {
        val result =
                mcpClient.callTool(
                        McpSchema.CallToolRequest(
                                "convertTemperature",
                                mapOf("value" to value, "from" to from, "to" to to)
                        )
                )
        return result.content().joinToString("\n") {
            if (it is McpSchema.TextContent) it.text() else it.toString()
        }
    }
}
