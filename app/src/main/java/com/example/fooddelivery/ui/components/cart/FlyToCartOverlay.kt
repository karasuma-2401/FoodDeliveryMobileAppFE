package com.example.fooddelivery.ui.components.cart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import com.example.fooddelivery.R
import com.example.fooddelivery.ui.theme.CustomerDimens
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

private const val FLY_DURATION_MS = 650
private const val CART_TARGET_WAIT_MS = 200L

@Composable
fun FlyToCartOverlay(
    state: FlyToCartState,
) {
    val request = state.pendingRequest ?: return

    Popup(
        alignment = Alignment.TopStart,
        properties = PopupProperties(
            focusable = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            clippingEnabled = false,
        ),
        onDismissRequest = {},
    ) {
        FlyToCartFlyingContent(state = state, request = request)
    }
}

@Composable
private fun FlyToCartFlyingContent(
    state: FlyToCartState,
    request: FlyToCartRequest,
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val startSizePx = with(density) { CustomerDimens.listThumbnailMd.toPx() }
    val endSizePx = with(density) { 32.dp.toPx() }
    val progress = remember(request) { Animatable(0f) }

    var overlayOrigin by remember { mutableStateOf(Offset.Zero) }
    var endCenter by remember(request) { mutableStateOf<Offset?>(null) }

    LaunchedEffect(request) {
        progress.snapTo(0f)
        endCenter = null

        var waited = 0L
        while (state.cartTarget == null && waited < CART_TARGET_WAIT_MS) {
            delay(16)
            waited += 16
        }

        endCenter = state.cartTarget ?: fallbackCartCenter(
            screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() },
            screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() },
            density = density,
        )

        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = FLY_DURATION_MS,
                easing = FastOutSlowInEasing,
            ),
        )
        state.onAnimationFinished()
    }

    val resolvedEnd = endCenter ?: state.cartTarget ?: fallbackCartCenter(
        screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() },
        screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() },
        density = density,
    )

    val controlPoint = Offset(
        x = (request.startCenter.x + resolvedEnd.x) / 2f,
        y = min(request.startCenter.y, resolvedEnd.y) - with(density) { 120.dp.toPx() },
    )

    val t = progress.value
    val currentCenter = quadraticBezier(
        t = t,
        p0 = request.startCenter,
        p1 = controlPoint,
        p2 = resolvedEnd,
    )
    val currentSizePx = lerp(startSizePx, endSizePx, t)
    val topLeftX = currentCenter.x - currentSizePx / 2f - overlayOrigin.x
    val topLeftY = currentCenter.y - currentSizePx / 2f - overlayOrigin.y
    val sizeDp = with(density) { currentSizePx.toDp() }

    Box(
        modifier = Modifier
            .size(screenWidth, screenHeight)
            .onGloballyPositioned { coordinates ->
                overlayOrigin = coordinates.positionInWindow()
            }
    ) {
        AsyncImage(
            model = request.imageUrl ?: R.drawable.food_bowl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.food_bowl),
            error = painterResource(R.drawable.food_bowl),
            modifier = Modifier
                .offset { IntOffset(topLeftX.roundToInt(), topLeftY.roundToInt()) }
                .size(sizeDp)
                .graphicsLayer {
                    alpha = if (t > 0.85f) 1f - ((t - 0.85f) / 0.15f) * 0.35f else 1f
                }
                .clip(CircleShape)
        )
    }
}

private fun quadraticBezier(t: Float, p0: Offset, p1: Offset, p2: Offset): Offset {
    val inverse = 1f - t
    return Offset(
        x = inverse * inverse * p0.x + 2f * inverse * t * p1.x + t * t * p2.x,
        y = inverse * inverse * p0.y + 2f * inverse * t * p1.y + t * t * p2.y,
    )
}

private fun lerp(start: Float, end: Float, fraction: Float): Float {
    return start + (end - start) * fraction
}

private fun fallbackCartCenter(
    screenWidthPx: Float,
    screenHeightPx: Float,
    density: androidx.compose.ui.unit.Density,
): Offset {
    val horizontalInset = with(density) {
        (CustomerDimens.cardPaddingLg + CustomerDimens.cartBarIconSize / 2).toPx()
    }
    val verticalInset = with(density) {
        (10.dp + CustomerDimens.cartBarIconSize / 2 + CustomerDimens.bottomBarPadding).toPx()
    }
    return Offset(
        x = horizontalInset.coerceAtMost(screenWidthPx),
        y = screenHeightPx - verticalInset,
    )
}

fun Modifier.onCenterPositioned(onCenter: (Offset) -> Unit): Modifier {
    return this.onGloballyPositioned { coordinates ->
        val position = coordinates.positionInWindow()
        onCenter(
            Offset(
                x = position.x + coordinates.size.width / 2f,
                y = position.y + coordinates.size.height / 2f,
            )
        )
    }
}
