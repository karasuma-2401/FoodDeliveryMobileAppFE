package com.example.fooddelivery.ui.screens.food.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.fooddelivery.R

val FoodHeroContentHeight = 280.dp
val FoodHeroPanelOverlap = 20.dp

@Composable
fun foodHeroTotalHeight(): Dp {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    return statusBarTop + FoodHeroContentHeight
}

@Composable
fun FoodDetailHeroImage(
    imageUrl: String?,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = imageUrl ?: R.drawable.food_bowl,
        contentDescription = null,
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
        placeholder = painterResource(id = R.drawable.food_bowl),
        error = painterResource(id = R.drawable.food_bowl)
    )
}

@Composable
fun FoodDetailHeroActions(
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        HeaderIconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        HeaderIconButton(onClick = onShareClick) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = "Share",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun HeaderIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
    ) {
        content()
    }
}
