package com.example.mcpserver.provider

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ResourceProviderTest {

    @Autowired lateinit var resourceProvider: ResourceProvider

    @Test
    fun `should list resources`() {
        val resources = resourceProvider.listResources()
        assertThat(resources).contains("users", "products")
    }

    @Test
    fun `should get resource by name`() {
        val users = resourceProvider.getResource("users")
        assertThat(users).isNotNull()
    }
}
