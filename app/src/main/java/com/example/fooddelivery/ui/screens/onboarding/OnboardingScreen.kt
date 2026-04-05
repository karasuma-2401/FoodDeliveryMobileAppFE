package com.example.fooddelivery.ui.screens.onboarding

import com.example.fooddelivery.R
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fooddelivery.ui.components.button.DFoodButton
import kotlinx.coroutines.launch

data class  OnboardingPage (
    @DrawableRes val imageRes: Int,
    val title: String,
    val description: String,
)

val onboardingPages = listOf(
    OnboardingPage(
        imageRes = R.drawable.ic_onboarding_1,
        title = "All your favorites",
        description = "Get all your loved foods in once place,\n you just place the order we do the rest"
    ),
    OnboardingPage(
        imageRes = R.drawable.ic_onboarding_2,
        title = "Order from choosen chef",
        description = "Get all your loved foods in once place,\n you just place the order we do the rest"
    ),
    OnboardingPage(
        imageRes = R.drawable.ic_onboarding_3,
        title = "Free delivery offers",
        description = "Get all your loved foods in once place,\n you just place the order we do the rest"
    )
)

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { position ->
            PagerScreen(page = onboardingPages[position])
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(onboardingPages.size) { iteration ->
                val color = if (pagerState.currentPage == iteration)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.primaryContainer

                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(if (pagerState.currentPage == iteration) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (pagerState.currentPage == onboardingPages.lastIndex) {
                DFoodButton(
                    text = "GET STARTED",
                    onClick = {
                        viewModel.saveOnboardingState(completed = true)
                        onFinishOnboarding()
                    }
                )
            } else {
                DFoodButton(
                    text = "NEXT",
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onFinishOnboarding) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PagerScreen(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = page.title,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.6f)
        )

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 24.dp)
        )

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp)
        )
    }
}