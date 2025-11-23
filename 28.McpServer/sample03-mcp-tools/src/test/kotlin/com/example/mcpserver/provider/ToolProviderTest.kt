package com.example.mcpserver.provider

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ToolProviderTest {

    @Autowired lateinit var toolProvider: ToolProvider

    @Test
    fun `should calculate addition`() {
        val result = toolProvider.calculate("add", 10.0, 5.0)
        assertThat(result).isEqualTo(15.0)
    }

    @Test
    fun `should convert temperature`() {
        val result = toolProvider.convertTemperature(0.0, "celsius", "fahrenheit")
        assertThat(result).isEqualTo(32.0)
    }
}
