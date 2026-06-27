package com.example.fooddelivery.ui.components.cart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset

data class FlyToCartRequest(
    val imageUrl: Any?,
    val startCenter: Offset,
)

@Stable
class FlyToCartState {
    var pendingRequest by mutableStateOf<FlyToCartRequest?>(null)
        private set

    var cartTarget by mutableStateOf<Offset?>(null)
        private set

    var cartBounceTrigger by mutableIntStateOf(0)
        private set

    fun updateCartTarget(center: Offset) {
        cartTarget = center
    }

    fun launch(imageUrl: Any?, startCenter: Offset) {
        pendingRequest = FlyToCartRequest(
            imageUrl = imageUrl,
            startCenter = startCenter,
        )
    }

    fun clearRequest() {
        pendingRequest = null
    }

    fun onAnimationFinished() {
        cartBounceTrigger++
        clearRequest()
    }
}

@Composable
fun rememberFlyToCartState(): FlyToCartState = remember { FlyToCartState() }
