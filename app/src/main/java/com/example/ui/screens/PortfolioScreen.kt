package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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
import com.example.data.model.PortfolioHolding
import com.example.data.model.TransactionType
import com.example.ui.components.MarketTopBar
import com.example.ui.components.PortfolioOverviewCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarketViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PortfolioScreen(
    viewModel: MarketViewModel,
    onNavigateToDetail: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenWallet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.portfolioSummary.collectAsState()
    val liveHoldings by viewModel.liveHoldings.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val cashBalance by viewModel.cashBalance.collectAsState()

    var showDepositDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            MarketTopBar(
                onAvatarClick = onOpenProfile,
                onWalletClick = onOpenWallet,
                formattedDateTime = "11 Dec 2021\n4:40 PM"
            )
        }

        // Portfolio Blue Card
        item {
            PortfolioOverviewCard(
                summary = summary
            )
        }

        // Quick Funds Actions (Deposit / Withdraw)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showDepositDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Deposit", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ফান্ড জমা করুন (Deposit)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onOpenWallet,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Text("ওয়ালেট ব্যালেন্স", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Section: আপনার হোল্ডিংস (Your Holdings)
        item {
            Text(
                text = "আপনার হোল্ডিংস (${liveHoldings.size})",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
            )
        }

        if (liveHoldings.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceVariant)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "আপনার কোনো সক্রিয় শেয়ার নেই। মার্কেট থেকে শেয়ার বা ক্রিপ্টো ক্রয় করুন।",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(liveHoldings, key = { it.id }) { holding ->
                HoldingItemCard(
                    holding = holding,
                    onClick = { onNavigateToDetail(holding.assetId) }
                )
            }
        }

        // Section: সাম্প্রতিক লেনদেন (Recent Transactions)
        item {
            Text(
                text = "সাম্প্রতিক লেনদেন ইতিহাস",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
            )
        }

        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceVariant)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("কোনো লেনদেন রেকর্ড পাওয়া যায়নি", color = TextTertiary, fontSize = 13.sp)
                }
            }
        } else {
            items(transactions.take(8), key = { it.id }) { tx ->
                TransactionItemRow(
                    symbol = tx.symbol,
                    name = tx.bengaliName,
                    type = tx.type,
                    shares = tx.shares,
                    price = tx.pricePerUnit,
                    total = tx.totalAmount,
                    currency = tx.currency,
                    timestamp = tx.timestamp
                )
            }
        }
    }

    if (showDepositDialog) {
        DepositFundsDialog(
            onDismiss = { showDepositDialog = false },
            onDeposit = { amount ->
                viewModel.depositFunds(amount)
                showDepositDialog = false
            }
        )
    }
}

@Composable
fun HoldingItemCard(
    holding: PortfolioHolding,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isProfit = holding.profitLoss >= 0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkBorderSubtle, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("holding_item_${holding.assetId}")
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = holding.bengaliName,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${holding.shares} টি শেয়ার • গড়: ${holding.currency}${String.format("%.2f", holding.averageBuyPrice)}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${holding.currency}${String.format("%,.2f", holding.currentValue)}",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${if (isProfit) "+" else ""}${String.format("%.2f", holding.profitLoss)} (${if (isProfit) "+" else ""}${String.format("%.1f", holding.profitLossPercent)}%)",
                        color = if (isProfit) NeonGreen else CrimsonRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItemRow(
    symbol: String,
    name: String,
    type: String,
    shares: Double,
    price: Double,
    total: Double,
    currency: String,
    timestamp: Long,
    modifier: Modifier = Modifier
) {
    val isBuy = type == TransactionType.BUY.name
    val timeStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(timestamp))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceVariant)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isBuy) NeonGreenBg else CrimsonRedBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isBuy) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = type,
                        tint = if (isBuy) NeonGreenLight else CrimsonRed,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "$symbol • ${if (isBuy) "ক্রয়" else "বিক্রয়"}",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = timeStr,
                        color = TextTertiary,
                        fontSize = 10.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isBuy) "-" else "+"}$currency${total.toInt()}",
                    color = if (isBuy) TextPrimary else NeonGreenLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$shares shares @ $price",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun DepositFundsDialog(
    onDismiss: () -> Unit,
    onDeposit: (Double) -> Unit
) {
    var amountInput by remember { mutableStateOf("50000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("ফান্ড জমা করুন (Add Money)", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "ট্রেডিং ওয়ালেটে তাৎক্ষণিক ফান্ড যুক্ত করুন:",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("পরিমাণ (টাকা/রুপি)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("10000", "25000", "50000", "100000").forEach { amt ->
                        SuggestionChip(
                            onClick = { amountInput = amt },
                            label = { Text("+$amt", fontSize = 10.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val a = amountInput.toDoubleOrNull() ?: 10000.0
                    onDeposit(a)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
            ) {
                Text("জমা নিশ্চিত করুন", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = TextSecondary)
            }
        }
    )
}
