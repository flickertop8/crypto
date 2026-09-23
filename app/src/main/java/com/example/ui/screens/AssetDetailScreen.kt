package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketAsset
import com.example.data.model.TransactionType
import com.example.ui.components.KeyStatisticsCard
import com.example.ui.components.StockDetailChart
import com.example.ui.components.TradeModal
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarketViewModel

@Composable
fun AssetDetailScreen(
    assetId: String,
    viewModel: MarketViewModel,
    onBackClick: () -> Unit,
    onOpenWallet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val assets by viewModel.assets.collectAsState()
    val cashBalance by viewModel.cashBalance.collectAsState()
    val liveHoldings by viewModel.liveHoldings.collectAsState()

    val asset = assets.find { it.id == assetId } ?: assets.firstOrNull() ?: return
    val userHolding = liveHoldings.find { it.assetId == asset.id }

    var tradeType by remember { mutableStateOf<TransactionType?>(null) }
    var showAlertSetup by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Top Bar matching Screen 3: Back button, "3 Dec 2021 \n 4:40 PM", Wallet button
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "3 Dec 2021",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "4:40 PM",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ElectricBlue)
                            .clickable(onClick = onOpenWallet)
                            .testTag("detail_wallet_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Asset Header matching Screen 3: Logo, Name, Ticker, BUY button ("কিনুন")
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Logo in rounded square
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = asset.symbol.take(4),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = asset.bengaliName,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = asset.symbol,
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Green Action Button: "কিনুন" (BUY) matching Screen 3
                    Button(
                        onClick = { tradeType = TransactionType.BUY },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("header_buy_button")
                    ) {
                        Text(
                            text = "কিনুন",
                            color = Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Interactive Live Glowing Chart matching Screen 3
            item {
                StockDetailChart(
                    asset = asset
                )
            }

            // Key Statistics Card matching Screen 3: Range Slider & Financial Metrics
            item {
                KeyStatisticsCard(
                    asset = asset,
                    onBuyClick = { tradeType = TransactionType.BUY },
                    onSellClick = { tradeType = TransactionType.SELL },
                    onAlertClick = { showAlertSetup = true }
                )
            }

            // About Company / Crypto Description
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, DarkBorderSubtle, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "বিবরণ (About)",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (asset.bengaliDescription.isNotBlank()) asset.bengaliDescription else asset.description,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Buy / Sell Bottom Sheet Modal
        if (tradeType != null) {
            TradeModal(
                asset = asset,
                type = tradeType!!,
                cashBalance = cashBalance,
                ownedShares = userHolding?.shares ?: 0.0,
                onDismiss = { tradeType = null },
                onConfirmTrade = { shares, pin ->
                    if (tradeType == TransactionType.BUY) {
                        viewModel.executeBuy(asset.id, shares, pin) {
                            tradeType = null
                        }
                    } else {
                        viewModel.executeSell(asset.id, shares, pin) {
                            tradeType = null
                        }
                    }
                }
            )
        }

        // Price Alert Dialog
        if (showAlertSetup) {
            PriceAlertDialog(
                asset = asset,
                onDismiss = { showAlertSetup = false },
                onSaveAlert = { target, isAbove ->
                    viewModel.addPriceAlert(asset.id, target, isAbove)
                    showAlertSetup = false
                }
            )
        }
    }
}

@Composable
fun PriceAlertDialog(
    asset: MarketAsset,
    onDismiss: () -> Unit,
    onSaveAlert: (target: Double, isAbove: Boolean) -> Unit
) {
    var targetInput by remember { mutableStateOf(String.format("%.2f", asset.currentPrice * 1.05)) }
    var isAbove by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("প্রাইস অ্যালার্ট সেট করুন", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "${asset.bengaliName} (${asset.symbol}) এর জন্য পুশ নোটিফিকেশন সেট করুন। লক্ষ্যমাত্রায় পৌঁছালে তাৎক্ষণিক নোটিফিকেশন পাঠানো হবে।",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isAbove,
                        onClick = { isAbove = true },
                        label = { Text("মূল্য উপরে উঠলে (>=)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = !isAbove,
                        onClick = { isAbove = false },
                        label = { Text("মূল্য নিচে নামলে (<=)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CrimsonRed,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = targetInput,
                    onValueChange = { targetInput = it },
                    label = { Text("টার্গেট মূল্য (${asset.currency})") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = targetInput.toDoubleOrNull() ?: asset.currentPrice
                    onSaveAlert(p, isAbove)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
            ) {
                Text("অ্যালার্ট সক্রিয় করুন", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = TextSecondary)
            }
        }
    )
}
