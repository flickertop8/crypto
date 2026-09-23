package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketAsset
import com.example.ui.theme.*

@Composable
fun KeyStatisticsCard(
    asset: MarketAsset,
    onBuyClick: () -> Unit,
    onSellClick: () -> Unit,
    onAlertClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("Overview") }
    val tabs = listOf("Overview", "News", "Health", "Technical")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Tab Row: Overview, News, Health, Technical matching Screen 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEach { tab ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) ElectricBlue else DarkSurfaceVariant)
                        .clickable { selectedTab = tab }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        color = if (isSelected) Color.White else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section: "মূল পরিসংখ্যান" (Key Statistics)
        Text(
            text = "মূল পরিসংখ্যান",
            color = TextPrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Range Slider / Metric matching Screen 3
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceVariant)
                .border(1.dp, DarkBorderSubtle, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Data/Range (দিন ও বছরের রেঞ্জ)",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive-looking Custom Track with thumb
                val currentP = asset.currentPrice.toFloat()
                val minP = asset.low52w.toFloat()
                val maxP = asset.high52w.toFloat()
                val progress = if (maxP > minP) ((currentP - minP) / (maxP - minP)).coerceIn(0.05f, 0.95f) else 0.5f

                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // Background line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(DarkBorder)
                        )
                        // Active colored line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(ElectricBlue)
                        )
                        // Thumb circle
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .wrapContentWidth(Alignment.End)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(3.dp, ElectricBlue, CircleShape)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${asset.currency}${asset.low52w}",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "বর্তমান: ${asset.currency}${asset.currentPrice}",
                            color = NeonGreenLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${asset.currency}${asset.high52w}",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Grid of Key Statistics (Market Cap, Volume, 52W High, P/E Ratio, Beta)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceVariant)
                .border(1.dp, DarkBorderSubtle, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem(label = "মার্কেট ক্যাপ", value = asset.marketCap, modifier = Modifier.weight(1f))
                StatisticItem(label = "ভলিউম (২৪ঘণ্টা)", value = asset.volume, modifier = Modifier.weight(1f))
            }
            Divider(color = DarkBorderSubtle, thickness = 0.8.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem(label = "পি/ই রেশিও (P/E)", value = asset.peRatio, modifier = Modifier.weight(1f))
                StatisticItem(label = "বেটা (Beta)", value = asset.beta, modifier = Modifier.weight(1f))
            }
            Divider(color = DarkBorderSubtle, thickness = 0.8.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatisticItem(label = "আজকের সর্বোচ্চ", value = "${asset.currency}${asset.high24h}", modifier = Modifier.weight(1f))
                StatisticItem(label = "আজকের সর্বনিম্ন", value = "${asset.currency}${asset.low24h}", modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Action Buttons: Buy (কিনুন), Sell, and Alert
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Neon Green BUY Button ("কিনুন")
            Button(
                onClick = onBuyClick,
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1.4f)
                    .height(52.dp)
            ) {
                Text(
                    text = "কিনুন (BUY)",
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // SELL Button
            OutlinedButton(
                onClick = onSellClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonRed),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, CrimsonRed),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text(
                    text = "বিক্রি (SELL)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Price Alert Button
            IconButton(
                onClick = onAlertClick,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            ) {
                Text(text = "🔔", fontSize = 20.sp)
            }
        }
    }
}

@Composable
private fun StatisticItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = label, color = TextSecondary, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
