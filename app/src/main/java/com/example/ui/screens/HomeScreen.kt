package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarketViewModel

@Composable
fun HomeScreen(
    viewModel: MarketViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToPortfolio: () -> Unit,
    onNavigateToMarket: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenWallet: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.portfolioSummary.collectAsState()
    val assets by viewModel.assets.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    HomeScreenContent(
        summary = summary,
        assets = assets,
        searchQuery = searchQuery,
        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToPortfolio = onNavigateToPortfolio,
        onNavigateToMarket = onNavigateToMarket,
        onOpenProfile = onOpenProfile,
        onOpenWallet = onOpenWallet,
        modifier = modifier
    )
}

@Composable
fun HomeScreenContent(
    summary: com.example.ui.viewmodel.PortfolioSummary,
    assets: List<MarketAsset>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToPortfolio: () -> Unit,
    onNavigateToMarket: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenWallet: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Top Bar: Profile avatar, "11 Dec 2021 \n 4:40 PM", Wallet button
        item {
            MarketTopBar(
                onAvatarClick = onOpenProfile,
                onWalletClick = onOpenWallet,
                formattedDateTime = "11 Dec 2021\n4:40 PM"
            )
        }

        // Search Bar matching Screen 1: "Search stocks, funds, commodities, etc."
        item {
            MarketSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = "Search stocks, funds, commodities, etc."
            )
        }

        // Portfolio Overview Glowing Blue Card matching Screen 1
        item {
            PortfolioOverviewCard(
                summary = summary,
                onClick = onNavigateToPortfolio
            )
        }

        // Today's Market 2x2 Grid matching Screen 1 ("আজকের মার্কেট")
        item {
            MarketIndicesGrid(
                assets = assets,
                onAssetClick = { asset ->
                    onNavigateToDetail(asset.id)
                }
            )
        }

        // Stock Picks / Watchlist matching Screen 1: "স্টক ধারণা" (View all)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "স্টক ধারণা",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant)
                        .clickable { onNavigateToMarket() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "View all",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Watchlist item cards (HDFC, Reliance, Tata, Bitcoin, etc.)
        val watchlistAssets = assets.filter { it.id in listOf("hdfc", "reliance", "tata", "btc", "aapl") }
        items(watchlistAssets, key = { it.id }) { asset ->
            HomeWatchlistItem(
                asset = asset,
                onClick = { onNavigateToDetail(asset.id) },
                onBuyClick = { onNavigateToDetail(asset.id) }
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0B0E14)
@Composable
fun HomeScreenPreview() {
    MyApplicationTheme {
        HomeScreenContent(
            summary = com.example.ui.viewmodel.PortfolioSummary(),
            assets = emptyList(),
            searchQuery = "",
            onSearchQueryChange = {},
            onNavigateToDetail = {},
            onNavigateToPortfolio = {},
            onNavigateToMarket = {},
            onOpenProfile = {},
            onOpenWallet = {}
        )
    }
}

@Composable
fun HomeWatchlistItem(
    asset: MarketAsset,
    onClick: () -> Unit,
    onBuyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPositive = asset.change >= 0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkBorderSubtle, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("watchlist_item_${asset.id}")
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1.2f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = asset.symbol.take(2),
                        color = ElectricBlue,
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

            // Mini Sparkline
            SparklineChart(
                points = asset.sparkline,
                isPositive = isPositive,
                modifier = Modifier
                    .width(55.dp)
                    .height(28.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Price & Buy Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${asset.currency}${asset.currentPrice}",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${if (isPositive) "+" else ""}${asset.changePercent}%",
                        color = if (isPositive) NeonGreen else CrimsonRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // BUY button like in screenshot
                Button(
                    onClick = onBuyClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = "BUY",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
