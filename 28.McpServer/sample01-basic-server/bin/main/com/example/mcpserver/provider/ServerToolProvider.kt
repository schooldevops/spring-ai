package com.example.mcpserver.provider

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Description
import java.time.LocalDateTime

@Configuration
class ServerToolProvider {

    @Bean
    @Description("Get the current server time")
    fun getServerTime(): (String) -> String {
        return { timezone ->
            "Server time in $timezone is ${LocalDateTime.now()}"
        }
    }
}
