package com.example.image.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.ai.image.ImageModel
import org.springframework.ai.image.ImagePrompt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "STABILITY_AI_API_KEY", matches = ".+")
class StabilityImageControllerTest {

    @Autowired lateinit var imageModel: ImageModel

    @Test
    fun `should generate image with Stability AI`() {
        val response = imageModel.call(ImagePrompt("A test image"))
        assertThat(response.result.output.url).isNotEmpty()
    }
}
