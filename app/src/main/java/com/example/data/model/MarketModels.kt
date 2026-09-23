package com.example.data.model

enum class AssetCategory {
    STUDIO,
    MAJOR,      // প্রধান সমূহ
    FUTURES,    // ফিউচার্স
    CRYPTO,     // ক্রিপ্টো
    COMMODITIES // কমোডিটি
}

enum class AssetType {
    STOCK,
    CRYPTO,
    INDEX,
    COMMODITY
}

data class MarketAsset(
    val id: String,
    val symbol: String,
    val name: String,
    val bengaliName: String,
    val category: AssetCategory,
    val type: AssetType,
    val currentPrice: Double,
    val change: Double,
    val changePercent: Double,
    val currency: String = "₹",
    val sparkline: List<Float>,
    val high24h: Double,
    val low24h: Double,
    val high52w: Double,
    val low52w: Double,
    val volume: String,
    val marketCap: String,
    val peRatio: String,
    val beta: String = "1.12",
    val description: String = "",
    val bengaliDescription: String = "",
    val isFavorite: Boolean = false,
    val tickDirection: Int = 0 // 1 for tick up, -1 for tick down, 0 neutral
)

data class PortfolioHolding(
    val id: Long = 0,
    val assetId: String,
    val symbol: String,
    val name: String,
    val bengaliName: String,
    val shares: Double,
    val averageBuyPrice: Double,
    val currentPrice: Double,
    val currency: String = "₹",
    val totalInvested: Double = shares * averageBuyPrice,
    val currentValue: Double = shares * currentPrice,
    val profitLoss: Double = currentValue - totalInvested,
    val profitLossPercent: Double = if (totalInvested > 0) ((currentValue - totalInvested) / totalInvested) * 100 else 0.0
)

enum class TransactionType {
    BUY, SELL
}

data class TransactionRecord(
    val id: Long = 0,
    val assetId: String,
    val symbol: String,
    val name: String,
    val bengaliName: String,
    val type: TransactionType,
    val shares: Double,
    val pricePerUnit: Double,
    val totalAmount: Double,
    val currency: String = "₹",
    val timestamp: Long = System.currentTimeMillis()
)

data class PriceAlert(
    val id: Long = 0,
    val assetId: String,
    val symbol: String,
    val name: String,
    val targetPrice: Double,
    val isAbove: Boolean, // true: alert when price >= target, false: price <= target
    val isActive: Boolean = true,
    val triggeredAt: Long? = null,
    val currency: String = "₹"
)

enum class NotificationType {
    PRICE_ALERT,
    PORTFOLIO_UPDATE,
    SECURITY_ALERT,
    TRANSACTION
}

data class PushNotificationItem(
    val id: Long = 0,
    val title: String,
    val bengaliTitle: String,
    val message: String,
    val bengaliMessage: String,
    val type: NotificationType,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val targetAssetId: String? = null
)

data class SecurityState(
    val isPinSet: Boolean = true,
    val pinHash: String = "1234", // Default PIN 1234
    val isAppLocked: Boolean = false,
    val isBiometricsEnabled: Boolean = true,
    val isTwoFactorEnabled: Boolean = true,
    val requirePinForTransactions: Boolean = true,
    val autoLockTimeoutMinutes: Int = 5
)
