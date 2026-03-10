package com.example.mcpserver.service

import org.springframework.stereotype.Service
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import java.time.LocalDate
import kotlin.random.Random

@Service
class MockStockServiceImpl : StockService {

    private val companies = mapOf(
        "AAPL" to "Apple Inc.",
        "MSFT" to "Microsoft Corporation",
        "GOOGL" to "Alphabet Inc.",
        "AMZN" to "Amazon.com, Inc.",
        "TSLA" to "Tesla, Inc."
    )

    @Tool(description = "Get current stock information for a US company by ticker symbol (e.g. AAPL, MSFT, TSLA)")
    override fun getStockInfo(
        @ToolParam(description = "ticker symbol to lookup") ticker: String
    ): StockInfo {
        val upperTicker = ticker.uppercase()
        val companyName = companies[upperTicker] ?: "$upperTicker Corp"
        // Generate a stable mock price based on string hash
        val basePrice = 100.0 + (upperTicker.hashCode() % 1000) / 10.0
        
        return StockInfo(
            ticker = upperTicker,
            companyName = companyName,
            currentPrice = basePrice,
            currency = "USD"
        )
    }

    @Tool(description = "Get 3-month price trend for a US stock by ticker symbol")
    override fun getStockPriceTrend(
        @ToolParam(description = "ticker symbol to lookup the 3-month trend") ticker: String
    ): StockPriceTrend {
        val upperTicker = ticker.uppercase()
        val basePrice = 100.0 + (upperTicker.hashCode() % 1000) / 10.0
        
        val prices = mutableListOf<PricePoint>()
        val today = LocalDate.now()
        
        // Generate 3 months of data roughly (90 days)
        var currentMockPrice = basePrice * 0.8 // Start lower 3 months ago
        for (i in 90 downTo 0 step 5) { // Every 5 days
            val date = today.minusDays(i.toLong())
            prices.add(PricePoint(date.toString(), String.format("%.2f", currentMockPrice).toDouble()))
            
            // Random walk
            val change = Random.nextDouble(-0.05, 0.06) // Slight upward trend
            currentMockPrice *= (1 + change)
        }
        
        // Ensure last price matches current price roughly
        prices.add(PricePoint(today.toString(), String.format("%.2f", basePrice).toDouble()))

        return StockPriceTrend(
            ticker = upperTicker,
            interval = "3-month",
            prices = prices
        )
    }
}
