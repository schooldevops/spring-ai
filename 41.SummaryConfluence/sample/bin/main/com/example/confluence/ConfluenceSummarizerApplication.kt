package com.example.confluence

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication @ConfigurationPropertiesScan class ConfluenceSummarizerApplication

fun main(args: Array<String>) {
    runApplication<ConfluenceSummarizerApplication>(*args)
}
