package com.example.image.controller

import org.springframework.ai.image.ImageModel
import org.springframework.ai.image.ImagePrompt
import org.springframework.web.bind.annotation.*

/** Sample 03: Stability AI */
@RestController
@RequestMapping("/api/image")
class StabilityImageController(private val imageModel: ImageModel) {

    data class ImageRequest(val prompt: String)

    @PostMapping("/generate")
    fun generateImage(@RequestBody request: ImageRequest): Map<String, Any> {
        val response = imageModel.call(ImagePrompt(request.prompt))
        return mapOf(
                "provider" to "Stability AI",
                "model" to "Stable Diffusion XL",
                "prompt" to request.prompt,
                "url" to response.result.output.url
        )
    }

    @GetMapping("/info")
    fun getInfo(): Map<String, Any> {
        return mapOf(
                "provider" to "Stability AI",
                "model" to "Stable Diffusion XL",
                "features" to listOf("Open source", "High quality", "Customizable")
        )
    }
}
