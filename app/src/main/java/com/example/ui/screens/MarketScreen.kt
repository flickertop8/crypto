package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import com.example.data.model.AssetCategory
import com.example.data.model.MarketAsset
import com.example.ui.components.MarketSearchBar
import com.example.ui.components.MarketTopBar
import com.example.ui.components.SparklineChart
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarketViewModel

@Composable
fun MarketScreen(
    viewModel: MarketViewModel,
    onNavigateToDetail: (String) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenWallet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val assets by viewModel.assets.collectAsState()
    val filteredAssets by viewModel.filteredAssets.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var isSearchExpanded by remember { mutableStateOf(false) }

    val categories = listOf(
        AssetCategory.STUDIO to "Studio",
        AssetCategory.MAJOR to "প্রধান সমূহ",
        AssetCategory.FUTURES to "Index Futures",
        AssetCategory.CRYPTO to "Crypto",
        AssetCategory.COMMODITIES to "Commodities"
    )

    // Indices for horizontal scroll cards (Dow Jones, S&P, Nasdaq, BTC)
    val indexAssets = assets.filter { it.id in listOf("dj", "sp500", "nasdaq", "btc") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(bottom = 20.dp)
    ) {
        // Top Bar matching Screen 2
        item {
            MarketTopBar(
                onAvatarClick = onOpenProfile,
                onWalletClick = onOpenWallet,
                onSearchClick = { isSearchExpanded = !isSearchExpanded },
                showSearchIcon = true,
                formattedDateTime = "11 Dec 2021\n4:40 PM"
            )
        }

        // Search Bar (expandable or default)
        if (isSearchExpanded || searchQuery.isNotBlank()) {
            item {
                MarketSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.updateSearchQuery(it) }
                )
            }
        }

        // Category Filter Chips matching Screen 2
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(categories) { (category, title) ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) ElectricBlue else DarkSurfaceVariant)
                            .border(1.dp, if (isSelected) ElectricBlue else DarkBorderSubtle, RoundedCornerShape(12.dp))
                            .clickable { viewModel.selectCategory(category) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) Color.White else TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Horizontal Indices Cards with Sparklines matching Screen 2
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(indexAssets, key = { it.id }) { asset ->
                    IndexSparklineCard(
                        asset = asset,
                        onClick = { onNavigateToDetail(asset.id) }
                    )
                }
            }
        }

        // Section Title: "সবচেয়ে সক্রিয়" (Most Active) matching Screen 2
        item {
            Text(
                text = "সবচেয়ে সক্রিয়",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
            )
        }

        // Most Active List Cards (Apple, Infosys, Adidas, Reliance, etc.)
        items(filteredAssets, key = { it.id }) { asset ->
            MostActiveAssetCard(
                asset = asset,
                onClick = { onNavigateToDetail(asset.id) }
            )
        }
    }
}

@Composable
fun IndexSparklineCard(
    asset: MarketAsset,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPositive = asset.change >= 0

    Box(
        modifier = modifier
            .width(150.dp)
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkBorderSubtle, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("index_card_${asset.id}")
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column {
                Text(
                    text = asset.bengaliName,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = asset.symbol,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            // Glowing Sparkline with gradient
            SparklineChart(
                points = asset.sparkline,
                isPositive = isPositive,
                strokeWidth = 3f,
                showGradientFill = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
            )

            // Price & Change
            Column {
                Text(
                    text = "${asset.currency}${String.format("%,.2f", asset.currentPrice)}",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                val isPos = asset.change >= 0
                Text(
                    text = "${if (isPos) "+" else ""}${asset.change} (${if (isPos) "+" else ""}${asset.changePercent}%)",
                    color = if (isPos) NeonGreen else CrimsonRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun MostActiveAssetCard(
    asset: MarketAsset,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPositive = asset.change >= 0

    // Flash on real-time price tick
    val targetBorder = when (asset.tickDirection) {
        1 -> NeonGreenLight
        -1 -> CrimsonRed
        else -> DarkBorderSubtle
    }
    val animatedBorder by animateColorAsState(
        targetValue = targetBorder,
        animationSpec = tween(500),
        label = "activeCardBorder"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, animatedBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("asset_item_${asset.id}")
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon & Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1.3f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = asset.symbol.take(2).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = asset.bengaliName,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = asset.symbol,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Neon Sparkline
            SparklineChart(
                points = asset.sparkline,
                isPositive = isPositive,
                modifier = Modifier
                    .width(60.dp)
                    .height(30.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Percentage badge & Price
            Column(horizontalAlignment = Alignment.End) {
                // Pill badge for percentage change
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isPositive) NeonGreenBg else CrimsonRedBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${if (isPositive) "+" else ""}${asset.changePercent}%",
                        color = if (isPositive) NeonGreenLight else CrimsonRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${asset.currency}${asset.currentPrice}",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0B0E14)
@Composable
fun MarketScreenPreview() {
    MyApplicationTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(16.dp)
        ) {
            Text("মার্কেট ও ট্রেন্ডস এক্সপ্লোরার", color = TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}
