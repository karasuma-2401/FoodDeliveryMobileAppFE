package com.example.fooddelivery.ui.screens.order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.domain.model.Order
import com.example.fooddelivery.domain.model.OrderType
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.order.components.OrderItemCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTrackOrder: (String) -> Unit,
    onNavigateToRate: (String) -> Unit,
//    viewModel: OrderViewModel = hiltViewModel()
) {
//    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })
    LaunchedEffect(pagerState.currentPage) {
//        viewModel.selectTab(pagerState.currentPage)
    }

    Scaffold(
        topBar = {
            DFoodTopBar(
                title = "My Orders",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { /* Actions */ }) {
                        Icon(Icons.Default.MoreHoriz, "more")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = Color.Transparent,
                divider = { HorizontalDivider(color = Color(0xFFF0F0F0)) },
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
//                        modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                listOf("Ongoing", "History").forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
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
//                val orders = if (page == 0) uiState.ongoingOrders else uiState.historyOrders
                
                OrderList(
                    //orders = orders,
                    orders = listOf(), // write temporary to test
                    onPrimaryAction = { id -> 
                        if (page == 0) onNavigateToTrackOrder(id) else { /* Re-order logic */ }
                    },
                    onSecondaryAction = { id ->
                        if (page == 0) { /* Cancel logic */ } else onNavigateToRate(id)
                    }
                )
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
        contentPadding = PaddingValues(vertical = 20.dp)
    ) {
        val groupedOrders = orders.groupBy { it.type }
        
        groupedOrders.forEach { (type, orderList) ->
            item {
                Text(
                    text = if (type == OrderType.FOOD) "Food" else "Drink",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = Color(0xFF32343E),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
            
            items(orderList) { order ->
                OrderItemCard(
                    order = order,
                    onPrimaryAction = { onPrimaryAction(order.id) },
                    onSecondaryAction = { onSecondaryAction(order.id) }
                )
            }
        }
    }
}
