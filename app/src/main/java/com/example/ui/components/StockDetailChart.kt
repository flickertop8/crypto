package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MarketAsset
import com.example.ui.theme.*
import kotlin.math.roundToInt

enum class ChartType {
    LINE,
    CANDLESTICK
}

@Composable
fun StockDetailChart(
    asset: MarketAsset,
    modifier: Modifier = Modifier
) {
    var selectedTimeframe by remember { mutableStateOf("1D") }
    var chartType by remember { mutableStateOf(ChartType.LINE) }
    val timeframes = listOf("1D", "5D", "1M", "6M", "YTD", "1Y", "5Y", "MAX")

    // Dynamic points based on timeframe
    val basePoints = remember(asset.id, selectedTimeframe) {
        generateChartPoints(asset.currentPrice, selectedTimeframe)
    }

    // Touch interactive scrubber state
    var selectedIndex by remember(basePoints) {
        mutableStateOf((basePoints.size - 1).coerceAtLeast(0))
    }

    val activePrice = basePoints.getOrElse(selectedIndex) { asset.currentPrice.toFloat() }
    val basePrice = basePoints.firstOrNull() ?: asset.currentPrice.toFloat()
    val changeAtScrub = activePrice - basePrice
    val changePercentAtScrub = if (basePrice > 0f) (changeAtScrub / basePrice) * 100f else 0f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Chart Mode Switcher Row (Line, Candlestick, Fullscreen)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { chartType = ChartType.LINE },
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (chartType == ChartType.LINE) DarkSurfaceVariant else Color.Transparent)
            ) {
                Icon(
                    imageVector = Icons.Default.ShowChart,
                    contentDescription = "Line Chart",
                    tint = if (chartType == ChartType.LINE) NeonGreenLight else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = { chartType = ChartType.CANDLESTICK },
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (chartType == ChartType.CANDLESTICK) DarkSurfaceVariant else Color.Transparent)
            ) {
                Icon(
                    imageVector = Icons.Default.CandlestickChart,
                    contentDescription = "Candle Chart",
                    tint = if (chartType == ChartType.CANDLESTICK) NeonGreenLight else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Floating Interactive Tooltip matching Screen 3: "₹176.23 \n +0.443 (+32%)"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceVariant.copy(alpha = 0.95f))
                    .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${asset.currency}${String.format("%.2f", activePrice)}",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    val isPos = changeAtScrub >= 0
                    Text(
                        text = "${if (isPos) "+" else ""}${String.format("%.3f", changeAtScrub)} (${if (isPos) "+" else ""}${String.format("%.1f", changePercentAtScrub)}%)",
                        color = if (isPos) NeonGreen else CrimsonRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main Chart Canvas with Right Y-Axis Price Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .padding(start = 16.dp, end = 12.dp)
        ) {
            // Interactive Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .pointerInput(basePoints) {
                        detectTapGestures { offset ->
                            val stepX = size.width / (basePoints.size - 1).coerceAtLeast(1)
                            val index = (offset.x / stepX).roundToInt().coerceIn(0, basePoints.size - 1)
                            selectedIndex = index
                        }
                    }
                    .pointerInput(basePoints) {
                        detectDragGestures { change, _ ->
                            val stepX = size.width / (basePoints.size - 1).coerceAtLeast(1)
                            val index = (change.position.x / stepX).roundToInt().coerceIn(0, basePoints.size - 1)
                            selectedIndex = index
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val minVal = basePoints.minOrNull() ?: 0f
                    val maxVal = basePoints.maxOrNull() ?: 1f
                    val range = if (maxVal - minVal > 0f) maxVal - minVal else 1f
                    val stepX = w / (basePoints.size - 1).coerceAtLeast(1)

                    if (chartType == ChartType.LINE) {
                        val path = Path()
                        val fillPath = Path()

                        basePoints.forEachIndexed { i, value ->
                            val x = i * stepX
                            val normalizedY = 1f - ((value - minVal) / range)
                            val y = normalizedY * (h - 24f) + 12f

                            if (i == 0) {
                                path.moveTo(x, y)
                                fillPath.moveTo(x, h)
                                fillPath.lineTo(x, y)
                            } else {
                                val prevX = (i - 1) * stepX
                                val prevNormalizedY = 1f - ((basePoints[i - 1] - minVal) / range)
                                val prevY = prevNormalizedY * (h - 24f) + 12f
                                val midX = (prevX + x) / 2f
                                path.cubicTo(midX, prevY, midX, y, x, y)
                                fillPath.cubicTo(midX, prevY, midX, y, x, y)
                            }
                        }

                        fillPath.lineTo(w, h)
                        fillPath.close()

                        // Neon gradient fill under chart curve
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    NeonGreenLight.copy(alpha = 0.35f),
                                    NeonCyan.copy(alpha = 0.15f),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = h
                            )
                        )

                        // Glowing curve stroke
                        drawPath(
                            path = path,
                            color = NeonGreenLight,
                            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        // Draw Scrubber Guide Line & Point
                        val scrubX = selectedIndex * stepX
                        val scrubNormY = 1f - ((activePrice - minVal) / range)
                        val scrubY = scrubNormY * (h - 24f) + 12f

                        // Vertical dashed indicator
                        drawLine(
                            color = Color.White.copy(alpha = 0.4f),
                            start = Offset(scrubX, 0f),
                            end = Offset(scrubX, h),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )

                        // Outer glowing pulse
                        drawCircle(
                            color = NeonGreenLight.copy(alpha = 0.35f),
                            radius = 12.dp.toPx(),
                            center = Offset(scrubX, scrubY)
                        )
                        // Inner solid dot
                        drawCircle(
                            color = Color.White,
                            radius = 5.dp.toPx(),
                            center = Offset(scrubX, scrubY)
                        )
                    } else {
                        // Candlestick rendering
                        basePoints.forEachIndexed { i, price ->
                            val x = i * stepX
                            val open = price * (1f + (if (i % 2 == 0) -0.015f else 0.012f))
                            val close = price
                            val high = maxOf(open, close) * 1.01f
                            val low = minOf(open, close) * 0.99f

                            val isGreen = close >= open
                            val candleColor = if (isGreen) NeonGreen else CrimsonRed

                            val normHigh = 1f - ((high - minVal) / range)
                            val normLow = 1f - ((low - minVal) / range)
                            val normOpen = 1f - ((open - minVal) / range)
                            val normClose = 1f - ((close - minVal) / range)

                            val yHigh = normHigh * (h - 24f) + 12f
                            val yLow = normLow * (h - 24f) + 12f
                            val yOpen = normOpen * (h - 24f) + 12f
                            val yClose = normClose * (h - 24f) + 12f

                            // Wick
                            drawLine(
                                color = candleColor,
                                start = Offset(x, yHigh),
                                end = Offset(x, yLow),
                                strokeWidth = 1.5.dp.toPx()
                            )

                            // Body
                            val topY = minOf(yOpen, yClose)
                            val bottomY = maxOf(yOpen, yClose)
                            drawRect(
                                color = candleColor,
                                topLeft = Offset(x - 4.dp.toPx(), topY),
                                size = androidx.compose.ui.geometry.Size(8.dp.toPx(), (bottomY - topY).coerceAtLeast(4f))
                            )
                        }
                    }
                }
            }

            // Right Y-Axis Price Labels matching Screen 3
            Column(
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                val maxVal = basePoints.maxOrNull() ?: asset.currentPrice.toFloat()
                val minVal = basePoints.minOrNull() ?: (asset.currentPrice * 0.9f).toFloat()
                val steps = 5
                for (i in 0..steps) {
                    val p = maxVal - ((maxVal - minVal) / steps) * i
                    Text(
                        text = "${asset.currency}${String.format("%.1f", p)}",
                        color = TextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bengali Timeline Labels matching Screen 3: ১ দিন, ৫ দিন, ১ মাস, ৬ মাস, ১ বছর, ৫ বছর, MAX
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val bnTimeline = listOf("১ দিন", "৫ দিন", "১ মাস", "৬ মাস", "১ বছর", "৫ বছর", "MAX")
            bnTimeline.forEach { label ->
                Text(
                    text = label,
                    color = TextTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Timeframe Selector Buttons (1D, 5D, 1M, 6M, YTD, 1Y, 5Y, MAX) + Fullscreen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            timeframes.forEach { tf ->
                val isSelected = selectedTimeframe == tf
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) DarkSurfaceVariant else Color.Transparent)
                        .border(
                            1.dp,
                            if (isSelected) ElectricBlue else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedTimeframe = tf }
                        .padding(horizontal = 7.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tf,
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Fullscreen,
                contentDescription = "Fullscreen",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// Generate realistic points for varying timeframes
private fun generateChartPoints(currentPrice: Double, timeframe: String): List<Float> {
    val count = when (timeframe) {
        "1D" -> 16
        "5D" -> 20
        "1M" -> 24
        "6M" -> 28
        "YTD" -> 30
        "1Y" -> 32
        "5Y" -> 36
        else -> 40
    }

    val points = mutableListOf<Float>()
    var p = (currentPrice * 0.75).toFloat()
    for (i in 0 until count - 1) {
        val trend = (currentPrice.toFloat() - p) / (count - i)
        val noise = (kotlin.random.Random.nextFloat() - 0.45f) * (currentPrice.toFloat() * 0.04f)
        p += trend + noise
        points.add(p)
    }
    points.add(currentPrice.toFloat())
    return points
}
