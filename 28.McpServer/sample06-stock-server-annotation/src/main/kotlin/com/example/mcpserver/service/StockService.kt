package com.example.mcpserver.service

data class StockInfo(
    val ticker: String,
    val companyName: String,
    val currentPrice: Double,
    val currency: String
)

data class PricePoint(
    val date: String,
    val price: Double
)

data class StockPriceTrend(
    val ticker: String,
    val interval: String,
    val prices: List<PricePoint>
)

interface StockService {
    fun getStockInfo(ticker: String): StockInfo
    fun getStockPriceTrend(ticker: String): StockPriceTrend
}
