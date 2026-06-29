package com.example.fooddelivery.ui.screens.customer.order

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.domain.model.Order
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.customer.order.components.OrderItemCard
import com.example.fooddelivery.ui.screens.customer.order.components.OrderItemCardSkeleton
import com.example.fooddelivery.ui.theme.CustomerDimens
import com.example.fooddelivery.ui.theme.DFoodTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun OrdersScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTrackOrder: (String) -> Unit,
    onNavigateToRate: (String, String, String, String) -> Unit,
    onNavigateToCart: () -> Unit,
    showBackButton: Boolean = true,
    viewModel: OrderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is OrderUiEffect.NavigateToCart -> onNavigateToCart()
                is OrderUiEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is OrderUiEffect.ShowSuccess -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.onEvent(OrderEvent.SelectTab(pagerState.currentPage))
    }

    OrderContent(
        state = state,
        pagerState = pagerState,
        scope = scope,
        showBackButton = showBackButton,
        onBackClick = onNavigateBack,
        onTrackOrder = onNavigateToTrackOrder,
        onRate = onNavigateToRate,
        onSelectTab = { index ->
            scope.launch {
                pagerState.animateScrollToPage(index)
            }
        },
        onCancelOrder = { id -> viewModel.onEvent(OrderEvent.CancelOrder(id)) },
        onReOrder = { id -> viewModel.onEvent(OrderEvent.ReOrder(id)) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderContent(
    state: OrderState,
    pagerState: PagerState,
    scope: CoroutineScope,
    showBackButton: Boolean = true,
    onBackClick: () -> Unit,
    onTrackOrder: (String) -> Unit,
    onRate: (String, String, String, String) -> Unit,
    onSelectTab: (Int) -> Unit,
    onCancelOrder: (String) -> Unit,
    onReOrder: (String) -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            DFoodTopBar(
                title = "My Orders",
                onBackClick = if (showBackButton) onBackClick else null,
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {

                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        if (pagerState.currentPage < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    divider = {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                ) {
                    val tabs = listOf("Ongoing", "History")
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = { onSelectTab(index) },
                            text = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal,
                                    ),
                                    color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 1
                ) { page ->

                    if (state.isInitLoading) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = CustomerDimens.screenHorizontalPadding),
                            contentPadding = PaddingValues(vertical = 16.dp),
                            userScrollEnabled = false
                        ) {
                            items(3) {
                                OrderItemCardSkeleton()
                            }
                        }
                    } else {
                        val orders = if (page == 0) state.ongoingOrders else state.historyOrders
                        OrderList(
                            orders = orders,
                            onPrimaryAction = { id ->
                                if (page == 0) onTrackOrder(id) else onReOrder(id)
                            },
                            onSecondaryAction = { id, restaurantId, restaurantName, restaurantImage ->
                                if (page == 0) onCancelOrder(id) else onRate(id, restaurantId, restaurantName, restaurantImage)
                            }
                        )
                    }
                }
            }

            if (state.isLoading) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderList(
    orders: List<Order>,
    onPrimaryAction: (String) -> Unit,
    onSecondaryAction: (String, String, String, String) -> Unit
) {
    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No orders found",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = CustomerDimens.screenHorizontalPadding),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(orders) { order ->
                OrderItemCard(
                    order = order,
                    onPrimaryAction = { onPrimaryAction(order.id) },
                    onSecondaryAction = { onSecondaryAction(order.id, order.restaurantId, order.restaurantName, order.restaurantImage) }
                )
            }
        }
    }
}
@Preview(showSystemUi = true, showBackground = true)
@Composable
fun OrdersScreenPreview() {
    DFoodTheme(darkTheme = false) {
        OrderContent(
            state = OrderState(),
            pagerState = rememberPagerState(pageCount = { 2 }),
            scope = rememberCoroutineScope(),
            onBackClick = {},
            onTrackOrder = {},
            onRate = { _, _, _, _ -> },
            onSelectTab = {},
            onCancelOrder = {},
            onReOrder = {}
        )
    }
}
