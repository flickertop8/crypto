package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen

@Composable
fun SparklineChart(
    points: List<Float>,
    modifier: Modifier = Modifier,
    isPositive: Boolean = true,
    lineColor: Color? = null,
    strokeWidth: Float = 4f,
    showGradientFill: Boolean = false
) {
    if (points.isEmpty()) return

    val strokeColor = lineColor ?: if (isPositive) NeonGreen else CrimsonRed

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val minVal = points.minOrNull() ?: 0f
        val maxVal = points.maxOrNull() ?: 1f
        val range = if (maxVal - minVal > 0f) maxVal - minVal else 1f

        val stepX = width / (points.size - 1).coerceAtLeast(1)

        val path = Path()
        val fillPath = Path()

        points.forEachIndexed { index, value ->
            val x = index * stepX
            val normalizedY = 1f - ((value - minVal) / range)
            // Keep small margin top and bottom so stroke isn't clipped
            val y = normalizedY * (height - 8f) + 4f

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                // Smooth bezier curve
                val prevX = (index - 1) * stepX
                val prevNormalizedY = 1f - ((points[index - 1] - minVal) / range)
                val prevY = prevNormalizedY * (height - 8f) + 4f
                val midX = (prevX + x) / 2f
                path.cubicTo(midX, prevY, midX, y, x, y)
                fillPath.cubicTo(midX, prevY, midX, y, x, y)
            }
        }

        if (showGradientFill) {
            fillPath.lineTo(width, height)
            fillPath.close()
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        strokeColor.copy(alpha = 0.35f),
                        strokeColor.copy(alpha = 0.0f)
                    ),
                    startY = 0f,
                    endY = height
                )
            )
        }

        drawPath(
            path = path,
            color = strokeColor,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
