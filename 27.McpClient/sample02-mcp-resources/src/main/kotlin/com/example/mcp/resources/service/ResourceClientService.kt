package com.example.mcp.resources.service

import io.modelcontextprotocol.client.McpSyncClient
import io.modelcontextprotocol.spec.McpSchema
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class ResourceClientService(
        @Qualifier("mcpSyncClients") private val mcpClients: List<McpSyncClient>
) {

    // mcpClients 리스트 중에서 첫 번째 클라이언트(local-server)를 가져옵니다.
    // 만약 SSE 등 여러 서버가 동시 연결된다면 순회하거나 이름을 지정해 활용 가능합니다.
    private val mcpClient: McpSyncClient
        get() = mcpClients.firstOrNull() ?: throw IllegalStateException("No MCP clients found")

    fun listResources(): List<String> {
        val resources = mcpClient.listResources(null)
        return resources.resources().map { it.name() }
    }

    fun readResource(uri: String): String {
        val result = mcpClient.readResource(McpSchema.ReadResourceRequest(uri))
        return result.contents().joinToString("\n") {
            if (it is McpSchema.TextResourceContents) it.text() else it.toString()
        }
    }
}
