package com.example.fooddelivery.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddelivery.domain.util.DiscountBadgeVisual

private val BadgeBackground = Color(0xFFFFE97A)
private val BadgeTopText = Color(0xFFEE4D2D)
private val BadgeBottomText = Color.White

private object ShopeeDiscountBadgeShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val notch = size.width * 0.24f
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height - notch)
            lineTo(size.width / 2f, size.height)
            lineTo(0f, size.height - notch)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun ShopeeDiscountBadge(
    visual: DiscountBadgeVisual,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(40.dp)
            .height(46.dp)
            .clip(ShopeeDiscountBadgeShape)
            .background(BadgeBackground)
            .padding(horizontal = 4.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = visual.topLine,
                color = BadgeTopText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = visual.bottomLine,
                color = BadgeBottomText,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}
