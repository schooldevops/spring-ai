package com.example.mcp

import com.example.mcp.service.BasicMcpService
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class BasicMcpIntegrationTest {

    @Autowired
    lateinit var basicMcpService: BasicMcpService

    @Test
    fun `should list server tools and call getServerTime tool`() {
        val tools = basicMcpService.getServerTools()
        assertTrue(tools.contains("getServerTime"), "Server should provide 'getServerTime' tool")

        val result = basicMcpService.callTimeTool("Asia/Seoul")
        assertTrue(result.contains("Server time in Asia/Seoul"), "Result should contain formatted time string")
        println("Tool Result: $result")
    }
}
