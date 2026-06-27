package com.example.fooddelivery.ui.screens.customer.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fooddelivery.R
import com.example.fooddelivery.ui.components.bounceClick
import com.example.fooddelivery.ui.components.shimmerEffect
import com.example.fooddelivery.ui.theme.CustomerDimens
import com.example.fooddelivery.ui.theme.DFoodTheme
import java.util.Calendar

private enum class TimeOfDay(
    val greeting: String,
    val subtitle: String,
    val gradientStart: Color,
    val gradientEnd: Color,
) {
    MORNING(
        greeting = "Good morning ☀️",
        subtitle = "A delicious breakfast is ready for you",
        gradientStart = Color(0xFFFFE8D6),
        gradientEnd = Color(0xFFFFF0E6),
    ),
    LUNCH(
        greeting = "Good Lunch \uD83C\uDF74",
        subtitle = "Hungry yet? Great food, fast delivery",
        gradientStart = Color(0xFFFFDBC9),
        gradientEnd = Color(0xFFFFF4EC),
    ),
    AFTERNOON(
        greeting = "Good afternoon \uD83C\uDF07",
        subtitle = "Beat the afternoon cravings",
        gradientStart = Color(0xFFFFD4BC),
        gradientEnd = Color(0xFFFFEDE3),
    ),
    EVENING(
        greeting = "Good evening",
        subtitle = "What would you like for dinner tonight?",
        gradientStart = Color(0xFFFFCDB2),
        gradientEnd = Color(0xFFFFE8DA),
    ),
    LATE_NIGHT(
        greeting = "Good evening 🌙",
        subtitle = "It's late, but we're still open",
        gradientStart = Color(0xFFEDD9D0),
        gradientEnd = Color(0xFFF5EBE6),
    ),
}

private fun resolveTimeOfDay(hour: Int): TimeOfDay = when (hour) {
    in 5..10 -> TimeOfDay.MORNING
    in 11..13 -> TimeOfDay.LUNCH
    in 14..16 -> TimeOfDay.AFTERNOON
    in 17..20 -> TimeOfDay.EVENING
    else -> TimeOfDay.LATE_NIGHT
}

@Composable
private fun rememberTimeOfDay(): TimeOfDay {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return remember(hour) { resolveTimeOfDay(hour) }
}

private fun displayName(fullName: String): String =
    fullName.trim().ifEmpty { "Guest" }

@Composable
private fun GreetingLogo() {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.85f))
            .border(
                width = 2.dp,
                color = Color.White.copy(alpha = 0.75f),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_dfood_logo),
            contentDescription = "DFood logo",
            modifier = Modifier
                .size(48.dp)
                .padding(0.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
fun HomeGreetingCard(
    userName: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val timeOfDay = rememberTimeOfDay()
    val name = displayName(userName)
    val cardShape = RoundedCornerShape(CustomerDimens.cardCornerRadius)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(cardShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(timeOfDay.gradientStart, timeOfDay.gradientEnd),
                ),
            )
            .then(
                if (onClick != null) {
                    Modifier.bounceClick(onClick = onClick)
                } else {
                    Modifier
                },
            ),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 32.dp, y = (-8).dp)
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.22f)),
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GreetingLogo()

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = timeOfDay.greeting,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.sp,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = timeOfDay.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
fun HomeGreetingSkeleton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(CustomerDimens.cardCornerRadius))
            .shimmerEffect(),
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeGreetingCardPreview() {
    DFoodTheme(darkTheme = false) {
        HomeGreetingCard(
            userName = "Minh",
            modifier = Modifier.padding(16.dp),
        )
    }
}
