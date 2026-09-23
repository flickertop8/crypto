package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketAsset
import com.example.ui.theme.*

@Composable
fun MarketIndicesGrid(
    assets: List<MarketAsset>,
    onAssetClick: (MarketAsset) -> Unit,
    modifier: Modifier = Modifier
) {
    // Select the 4 key indices from screenshot
    val nifty = assets.find { it.id == "nifty" } ?: assets.getOrNull(0)
    val sensex = assets.find { it.id == "sensex" } ?: assets.getOrNull(1)
    val usd = assets.find { it.id == "usdink" } ?: assets.getOrNull(2)
    val metal = assets.find { it.id == "bse_metal" } ?: assets.getOrNull(3)

    val items = listOfNotNull(nifty, sensex, usd, metal)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        // Section Header: "আজকের মার্কেট"
        Text(
            text = "আজকের মার্কেট",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 2x2 Grid using Rows and Columns
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items.getOrNull(0)?.let { asset ->
                MarketIndexCard(
                    asset = asset,
                    onClick = { onAssetClick(asset) },
                    modifier = Modifier.weight(1f)
                )
            }
            items.getOrNull(1)?.let { asset ->
                MarketIndexCard(
                    asset = asset,
                    onClick = { onAssetClick(asset) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items.getOrNull(2)?.let { asset ->
                MarketIndexCard(
                    asset = asset,
                    onClick = { onAssetClick(asset) },
                    modifier = Modifier.weight(1f)
                )
            }
            items.getOrNull(3)?.let { asset ->
                MarketIndexCard(
                    asset = asset,
                    onClick = { onAssetClick(asset) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MarketIndexCard(
    asset: MarketAsset,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPositive = asset.change >= 0

    // Subtle flash on real-time price tick
    val targetBorderColor = when (asset.tickDirection) {
        1 -> NeonGreenLight
        -1 -> CrimsonRed
        else -> DarkBorderSubtle
    }
    val animatedBorder by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(600),
        label = "tickBorder"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, animatedBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("index_card_${asset.id}")
            .padding(14.dp)
    ) {
        Column {
            // Title & Change Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = asset.bengaliName,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )

                // Colored Change Text with +/-
                val changeStr = if (isPositive) "+${asset.change}" else "${asset.change}"
                Text(
                    text = changeStr,
                    color = if (isPositive) NeonGreen else CrimsonRed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Current Price & Arrow Indicator Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("%,.2f", asset.currentPrice),
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = if (isPositive) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = if (isPositive) "Up" else "Down",
                    tint = if (isPositive) NeonGreen else CrimsonRed,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
