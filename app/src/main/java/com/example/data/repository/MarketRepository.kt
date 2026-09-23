package com.example.data.repository

import android.content.Context
import com.example.data.db.*
import com.example.data.model.*
import com.example.notifications.NotificationHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.roundToInt
import kotlin.random.Random

class MarketRepository(
    private val context: Context,
    private val db: AppDatabase = AppDatabase.getDatabase(context),
    private val notificationHelper: NotificationHelper = NotificationHelper(context),
    private val repositoryScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {

    private val holdingDao = db.holdingDao()
    private val transactionDao = db.transactionDao()
    private val alertDao = db.alertDao()
    private val notificationDao = db.notificationDao()

    // Currency setting ("₹", "$", "৳")
    private val _currency = MutableStateFlow("₹")
    val currency: StateFlow<String> = _currency.asStateFlow()

    // Language setting ("bn" for Bengali, "en" for English)
    private val _language = MutableStateFlow("bn")
    val language: StateFlow<String> = _language.asStateFlow()

    // Cash Balance
    private val _cashBalance = MutableStateFlow(150000.0)
    val cashBalance: StateFlow<Double> = _cashBalance.asStateFlow()

    // Security State
    private val _securityState = MutableStateFlow(
        SecurityState(
            isPinSet = true,
            pinHash = "1234",
            isAppLocked = false,
            isBiometricsEnabled = true,
            isTwoFactorEnabled = true,
            requirePinForTransactions = true
        )
    )
    val securityState: StateFlow<SecurityState> = _securityState.asStateFlow()

    // Master list of Assets with real-time updates
    private val _assets = MutableStateFlow<List<MarketAsset>>(initialAssets())
    val assets: StateFlow<List<MarketAsset>> = _assets.asStateFlow()

    // Live Ticker is running
    private val _isLiveSimulationActive = MutableStateFlow(true)
    val isLiveSimulationActive: StateFlow<Boolean> = _isLiveSimulationActive.asStateFlow()

    // Database Flows
    val holdingsFlow: Flow<List<HoldingEntity>> = holdingDao.getAllHoldings()
    val transactionsFlow: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val alertsFlow: Flow<List<AlertEntity>> = alertDao.getAllAlerts()
    val notificationsFlow: Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()
    val unreadNotificationCount: Flow<Int> = notificationDao.getUnreadCount()

    // Combined live holdings with live prices
    val liveHoldings: Flow<List<PortfolioHolding>> = combine(holdingsFlow, _assets) { dbHoldings, currentAssets ->
        dbHoldings.map { entity ->
            val asset = currentAssets.find { it.id == entity.assetId }
            val currentPrice = asset?.currentPrice ?: entity.averageBuyPrice
            PortfolioHolding(
                id = entity.id,
                assetId = entity.assetId,
                symbol = entity.symbol,
                name = entity.name,
                bengaliName = entity.bengaliName,
                shares = entity.shares,
                averageBuyPrice = entity.averageBuyPrice,
                currentPrice = currentPrice,
                currency = entity.currency
            )
        }
    }

    init {
        // Seed initial portfolio & notifications if empty
        repositoryScope.launch {
            seedInitialData()
            startPriceSimulation()
        }
    }

    private suspend fun seedInitialData() {
        val existing = holdingDao.getHoldingByAssetId("tata")
        if (existing == null) {
            // Seed initial holdings matching screenshot:
            // "বর্তমান মূল্য" 12,30,500 total valuation
            holdingDao.insertOrUpdateHolding(
                HoldingEntity(
                    assetId = "tata",
                    symbol = "TATA",
                    name = "Tata Motors Limited",
                    bengaliName = "টাটা মোটরস লিমিটেড",
                    shares = 2500.0,
                    averageBuyPrice = 133.50,
                    currency = "₹"
                )
            )
            holdingDao.insertOrUpdateHolding(
                HoldingEntity(
                    assetId = "aapl",
                    symbol = "AAPL",
                    name = "Apple Inc.",
                    bengaliName = "অ্যাপল ইনক.",
                    shares = 1200.0,
                    averageBuyPrice = 113.20,
                    currency = "₹"
                )
            )
            holdingDao.insertOrUpdateHolding(
                HoldingEntity(
                    assetId = "infy",
                    symbol = "INFY",
                    name = "Infosys Limited",
                    bengaliName = "ইনফোসিস লিমিটেড",
                    shares = 850.0,
                    averageBuyPrice = 286.90,
                    currency = "₹"
                )
            )
            holdingDao.insertOrUpdateHolding(
                HoldingEntity(
                    assetId = "btc",
                    symbol = "BTC",
                    name = "Bitcoin",
                    bengaliName = "বিটকয়েন",
                    shares = 0.08,
                    averageBuyPrice = 59800.0,
                    currency = "$"
                )
            )

            // Seed initial notifications
            notificationDao.insertNotification(
                NotificationEntity(
                    title = "Market Open Alert",
                    bengaliTitle = "মার্কেট ওপেন নোটিফিকেশন",
                    message = "Today's market indices are showing active movement.",
                    bengaliMessage = "আজকের সূচকসমূহে ঊর্ধ্বমুখী প্রবণতা লক্ষ্য করা যাচ্ছে।",
                    type = NotificationType.PORTFOLIO_UPDATE.name,
                    timestamp = System.currentTimeMillis() - 3600000,
                    isRead = false,
                    targetAssetId = "tata"
                )
            )
            notificationDao.insertNotification(
                NotificationEntity(
                    title = "Price Target Reached!",
                    bengaliTitle = "টার্গেট প্রাইস সফল!",
                    message = "Tata Motors (TATA) hit your milestone of ₹175.00 (+32%).",
                    bengaliMessage = "টাটা মোটরস (TATA) আপনার লক্ষ্যমাত্রা ₹১৭৫.০০ অতিক্রম করেছে (+৩২%)।",
                    type = NotificationType.PRICE_ALERT.name,
                    timestamp = System.currentTimeMillis() - 7200000,
                    isRead = false,
                    targetAssetId = "tata"
                )
            )
            notificationDao.insertNotification(
                NotificationEntity(
                    title = "Security Alert",
                    bengaliTitle = "নিরাপত্তা সতর্কতা",
                    message = "Biometric & 2-Factor Authentication are active.",
                    bengaliMessage = "বায়োমেট্রিক ও দ্বি-স্তরীয় যাচাইকরণ সক্রিয় রয়েছে।",
                    type = NotificationType.SECURITY_ALERT.name,
                    timestamp = System.currentTimeMillis() - 18000000,
                    isRead = true,
                    targetAssetId = null
                )
            )
        }
    }

    private fun startPriceSimulation() {
        repositoryScope.launch {
            while (isActive) {
                delay(2200)
                if (!_isLiveSimulationActive.value) continue

                val currentList = _assets.value.toMutableList()
                val randomIndex = Random.nextInt(currentList.size)
                val target = currentList[randomIndex]

                // Random fluctuation: -0.8% to +0.8%
                val deltaPercent = (Random.nextDouble(-0.8, 0.85) * 100).roundToInt() / 100.0
                val deltaPrice = target.currentPrice * (deltaPercent / 100.0)
                val newPrice = ((target.currentPrice + deltaPrice) * 100.0).roundToInt() / 100.0
                val newChange = ((target.change + deltaPrice) * 100.0).roundToInt() / 100.0
                val newChangePercent = ((target.changePercent + deltaPercent) * 100.0).roundToInt() / 100.0

                // Update sparkline points
                val updatedSparkline = (target.sparkline.drop(1) + (newPrice.toFloat())).takeLast(14)
                val tickDir = if (deltaPrice > 0) 1 else if (deltaPrice < 0) -1 else 0

                currentList[randomIndex] = target.copy(
                    currentPrice = newPrice,
                    change = newChange,
                    changePercent = newChangePercent,
                    sparkline = updatedSparkline,
                    tickDirection = tickDir
                )

                _assets.value = currentList

                // Check Price Alerts
                checkAlerts(target.id, newPrice, target.name, target.bengaliName)
            }
        }
    }

    private suspend fun checkAlerts(assetId: String, currentPrice: Double, name: String, bengaliName: String) {
        val activeAlerts = alertDao.getActiveAlerts()
        for (alert in activeAlerts) {
            if (alert.assetId == assetId) {
                val isTriggered = if (alert.isAbove) {
                    currentPrice >= alert.targetPrice
                } else {
                    currentPrice <= alert.targetPrice
                }

                if (isTriggered) {
                    // Update alert in DB
                    alertDao.updateAlert(alert.copy(isActive = false, triggeredAt = System.currentTimeMillis()))

                    val directionText = if (alert.isAbove) "exceeded" else "dropped below"
                    val bengaliDir = if (alert.isAbove) "অতিক্রম করেছে" else "নিচে নেমে গেছে"
                    val enTitle = "Price Alert: ${alert.symbol}"
                    val bnTitle = "প্রাইস অ্যালার্ট: ${alert.symbol}"
                    val enMsg = "$name has $directionText ${alert.currency}${alert.targetPrice}. Current: ${alert.currency}$currentPrice"
                    val bnMsg = "$bengaliName ${alert.currency}${alert.targetPrice} $bengaliDir। বর্তমান মূল্য: ${alert.currency}$currentPrice"

                    // Post real system push notification
                    notificationHelper.postNotification(
                        title = if (_language.value == "bn") bnTitle else enTitle,
                        message = if (_language.value == "bn") bnMsg else enMsg,
                        channelId = NotificationHelper.CHANNEL_PRICE_ALERTS
                    )

                    // Save to Notification database
                    notificationDao.insertNotification(
                        NotificationEntity(
                            title = enTitle,
                            bengaliTitle = bnTitle,
                            message = enMsg,
                            bengaliMessage = bnMsg,
                            type = NotificationType.PRICE_ALERT.name,
                            timestamp = System.currentTimeMillis(),
                            isRead = false,
                            targetAssetId = assetId
                        )
                    )
                }
            }
        }
    }

    // Trade actions
    suspend fun executeBuy(
        assetId: String,
        shares: Double,
        securityPin: String
    ): Result<TransactionRecord> {
        if (_securityState.value.requirePinForTransactions && securityPin != _securityState.value.pinHash) {
            return Result.failure(Exception("ভুল পিন নম্বর! লেনদেন বাতিল করা হয়েছে। (Invalid PIN)"))
        }

        val asset = _assets.value.find { it.id == assetId }
            ?: return Result.failure(Exception("সম্পদ পাওয়া যায়নি (Asset not found)"))

        val totalCost = shares * asset.currentPrice
        if (totalCost > _cashBalance.value) {
            return Result.failure(Exception("পর্যাপ্ত ব্যালেন্স নেই! প্রয়োজন: ${asset.currency}${totalCost.toInt()} (Insufficient balance)"))
        }

        // Deduct cash
        _cashBalance.value -= totalCost

        // Update Holdings
        val existingHolding = holdingDao.getHoldingByAssetId(assetId)
        if (existingHolding != null) {
            val totalShares = existingHolding.shares + shares
            val newAvgPrice = ((existingHolding.shares * existingHolding.averageBuyPrice) + totalCost) / totalShares
            holdingDao.updateHolding(
                existingHolding.copy(
                    shares = totalShares,
                    averageBuyPrice = newAvgPrice
                )
            )
        } else {
            holdingDao.insertOrUpdateHolding(
                HoldingEntity(
                    assetId = asset.id,
                    symbol = asset.symbol,
                    name = asset.name,
                    bengaliName = asset.bengaliName,
                    shares = shares,
                    averageBuyPrice = asset.currentPrice,
                    currency = asset.currency
                )
            )
        }

        // Record Transaction
        val tx = TransactionEntity(
            assetId = asset.id,
            symbol = asset.symbol,
            name = asset.name,
            bengaliName = asset.bengaliName,
            type = TransactionType.BUY.name,
            shares = shares,
            pricePerUnit = asset.currentPrice,
            totalAmount = totalCost,
            currency = asset.currency,
            timestamp = System.currentTimeMillis()
        )
        val txId = transactionDao.insertTransaction(tx)

        // Post Push Notification
        val bnTitle = "ক্রয় সফল হয়েছে! (${asset.symbol})"
        val enTitle = "Buy Order Completed! (${asset.symbol})"
        val bnMsg = "${asset.bengaliName} এর $shares টি শেয়ার ${asset.currency}${asset.currentPrice} দরে ক্রয় করা হয়েছে।"
        val enMsg = "Successfully purchased $shares shares of ${asset.name} at ${asset.currency}${asset.currentPrice}."

        notificationHelper.postNotification(
            title = if (_language.value == "bn") bnTitle else enTitle,
            message = if (_language.value == "bn") bnMsg else enMsg,
            channelId = NotificationHelper.CHANNEL_PORTFOLIO
        )

        notificationDao.insertNotification(
            NotificationEntity(
                title = enTitle,
                bengaliTitle = bnTitle,
                message = enMsg,
                bengaliMessage = bnMsg,
                type = NotificationType.TRANSACTION.name,
                timestamp = System.currentTimeMillis(),
                isRead = false,
                targetAssetId = asset.id
            )
        )

        return Result.success(
            TransactionRecord(
                id = txId,
                assetId = asset.id,
                symbol = asset.symbol,
                name = asset.name,
                bengaliName = asset.bengaliName,
                type = TransactionType.BUY,
                shares = shares,
                pricePerUnit = asset.currentPrice,
                totalAmount = totalCost,
                currency = asset.currency,
                timestamp = tx.timestamp
            )
        )
    }

    suspend fun executeSell(
        assetId: String,
        shares: Double,
        securityPin: String
    ): Result<TransactionRecord> {
        if (_securityState.value.requirePinForTransactions && securityPin != _securityState.value.pinHash) {
            return Result.failure(Exception("ভুল পিন নম্বর! লেনদেন বাতিল করা হয়েছে। (Invalid PIN)"))
        }

        val holding = holdingDao.getHoldingByAssetId(assetId)
            ?: return Result.failure(Exception("আপনার পোর্টফোলিওতে এই শেয়ারটি নেই (No shares owned)"))

        if (shares > holding.shares) {
            return Result.failure(Exception("পর্যাপ্ত শেয়ার নেই! আপনার আছে: ${holding.shares} টি"))
        }

        val asset = _assets.value.find { it.id == assetId }
            ?: return Result.failure(Exception("সম্পদ পাওয়া যায়নি"))

        val totalEarned = shares * asset.currentPrice

        // Add cash
        _cashBalance.value += totalEarned

        // Update Holdings
        val remainingShares = holding.shares - shares
        if (remainingShares <= 0.0001) {
            holdingDao.deleteByAssetId(assetId)
        } else {
            holdingDao.updateHolding(holding.copy(shares = remainingShares))
        }

        // Record Transaction
        val tx = TransactionEntity(
            assetId = asset.id,
            symbol = asset.symbol,
            name = asset.name,
            bengaliName = asset.bengaliName,
            type = TransactionType.SELL.name,
            shares = shares,
            pricePerUnit = asset.currentPrice,
            totalAmount = totalEarned,
            currency = asset.currency,
            timestamp = System.currentTimeMillis()
        )
        val txId = transactionDao.insertTransaction(tx)

        // Post Push Notification
        val bnTitle = "বিক্রয় সম্পন্ন হয়েছে! (${asset.symbol})"
        val enTitle = "Sell Order Executed! (${asset.symbol})"
        val bnMsg = "${asset.bengaliName} এর $shares টি শেয়ার বিক্রি করে ${asset.currency}${totalEarned.toInt()} গ্রহণ করা হয়েছে।"
        val enMsg = "Sold $shares shares of ${asset.name} for ${asset.currency}${totalEarned.toInt()}."

        notificationHelper.postNotification(
            title = if (_language.value == "bn") bnTitle else enTitle,
            message = if (_language.value == "bn") bnMsg else enMsg,
            channelId = NotificationHelper.CHANNEL_PORTFOLIO
        )

        notificationDao.insertNotification(
            NotificationEntity(
                title = enTitle,
                bengaliTitle = bnTitle,
                message = enMsg,
                bengaliMessage = bnMsg,
                type = NotificationType.TRANSACTION.name,
                timestamp = System.currentTimeMillis(),
                isRead = false,
                targetAssetId = asset.id
            )
        )

        return Result.success(
            TransactionRecord(
                id = txId,
                assetId = asset.id,
                symbol = asset.symbol,
                name = asset.name,
                bengaliName = asset.bengaliName,
                type = TransactionType.SELL,
                shares = shares,
                pricePerUnit = asset.currentPrice,
                totalAmount = totalEarned,
                currency = asset.currency,
                timestamp = tx.timestamp
            )
        )
    }

    suspend fun addPriceAlert(
        assetId: String,
        targetPrice: Double,
        isAbove: Boolean
    ): Long {
        val asset = _assets.value.find { it.id == assetId }
        val symbol = asset?.symbol ?: "ALERT"
        val name = asset?.name ?: "Asset"

        val alertEntity = AlertEntity(
            assetId = assetId,
            symbol = symbol,
            name = name,
            targetPrice = targetPrice,
            isAbove = isAbove,
            isActive = true,
            triggeredAt = null,
            currency = asset?.currency ?: "₹"
        )
        return alertDao.insertAlert(alertEntity)
    }

    suspend fun deletePriceAlert(alertId: Long) {
        val alert = alertDao.getAllAlerts().first().find { it.id == alertId }
        if (alert != null) {
            alertDao.deleteAlert(alert)
        }
    }

    suspend fun depositFunds(amount: Double) {
        _cashBalance.value += amount
        notificationHelper.postNotification(
            title = "Deposit Successful",
            message = "Deposited ₹${amount.toInt()} to your trading balance.",
            channelId = NotificationHelper.CHANNEL_PORTFOLIO
        )
    }

    suspend fun withdrawFunds(amount: Double): Boolean {
        if (amount <= _cashBalance.value) {
            _cashBalance.value -= amount
            notificationHelper.postNotification(
                title = "Withdrawal Processed",
                message = "Withdrawn ₹${amount.toInt()} from your account.",
                channelId = NotificationHelper.CHANNEL_PORTFOLIO
            )
            return true
        }
        return false
    }

    fun toggleSimulation() {
        _isLiveSimulationActive.value = !_isLiveSimulationActive.value
    }

    fun setCurrency(newCurrency: String) {
        _currency.value = newCurrency
    }

    fun setLanguage(newLanguage: String) {
        _language.value = newLanguage
    }

    fun updateSecurityPin(oldPin: String, newPin: String): Boolean {
        if (oldPin == _securityState.value.pinHash) {
            _securityState.value = _securityState.value.copy(pinHash = newPin)
            notificationHelper.postNotification(
                title = "Security Alert",
                message = "Your trading security PIN has been updated successfully.",
                channelId = NotificationHelper.CHANNEL_SECURITY
            )
            return true
        }
        return false
    }

    fun toggleBiometrics(enabled: Boolean) {
        _securityState.value = _securityState.value.copy(isBiometricsEnabled = enabled)
    }

    fun toggleTwoFactor(enabled: Boolean) {
        _securityState.value = _securityState.value.copy(isTwoFactorEnabled = enabled)
    }

    fun toggleRequirePinForTransactions(required: Boolean) {
        _securityState.value = _securityState.value.copy(requirePinForTransactions = required)
    }

    fun lockApp() {
        _securityState.value = _securityState.value.copy(isAppLocked = true)
    }

    fun unlockApp(pin: String): Boolean {
        if (pin == _securityState.value.pinHash) {
            _securityState.value = _securityState.value.copy(isAppLocked = false)
            return true
        }
        return false
    }

    suspend fun markNotificationAsRead(id: Long) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() {
        notificationDao.markAllAsRead()
    }

    suspend fun triggerTestAlert() {
        val testAsset = _assets.value.firstOrNull { it.id == "tata" } ?: _assets.value.first()
        notificationHelper.postNotification(
            title = "Test Price Alert: ${testAsset.symbol}",
            message = "Market volatility detected! ${testAsset.name} reached ₹${testAsset.currentPrice}.",
            channelId = NotificationHelper.CHANNEL_PRICE_ALERTS
        )
        notificationDao.insertNotification(
            NotificationEntity(
                title = "Test Price Alert: ${testAsset.symbol}",
                bengaliTitle = "টেস্ট প্রাইস অ্যালার্ট: ${testAsset.symbol}",
                message = "Market volatility detected! ${testAsset.name} reached ₹${testAsset.currentPrice}.",
                bengaliMessage = "মার্কেট ভলাটিলিটি নোটিফিকেশন! ${testAsset.bengaliName} বর্তমান মূল্য ₹${testAsset.currentPrice}।",
                type = NotificationType.PRICE_ALERT.name,
                timestamp = System.currentTimeMillis(),
                isRead = false,
                targetAssetId = testAsset.id
            )
        )
    }

    private fun initialAssets(): List<MarketAsset> = listOf(
        // Screen 3 Spotlight Asset
        MarketAsset(
            id = "tata",
            symbol = "TATA",
            name = "Tata Motors Limited",
            bengaliName = "টাটা মোটরস লিমিটেড",
            category = AssetCategory.MAJOR,
            type = AssetType.STOCK,
            currentPrice = 176.23,
            change = 0.443,
            changePercent = 32.0,
            currency = "₹",
            sparkline = listOf(116.6f, 116.4f, 116.2f, 117.1f, 118.3f, 117.8f, 119.5f, 122.0f, 135.0f, 150.0f, 168.0f, 172.5f, 176.23f),
            high24h = 178.50,
            low24h = 174.10,
            high52w = 215.00,
            low52w = 75.90,
            volume = "18.4M",
            marketCap = "₹64,280 Cr",
            peRatio = "14.8",
            beta = "1.34",
            description = "Tata Motors Limited is a leading global automobile manufacturer with a portfolio of cars, SUVs, trucks, and defense vehicles.",
            bengaliDescription = "টাটা মোটরস লিমিটেড বিশ্বের অন্যতম বৃহৎ মোটরযান নির্মাতা প্রতিষ্ঠান, যা অটোমোবাইল ও ইভি সেক্টরে অগ্রণী ভূমিকা রাখছে।"
        ),
        // Screen 1 & 2 Indices
        MarketAsset(
            id = "dj",
            symbol = "DJ",
            name = "Dow Jones Industrial",
            bengaliName = "ডাও জোনস",
            category = AssetCategory.STUDIO,
            type = AssetType.INDEX,
            currentPrice = 35752.89,
            change = 620.09,
            changePercent = 6.23,
            currency = "₹",
            sparkline = listOf(35100f, 35240f, 35180f, 35350f, 35420f, 35500f, 35680f, 35752.89f),
            high24h = 35850.0,
            low24h = 35120.0,
            high52w = 36952.0,
            low52w = 30500.0,
            volume = "382M",
            marketCap = "₹8.4T",
            peRatio = "21.4"
        ),
        MarketAsset(
            id = "sp500",
            symbol = "S&PC",
            name = "S&P 500 Index",
            bengaliName = "এস অ্যান্ড পি",
            category = AssetCategory.STUDIO,
            type = AssetType.INDEX,
            currentPrice = 4696.56,
            change = 47.37,
            changePercent = 5.35,
            currency = "₹",
            sparkline = listOf(4620f, 4640f, 4630f, 4660f, 4680f, 4675f, 4690f, 4696.56f),
            high24h = 4710.0,
            low24h = 4640.0,
            high52w = 4818.0,
            low52w = 3800.0,
            volume = "2.1B",
            marketCap = "₹34T",
            peRatio = "24.2"
        ),
        MarketAsset(
            id = "nasdaq",
            symbol = "NAS",
            name = "Nasdaq Composite",
            bengaliName = "নাসডাক",
            category = AssetCategory.STUDIO,
            type = AssetType.INDEX,
            currentPrice = 15520.10,
            change = -115.82,
            changePercent = -0.74,
            currency = "₹",
            sparkline = listOf(15700f, 15650f, 15680f, 15600f, 15580f, 15540f, 15520.1f),
            high24h = 15720.0,
            low24h = 15480.0,
            high52w = 16212.0,
            low52w = 12500.0,
            volume = "4.5B",
            marketCap = "₹22T",
            peRatio = "28.5"
        ),
        // Screen 1 Market Cards:
        MarketAsset(
            id = "nifty",
            symbol = "NIFTY 50",
            name = "Nifty 50 Index",
            bengaliName = "নিফটি ৫০",
            category = AssetCategory.MAJOR,
            type = AssetType.INDEX,
            currentPrice = 17522.30,
            change = -31.59,
            changePercent = -0.18,
            currency = "₹",
            sparkline = listOf(17580f, 17560f, 17570f, 17540f, 17530f, 17510f, 17522.30f),
            high24h = 17610.0,
            low24h = 17480.0,
            high52w = 18600.0,
            low52w = 14200.0,
            volume = "215M",
            marketCap = "₹120T",
            peRatio = "22.1"
        ),
        MarketAsset(
            id = "sensex",
            symbol = "SENSEX",
            name = "BSE Sensex",
            bengaliName = "সেনসেক্স",
            category = AssetCategory.MAJOR,
            type = AssetType.INDEX,
            currentPrice = 58166.60,
            change = -25.65,
            changePercent = -0.04,
            currency = "₹",
            sparkline = listOf(58250f, 58220f, 58240f, 58180f, 58150f, 58166.60f),
            high24h = 58450.0,
            low24h = 58050.0,
            high52w = 62245.0,
            low52w = 48000.0,
            volume = "18M",
            marketCap = "₹160T",
            peRatio = "25.8"
        ),
        MarketAsset(
            id = "usdink",
            symbol = "USDINK",
            name = "USD Currency Pair",
            bengaliName = "USDINK",
            category = AssetCategory.FUTURES,
            type = AssetType.COMMODITY,
            currentPrice = 76.0950,
            change = 29.30,
            changePercent = 0.38,
            currency = "₹",
            sparkline = listOf(75.8f, 75.9f, 75.85f, 76.0f, 76.05f, 76.095f),
            high24h = 76.25,
            low24h = 75.75,
            high52w = 83.50,
            low52w = 72.80,
            volume = "1.2B",
            marketCap = "-",
            peRatio = "-"
        ),
        MarketAsset(
            id = "bse_metal",
            symbol = "BSE METAL",
            name = "BSE Metal Sector",
            bengaliName = "বিএসই মেটাল",
            category = AssetCategory.COMMODITIES,
            type = AssetType.INDEX,
            currentPrice = 48364.00,
            change = 9.50,
            changePercent = 0.02,
            currency = "₹",
            sparkline = listOf(48200f, 48250f, 48310f, 48340f, 48364.00f),
            high24h = 48500.0,
            low24h = 48100.0,
            high52w = 52000.0,
            low52w = 34000.0,
            volume = "85M",
            marketCap = "₹28T",
            peRatio = "16.4"
        ),
        // Screen 2 Most Active ("সবচেয়ে সক্রিয়")
        MarketAsset(
            id = "aapl",
            symbol = "Apple",
            name = "Apple Inc.",
            bengaliName = "অ্যাপল ইনক.",
            category = AssetCategory.MAJOR,
            type = AssetType.STOCK,
            currentPrice = 116.23,
            change = 2.98,
            changePercent = 2.63,
            currency = "₹",
            sparkline = listOf(112.5f, 113.2f, 114.0f, 114.8f, 115.4f, 116.23f),
            high24h = 117.50,
            low24h = 112.00,
            high52w = 182.00,
            low52w = 95.00,
            volume = "82.5M",
            marketCap = "$2.8T",
            peRatio = "29.8",
            description = "Apple Inc. designs, manufactures, and markets smartphones, personal computers, tablets, wearables, and accessories.",
            bengaliDescription = "অ্যাপল ইনক. বিশ্বখ্যাত প্রযুক্তি কোম্পানি যারা আইফোন, ম্যাক ও ডিজিটাল সেবা উৎপাদন করে।"
        ),
        MarketAsset(
            id = "infy",
            symbol = "Infosys",
            name = "Infosys Limited",
            bengaliName = "ইনফোসিস লিমিটেড",
            category = AssetCategory.MAJOR,
            type = AssetType.STOCK,
            currentPrice = 294.41,
            change = 7.48,
            changePercent = 2.61,
            currency = "₹",
            sparkline = listOf(284.0f, 286.5f, 289.0f, 291.2f, 294.41f),
            high24h = 296.00,
            low24h = 285.50,
            high52w = 340.00,
            low52w = 210.00,
            volume = "12.8M",
            marketCap = "₹72,400 Cr",
            peRatio = "25.2",
            description = "Infosys is a global leader in next-generation digital services and consulting.",
            bengaliDescription = "ইনফোসিস লিমিটেড তথ্যপ্রযুক্তি ও সফটওয়্যার কনসাল্টিংয়ের অন্যতম শীর্ষ গ্লোবাল প্রতিষ্ঠান।"
        ),
        MarketAsset(
            id = "adidas",
            symbol = "Adidas",
            name = "Adidas AG",
            bengaliName = "আডিডাস",
            category = AssetCategory.MAJOR,
            type = AssetType.STOCK,
            currentPrice = 193.43,
            change = -0.64,
            changePercent = -0.33,
            currency = "₹",
            sparkline = listOf(196.0f, 195.2f, 194.5f, 194.0f, 193.43f),
            high24h = 197.00,
            low24h = 192.50,
            high52w = 230.00,
            low52w = 160.00,
            volume = "4.2M",
            marketCap = "€36B",
            peRatio = "32.1",
            description = "Adidas AG designs and manufactures athletic and sports lifestyle footwear, apparel and accessories.",
            bengaliDescription = "আডিডাস বিশ্বের শীর্ষস্থানীয় স্পোর্টসওয়্যার ও ফুটওয়্যার ব্র্যান্ড।"
        ),
        MarketAsset(
            id = "reliance",
            symbol = "Reliance",
            name = "Reliance Industries",
            bengaliName = "রিল্যায়েন্স ইন্ডাস্ট্রিজ",
            category = AssetCategory.MAJOR,
            type = AssetType.STOCK,
            currentPrice = 2410.50,
            change = 68.20,
            changePercent = 2.91,
            currency = "₹",
            sparkline = listOf(2340f, 2360f, 2380f, 2395f, 2410.5f),
            high24h = 2430.0,
            low24h = 2335.0,
            high52w = 2850.0,
            low52w = 1950.0,
            volume = "9.1M",
            marketCap = "₹16.5T",
            peRatio = "24.5",
            description = "Reliance Industries is an Indian multinational conglomerate with petrochemicals, telecom, and retail.",
            bengaliDescription = "রিল্যায়েন্স ইন্ডাস্ট্রিজ টেলিকম, পেট্রোকেমিক্যাল ও রিটেইলের শীর্ষ কনগ্লোমারেট।"
        ),
        MarketAsset(
            id = "hdfc",
            symbol = "HDFC",
            name = "HDFC Bank",
            bengaliName = "এইচডিএফসি ব্যাংক",
            category = AssetCategory.MAJOR,
            type = AssetType.STOCK,
            currentPrice = 1520.80,
            change = 21.75,
            changePercent = 1.45,
            currency = "₹",
            sparkline = listOf(1495f, 1502f, 1510f, 1516f, 1520.80f),
            high24h = 1535.0,
            low24h = 1490.0,
            high52w = 1720.0,
            low52w = 1380.0,
            volume = "14.2M",
            marketCap = "₹11.8T",
            peRatio = "19.3",
            description = "HDFC Bank is India's largest private sector bank by assets and market capitalization.",
            bengaliDescription = "এইচডিএফসি ব্যাংক শীর্ষস্থানীয় আর্থিক ও ব্যাংকিং সেবা প্রদানকারী প্রতিষ্ঠান।"
        ),
        // Cryptocurrencies
        MarketAsset(
            id = "btc",
            symbol = "BTC",
            name = "Bitcoin",
            bengaliName = "বিটকয়েন",
            category = AssetCategory.CRYPTO,
            type = AssetType.CRYPTO,
            currentPrice = 64250.00,
            change = 1840.50,
            changePercent = 2.95,
            currency = "$",
            sparkline = listOf(61800f, 62400f, 63100f, 62800f, 63500f, 64250f),
            high24h = 65100.0,
            low24h = 61900.0,
            high52w = 73750.0,
            low52w = 26500.0,
            volume = "$32.4B",
            marketCap = "$1.26T",
            peRatio = "-",
            description = "Bitcoin is the first decentralized digital cryptocurrency, secured through blockchain cryptography.",
            bengaliDescription = "বিটকয়েন বিশ্বের সর্বপ্রথম ও প্রধান ডিসেন্ট্রালাইজড ক্রিপ্টোকারেন্সি ডিজিটাল কারেন্সি।"
        ),
        MarketAsset(
            id = "eth",
            symbol = "ETH",
            name = "Ethereum",
            bengaliName = "ইথেরিয়াম",
            category = AssetCategory.CRYPTO,
            type = AssetType.CRYPTO,
            currentPrice = 3450.20,
            change = 62.70,
            changePercent = 1.85,
            currency = "$",
            sparkline = listOf(3360f, 3390f, 3410f, 3425f, 3450.2f),
            high24h = 3520.0,
            low24h = 3340.0,
            high52w = 4090.0,
            low52w = 1550.0,
            volume = "$16.8B",
            marketCap = "$415B",
            peRatio = "-",
            description = "Ethereum is a decentralized open-source blockchain with smart contract functionality.",
            bengaliDescription = "ইথেরিয়াম হলো স্মার্ট কন্ট্রাক্ট এবং ডিসেন্ট্রালাইজড অ্যাপ্লিকেশনের প্রধান প্ল্যাটফর্ম।"
        ),
        MarketAsset(
            id = "sol",
            symbol = "SOL",
            name = "Solana",
            bengaliName = "সোলানা",
            category = AssetCategory.CRYPTO,
            type = AssetType.CRYPTO,
            currentPrice = 152.80,
            change = 7.44,
            changePercent = 5.12,
            currency = "$",
            sparkline = listOf(142f, 144f, 147f, 149f, 152.8f),
            high24h = 158.0,
            low24h = 141.0,
            high52w = 209.0,
            low52w = 18.0,
            volume = "$4.9B",
            marketCap = "$70.5B",
            peRatio = "-",
            description = "Solana is a high-performance blockchain supporting builders to craft crypto apps that scale today.",
            bengaliDescription = "সোলানা উচ্চগতির আল্ট্রা-ফাস্ট ব্লকচেইন প্ল্যাটফর্ম।"
        )
    )
}
