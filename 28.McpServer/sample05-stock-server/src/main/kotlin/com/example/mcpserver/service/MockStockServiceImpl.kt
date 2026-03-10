package com.example.mcpserver.service

import org.springframework.stereotype.Service
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

    override fun getStockInfo(ticker: String): StockInfo {
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

    override fun getStockPriceTrend(ticker: String): StockPriceTrend {
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
