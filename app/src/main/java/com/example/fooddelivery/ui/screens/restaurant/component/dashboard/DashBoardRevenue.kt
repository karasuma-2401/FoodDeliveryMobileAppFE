package com.example.fooddelivery.ui.screens.restaurant.component.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fooddelivery.ui.screens.restaurant.dashboard.RecentOrder
import com.example.fooddelivery.domain.util.CurrencyFormatter
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun RevenueSection(
    revenue: Double,
    recentOrders: List<RecentOrder>,
    onSeeDetailsClick: () -> Unit = {}
) {
    val chartData = remember(recentOrders) {
        recentOrders.reversed().map { Pair(it.time, it.totalPrice) }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Revenue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "See Details",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable { onSeeDetailsClick() }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            val formattedRevenue = CurrencyFormatter.format(revenue)
            Text(
                text = formattedRevenue,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(vertical = 12.dp, horizontal = 12.dp)
            ) {
                if (chartData.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No revenue history yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    DashboardRealLineChart(
                        dataPoints = chartData,
                        lineColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardRealLineChart(
    dataPoints: List<Pair<String, Double>>,
    lineColor: Color
) {
    val maxPrice = remember(dataPoints) {
        val max = dataPoints.maxOfOrNull { it.second } ?: 1.0
        if (max == 0.0) 1.0 else max
    }

    val textMeasurer = rememberTextMeasurer()
    val onSurfaceColor = MaterialTheme.colorScheme.onSurfaceVariant
    val labelStyle = TextStyle(color = onSurfaceColor, fontSize = 10.sp)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val paddingLeft = 45.dp.toPx()
        val paddingBottom = 24.dp.toPx()
        val paddingTop = 16.dp.toPx()

        val chartWidth = size.width - paddingLeft
        val chartHeight = size.height - paddingBottom - paddingTop

        val stepX = if (dataPoints.size > 1) chartWidth / (dataPoints.size - 1) else chartWidth

        val gridColor = Color.Gray.copy(alpha = 0.3f)
        val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

        drawLine(
            color = onSurfaceColor,
            start = Offset(paddingLeft, paddingTop),
            end = Offset(paddingLeft, size.height - paddingBottom),
            strokeWidth = 2f
        )
        drawLine(
            color = onSurfaceColor,
            start = Offset(paddingLeft, size.height - paddingBottom),
            end = Offset(size.width, size.height - paddingBottom),
            strokeWidth = 2f
        )

        val horizontalLines = 4
        for (i in 0..horizontalLines) {
            val ratio = i.toFloat() / horizontalLines
            val y = paddingTop + chartHeight * ratio
            val value = maxPrice * (1f - ratio)

            drawLine(
                color = gridColor,
                start = Offset(paddingLeft, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = dashPathEffect
            )

            val textLayoutResult = textMeasurer.measure(
                text = "$${String.format(Locale.US, "%.1f", value)}",
                style = labelStyle
            )
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    x = paddingLeft - textLayoutResult.size.width - 8.dp.toPx(),
                    y = y - textLayoutResult.size.height / 2f
                )
            )
        }


        val xLabelStep = if (dataPoints.size > 4) dataPoints.size / 3 else 1

        for (i in dataPoints.indices) {
            val x = paddingLeft + (i * stepX)

            drawLine(
                color = gridColor,
                start = Offset(x, paddingTop),
                end = Offset(x, size.height - paddingBottom),
                strokeWidth = 1.dp.toPx(),
                pathEffect = dashPathEffect
            )

            if (i % xLabelStep == 0 || i == dataPoints.lastIndex) {
                val textLayoutResult = textMeasurer.measure(
                    text = dataPoints[i].first,
                    style = labelStyle
                )
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        x = x - textLayoutResult.size.width / 2f,
                        y = size.height - paddingBottom + 6.dp.toPx()
                    )
                )
            }
        }

        val strokePath = Path()
        val fillPath = Path()

        dataPoints.forEachIndexed { index, point ->
            val x = paddingLeft + (index * stepX)
            val ratioY = (point.second / maxPrice).toFloat()
            val y = paddingTop + chartHeight - (ratioY * chartHeight)

            if (index == 0) {
                strokePath.moveTo(x, y)
                fillPath.moveTo(x, size.height - paddingBottom)
                fillPath.lineTo(x, y)
            } else {
                strokePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            if (index == dataPoints.lastIndex) {
                fillPath.lineTo(x, size.height - paddingBottom)
                fillPath.close()
            }

            drawCircle(
                color = lineColor,
                radius = 3.5.dp.toPx(),
                center = Offset(x, y)
            )
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.35f), Color.Transparent),
                startY = paddingTop,
                endY = size.height - paddingBottom
            )
        )

        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(width = 2.5.dp.toPx())
        )
    }
}