package com.example.image.controller

import org.springframework.ai.image.ImageModel
import org.springframework.ai.image.ImagePrompt
import org.springframework.web.bind.annotation.*

/** Sample 02: Azure OpenAI DALL-E */
@RestController
@RequestMapping("/api/image")
class AzureImageController(private val imageModel: ImageModel) {

    data class ImageRequest(val prompt: String)

    @PostMapping("/generate")
    fun generateImage(@RequestBody request: ImageRequest): Map<String, Any> {
        val imagePrompt = ImagePrompt(request.prompt)
        val response = imageModel.call(imagePrompt)

        return mapOf(
                "provider" to "Azure OpenAI",
                "prompt" to request.prompt,
                "url" to response.result.output.url
        )
    }

    @GetMapping("/info")
    fun getInfo(): Map<String, Any> {
        return mapOf(
                "provider" to "Azure OpenAI",
                "model" to "DALL-E 3",
                "features" to
                        listOf(
                                "Enterprise-grade security",
                                "Azure integration",
                                "Compliance certifications"
                        )
        )
    }
}
