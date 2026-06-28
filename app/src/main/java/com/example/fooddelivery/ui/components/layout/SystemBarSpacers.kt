package com.example.fooddelivery.ui.components.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun NavigationBarBottomSpacer(modifier: Modifier = Modifier) {
    Spacer(modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
}

@Composable
fun ScaffoldBottomBarSurface(
    modifier: Modifier = Modifier,
    shadowElevation: Dp = 12.dp,
    tonalElevation: Dp = 2.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = shadowElevation,
        tonalElevation = tonalElevation,
    ) {
        Column(Modifier.fillMaxWidth()) {
            content()
            NavigationBarBottomSpacer()
        }
    }
}
