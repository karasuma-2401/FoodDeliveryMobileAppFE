package com.example.fooddelivery.ui.screens.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.ui.components.button.DFoodButton
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.cart.components.BillBreakdown
import com.example.fooddelivery.ui.screens.cart.components.CartItemCard
import com.example.fooddelivery.ui.screens.cart.components.VoucherSection
import com.example.fooddelivery.ui.screens.cart.components.VoucherSelectionSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCheckout: () -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showVoucherSheet by remember { mutableStateOf(false) }

    CartContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onNavigateBack,
        onCheckoutClick = onNavigateToCheckout,
        onShowVoucherSheet = { showVoucherSheet = true }
    )

    if (showVoucherSheet) {
        ModalBottomSheet(
            onDismissRequest = { showVoucherSheet = false },
            containerColor = Color.White,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            VoucherSelectionSheet(
                vouchers = state.availableVouchers,
                selectedVoucherId = state.selectedVoucher?.id,
                promoCode = state.promoCode,
                promoError = state.promoError,
                onPromoCodeChange = { viewModel.onEvent(CartEvent.PromoCodeChanged(it)) },
                onApplyPromoCode = { viewModel.onEvent(CartEvent.ApplyPromoCode) },
                onVoucherSelected = { viewModel.onEvent(CartEvent.ApplyVoucher(it)) },
                onConfirm = { showVoucherSheet = false },
                onDismiss = { showVoucherSheet = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartContent(
    state: CartState,
    onEvent: (CartEvent) -> Unit,
    onBackClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    onShowVoucherSheet: () -> Unit
) {
    Scaffold(
        topBar = {
            DFoodTopBar(
                title = "My Cart",
                onBackClick = onBackClick,
                actions = {
                    if (!state.isCartEmpty) {
                        IconButton(onClick = { onEvent(CartEvent.ClearCart) }) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Clear Cart",
                                tint = Color.Red.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (!state.isCartEmpty) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    DFoodButton(
                        text = "Proceed to Checkout",
                        onClick = onCheckoutClick,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (state.isCartEmpty) {
            EmptyCartView(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    RestaurantHeader()
                }

                items(
                    items = state.items,
                    key = { it.food.id + it.size }
                ) { item ->
                    SwipeToDeleteContainer(
                        onDelete = { onEvent(CartEvent.RemoveItem(item.food.id, item.size)) }
                    ) {
                        CartItemCard(
                            item = item,
                            onIncrease = { onEvent(CartEvent.UpdateQuantity(item.food.id, item.size, 1)) },
                            onDecrease = { onEvent(CartEvent.UpdateQuantity(item.food.id, item.size, -1)) },
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }
                }

                item {
                    VoucherSection(
                        onSelectVoucherClick = onShowVoucherSheet,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
                    )
                }

                item {
                    BillBreakdown(
                        subtotal = state.subTotal,
                        deliveryFee = state.deliveryFee,
                        discount = state.discount,
                        total = state.total,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }
    }
}

@Composable
fun EmptyCartView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Your cart is empty",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Looks like you haven't added anything yet",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun RestaurantHeader() {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        Text(
            text = "Rose Garden Restaurant",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "West 4th Avenue, NY",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteContainer(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) 
                Color.Red.copy(alpha = 0.1f) else Color.Transparent
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .background(color, MaterialTheme.shapes.large),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }
        },
        enableDismissFromStartToEnd = false,
        content = { content() }
    )
}
