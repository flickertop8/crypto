package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio_holdings")
data class HoldingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assetId: String,
    val symbol: String,
    val name: String,
    val bengaliName: String,
    val shares: Double,
    val averageBuyPrice: Double,
    val currency: String
)

@Entity(tableName = "transaction_records")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assetId: String,
    val symbol: String,
    val name: String,
    val bengaliName: String,
    val type: String, // BUY or SELL
    val shares: Double,
    val pricePerUnit: Double,
    val totalAmount: Double,
    val currency: String,
    val timestamp: Long
)

@Entity(tableName = "price_alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assetId: String,
    val symbol: String,
    val name: String,
    val targetPrice: Double,
    val isAbove: Boolean,
    val isActive: Boolean,
    val triggeredAt: Long?,
    val currency: String
)

@Entity(tableName = "push_notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val bengaliTitle: String,
    val message: String,
    val bengaliMessage: String,
    val type: String,
    val timestamp: Long,
    val isRead: Boolean,
    val targetAssetId: String?
)
