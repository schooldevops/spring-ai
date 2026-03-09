package com.example.mcp.service

import io.modelcontextprotocol.client.McpSyncClient
import io.modelcontextprotocol.spec.McpSchema
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class BasicMcpService(@Qualifier("mcpSyncClients") private val mcpClients: List<McpSyncClient>) {

    private val mcpClient: McpSyncClient
        get() = mcpClients.firstOrNull() ?: throw IllegalStateException("No MCP clients found")

    fun getServerTools(): List<String> {
        val tools = mcpClient.listTools(null)
        return tools.tools().map { it.name() }
    }

    fun callTimeTool(timezone: String): String {
        // Find the tool by name, typically the bean name or description name
        val result = mcpClient.callTool(
            McpSchema.CallToolRequest("getServerTime", mapOf("timezone" to timezone))
        )
        return result.content().joinToString("\n") { 
            if (it is McpSchema.TextContent) it.text() else it.toString() 
        }
    }
}
