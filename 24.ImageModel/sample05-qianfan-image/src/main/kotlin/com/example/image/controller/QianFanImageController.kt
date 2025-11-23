package com.example.image.controller

import org.springframework.ai.image.ImageModel
import org.springframework.ai.image.ImagePrompt
import org.springframework.web.bind.annotation.*

/** Sample 05: Baidu QianFan */
@RestController
@RequestMapping("/api/image")
class QianFanImageController(private val imageModel: ImageModel) {

    data class ImageRequest(val prompt: String)

    @PostMapping("/generate")
    fun generateImage(@RequestBody request: ImageRequest): Map<String, Any> {
        val response = imageModel.call(ImagePrompt(request.prompt))
        return mapOf(
                "provider" to "Baidu QianFan",
                "model" to "ERNIE-ViLG",
                "prompt" to request.prompt,
                "url" to response.result.output.url
        )
    }

    @GetMapping("/info")
    fun getInfo(): Map<String, Any> {
        return mapOf(
                "provider" to "Baidu QianFan",
                "model" to "ERNIE-ViLG",
                "features" to listOf("Baidu ecosystem", "Chinese market", "Enterprise support")
        )
    }
}
