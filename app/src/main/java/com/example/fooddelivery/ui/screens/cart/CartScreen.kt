package com.example.fooddelivery.ui.screens.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fooddelivery.domain.model.Voucher
import com.example.fooddelivery.ui.components.button.DFoodButton
import com.example.fooddelivery.ui.components.topbar.DFoodTopBar
import com.example.fooddelivery.ui.screens.cart.components.BillBreakdown
import com.example.fooddelivery.ui.screens.cart.components.CartItemCard
import com.example.fooddelivery.ui.screens.cart.components.EmptyCartView
import com.example.fooddelivery.ui.screens.cart.components.RestaurantHeader
import com.example.fooddelivery.ui.screens.cart.components.SwipeToDeleteContainer
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
    var selectedVoucher by remember { mutableStateOf<Voucher?>(null) }

    CartContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onNavigateBack,
        onCheckoutClick = onNavigateToCheckout,
        onShowVoucherSheet = { showVoucherSheet = true }
    )

    if (showVoucherSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                selectedVoucher = null
                showVoucherSheet = false
            },
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
                onVoucherSelected = { selectedVoucher = it },
                onConfirm = { voucher ->
                    if (voucher != null) {
                        viewModel.onEvent(CartEvent.ApplyVoucher(voucher))
                    }
                    selectedVoucher = null
                    showVoucherSheet = false
                },
                onDismiss = {
                    selectedVoucher = null
                    showVoucherSheet = false
                }
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
                    RestaurantHeader(
                        restaurantName = state.items.firstOrNull()?.restaurantName ?: "",
                        restaurantAddress = null
                    )
                }

                items(
                    items = state.items,
                    key = { "${it.food.id}::${it.size}" }
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
                        promoCode = state.promoCode,
                        onPromoCodeChange = { onEvent(CartEvent.PromoCodeChanged(it)) },
                        onApplyPromoCode = { onEvent(CartEvent.ApplyPromoCode) },
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






