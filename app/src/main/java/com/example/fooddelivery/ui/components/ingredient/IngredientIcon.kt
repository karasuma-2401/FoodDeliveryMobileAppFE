package com.example.fooddelivery.ui.components.ingredient

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun IngredientIcon(
    iconKey: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    Icon(
        imageVector = IngredientIconMapper.resolve(iconKey),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}
