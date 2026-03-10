package com.example.mcpserver.provider

import com.example.mcpserver.service.StockService
import com.example.mcpserver.service.StockInfo
import com.example.mcpserver.service.StockPriceTrend
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Description

@Configuration
class StockToolProvider(
    private val stockService: StockService
) {

    @Bean
    @Description("Get current stock information for a US company by ticker symbol (e.g. AAPL, MSFT, TSLA)")
    fun getStockInfo(): (String) -> StockInfo {
        return { ticker ->
            stockService.getStockInfo(ticker)
        }
    }

    @Bean
    @Description("Get 3-month price trend for a US stock by ticker symbol")
    fun getStockPriceTrend(): (String) -> StockPriceTrend {
        return { ticker ->
            stockService.getStockPriceTrend(ticker)
        }
    }
}
