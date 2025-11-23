package com.example.image.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.ai.image.ImageModel
import org.springframework.ai.image.ImagePrompt
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "QIANFAN_API_KEY", matches = ".+")
class QianFanImageControllerTest {

    @Autowired lateinit var imageModel: ImageModel

    @Test
    fun `should generate image with QianFan`() {
        val response = imageModel.call(ImagePrompt("测试图片"))
        assertThat(response.result.output.url).isNotEmpty()
    }
}
