package com.example.fooddelivery.ui.screens.order

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.domain.model.Order
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.order.components.OrderItemCard
import com.example.fooddelivery.ui.theme.DFoodTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun OrdersScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTrackOrder: (String) -> Unit,
    onNavigateToRate: (String) -> Unit,
    viewModel: OrderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })

    LaunchedEffect(pagerState.currentPage) {
        viewModel.onEvent(OrderEvent.SelectTab(pagerState.currentPage))
    }

    OrderContent(
        state = state,
        pagerState = pagerState,
        scope = scope,
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
    onBackClick: () -> Unit,
    onTrackOrder: (String) -> Unit,
    onRate: (String) -> Unit,
    onSelectTab: (Int) -> Unit,
    onCancelOrder: (String) -> Unit,
    onReOrder: (String) -> Unit
) {
    Scaffold(
        topBar = {
            DFoodTopBar(
                title = "My Orders",
                onBackClick = onBackClick
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
                        HorizontalDivider(color = Color(0xFFF0F0F0))
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
                                        color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else Color.LightGray
                                    )
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
                    val orders = if (page == 0) state.ongoingOrders else state.historyOrders
                    
                    OrderList(
                        orders = orders,
                        onPrimaryAction = { id ->
                            if (page == 0) onTrackOrder(id) else onReOrder(id)
                        },
                        onSecondaryAction = { id ->
                            if (page == 0) onCancelOrder(id) else onRate(id)
                        }
                    )
                }
            }

            if (state.isLoading) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.3f)
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
    onSecondaryAction: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(orders) { order ->
            OrderItemCard(
                order = order,
                onPrimaryAction = { onPrimaryAction(order.id) },
                onSecondaryAction = { onSecondaryAction(order.id) }
            )
        }
    }
}
@Preview(showSystemUi = true, showBackground = true)
@Composable
fun OrdersScreenPreview() {
    DFoodTheme(darkTheme = false) {
        OrderContent (
            state = OrderState(),
            pagerState = rememberPagerState(pageCount = { 2 }),
            scope = rememberCoroutineScope(),
            onBackClick = {},
            onTrackOrder = {},
            onRate = {},
            onSelectTab = {},
            onCancelOrder = {},
            onReOrder = {}
        )
    }
}