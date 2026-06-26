package com.example.fooddelivery.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.domain.util.toTicketLabels

val TicketVoucherBackground = Color(0xFFE8F5F3)
val TicketVoucherTeal = Color(0xFF26A69A)

private object TicketVoucherShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val notchDepth = with(density) { 3.dp.toPx() }
        val toothStep = with(density) { 5.dp.toPx() }
        val cornerRadius = with(density) { 4.dp.toPx() }

        val path = Path().apply {
            moveTo(notchDepth, 0f)
            lineTo(size.width - cornerRadius, 0f)
            arcTo(
                rect = Rect(
                    left = size.width - cornerRadius * 2,
                    top = 0f,
                    right = size.width,
                    bottom = cornerRadius * 2
                ),
                startAngleDegrees = -90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(size.width, size.height - cornerRadius)
            arcTo(
                rect = Rect(
                    left = size.width - cornerRadius * 2,
                    top = size.height - cornerRadius * 2,
                    right = size.width,
                    bottom = size.height
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            lineTo(notchDepth, size.height)

            var y = size.height
            var pointOut = false
            while (y > 0f) {
                val nextY = (y - toothStep).coerceAtLeast(0f)
                val midY = (y + nextY) / 2f
                lineTo(if (pointOut) 0f else notchDepth, midY)
                lineTo(if (pointOut) notchDepth else 0f, nextY)
                y = nextY
                pointOut = !pointOut
            }
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun TicketVoucherTag(
    voucher: Voucher,
    modifier: Modifier = Modifier
) {
    val (discountLine, minOrderLine) = voucher.toTicketLabels()

    Row(
        modifier = modifier
            .height(34.dp)
            .width(200.dp)
            .clip(TicketVoucherShape)
            .border(width = 1.dp, color = TicketVoucherTeal, shape = TicketVoucherShape)
            .background(TicketVoucherBackground, TicketVoucherShape),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = discountLine,
                color = TicketVoucherTeal,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }

        Canvas(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .padding(vertical = 5.dp)
        ) {
            drawLine(
                color = TicketVoucherTeal,
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(0f, size.height),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()))
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = minOrderLine,
                color = TicketVoucherTeal,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
