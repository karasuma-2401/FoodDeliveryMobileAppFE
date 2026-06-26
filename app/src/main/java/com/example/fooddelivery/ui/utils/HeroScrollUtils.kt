package com.example.fooddelivery.ui.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable
fun rememberHeroOverlayState(
    listState: LazyListState,
    heroHeight: Dp,
    panelOverlap: Dp
): HeroOverlayState {
    val density = LocalDensity.current
    val hideThresholdPx = with(density) { (heroHeight - panelOverlap).toPx() }

    val showHeroImage by remember(listState, hideThresholdPx) {
        derivedStateOf {
            when {
                listState.firstVisibleItemIndex > 0 -> false
                else -> listState.firstVisibleItemScrollOffset < hideThresholdPx
            }
        }
    }

    val showHeroActions by remember(listState) {
        derivedStateOf { listState.firstVisibleItemIndex == 0 }
    }

    return HeroOverlayState(
        showHeroImage = showHeroImage,
        showHeroActions = showHeroActions
    )
}

data class HeroOverlayState(
    val showHeroImage: Boolean,
    val showHeroActions: Boolean
)
